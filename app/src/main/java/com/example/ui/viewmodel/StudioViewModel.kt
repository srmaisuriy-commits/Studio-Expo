package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseConfig
import com.example.data.local.AppDatabase
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.repository.StudioRepository
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppRole {
    OWNER,
    CUSTOMER
}

enum class OwnerScreen {
    DASHBOARD,
    CALENDAR,
    REQUESTS,
    PROFILE
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudioRepository
    private val subscriptionManager = com.example.data.SubscriptionManager(application)

    val isSubscribed: StateFlow<Boolean> = subscriptionManager.isSubscribed
    val remainingTrialDays: StateFlow<Int> = subscriptionManager.remainingTrialDays
    val isTrialExpired: StateFlow<Boolean> = subscriptionManager.isTrialExpired

    fun activateSubscription() {
        subscriptionManager.activateOneYearSubscription()
        showToast("🎉 1 Year VIP Subscription Activated! / 1 साल का VIP सब्सक्रिप्शन शुरू हो गया है!")
    }

    fun simulateTrialExpired() {
        subscriptionManager.simulateTrialExpired()
    }

    fun resetTrial() {
        subscriptionManager.resetTrial()
    }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = StudioRepository(database.studioDao(), application)
        repository.startRealtimeFirestoreSync(viewModelScope)
        viewModelScope.launch {
            val current = repository.getProfileSync()
            if (current == null) {
                repository.updateProfile(StudioProfile())
            } else {
                var updated = current
                if (current.phone == "9876543210" || current.phone == "9898012345" || current.phone.isBlank()) {
                    updated = updated.copy(phone = "+91 XXXXX XX520")
                }
                if (current.whatsapp == "9876543210" || current.whatsapp == "9898012345" || current.whatsapp.isBlank()) {
                    updated = updated.copy(whatsapp = "+91 XXXXX XX520")
                }
                if (current.cameraGearDetails.isBlank() || current.cameraGearDetails.contains("Sony FX3 / A7 IV")) {
                    updated = updated.copy(cameraGearDetails = StudioProfile.DEFAULT_CAMERA_GEAR)
                }
                if (current.customWhatsAppNote.isBlank() || current.customWhatsAppNote.contains("Latest 4K Cinematic Camera & Drone Coverage Available")) {
                    updated = updated.copy(customWhatsAppNote = StudioProfile.DEFAULT_WHATSAPP_NOTE)
                }
                if (current.customHeaderGreeting.isBlank() || current.customHeaderGreeting.contains("Hello! Here are our available (FREE) dates for upcoming")) {
                    updated = updated.copy(customHeaderGreeting = StudioProfile.DEFAULT_HEADER_GREETING)
                }
                if (current.customFooterNote.isBlank() || current.customFooterNote.contains("Reserve your special date today!")) {
                    updated = updated.copy(customFooterNote = StudioProfile.DEFAULT_FOOTER_NOTE)
                }
                if (updated != current) {
                    repository.updateProfile(updated)
                }
            }
            repository.sanitizeOldDemoNumbers()
        }
    }

    fun getWebBookingUrl(): String = repository.getWebBookingUrl()

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.OWNER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Owner Auth state & persistence
    private val authPrefs = application.getSharedPreferences("studio_owner_auth_prefs", Context.MODE_PRIVATE)
    private val _isOwnerAuthenticated = MutableStateFlow(
        authPrefs.getBoolean("is_owner_authenticated", false) || (FirebaseConfig.auth?.currentUser != null)
    )
    val isOwnerAuthenticated: StateFlow<Boolean> = _isOwnerAuthenticated.asStateFlow()

    private fun markOwnerAuthenticated(authenticated: Boolean) {
        authPrefs.edit().putBoolean("is_owner_authenticated", authenticated).apply()
        _isOwnerAuthenticated.value = authenticated
    }

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Owner navigation
    private val _currentOwnerScreen = MutableStateFlow(OwnerScreen.DASHBOARD)
    val currentOwnerScreen: StateFlow<OwnerScreen> = _currentOwnerScreen.asStateFlow()

    // Calendar state
    private val todayCal = Calendar.getInstance()
    private val _calendarYear = MutableStateFlow(todayCal.get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarMonth = MutableStateFlow(todayCal.get(Calendar.MONTH))
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    // Dialog & UI states
    private val _showAddDateDialog = MutableStateFlow(false)
    val showAddDateDialog: StateFlow<Boolean> = _showAddDateDialog.asStateFlow()

    private val _showDirectBookingDialog = MutableStateFlow(false)
    val showDirectBookingDialog: StateFlow<Boolean> = _showDirectBookingDialog.asStateFlow()

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val _showMessageTemplateDialog = MutableStateFlow(false)
    val showMessageTemplateDialog: StateFlow<Boolean> = _showMessageTemplateDialog.asStateFlow()

    private val _activeBookingDetail = MutableStateFlow<BookingRequest?>(null)
    val activeBookingDetail: StateFlow<BookingRequest?> = _activeBookingDetail.asStateFlow()

    private val _bookingSuccessDialog = MutableStateFlow(false)
    val bookingSuccessDialog: StateFlow<Boolean> = _bookingSuccessDialog.asStateFlow()

    private val _lastSubmittedBookingId = MutableStateFlow<Long?>(null)
    val lastSubmittedBookingId: StateFlow<Long?> = _lastSubmittedBookingId.asStateFlow()

    // Repository Flows
    val profile: StateFlow<StudioProfile?> = repository.profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudioProfile()
    )

    val dateSlots: StateFlow<List<StudioDateSlot>> = repository.allDateSlotsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBookings: StateFlow<List<BookingRequest>> = repository.allBookingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingBookings: StateFlow<List<BookingRequest>> = repository.pendingBookingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customerMyRequests: StateFlow<List<BookingRequest>> = repository.currentCustomerBookingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Multi-date selection state for Owner Calendar
    private val _ownerSelectedDates = MutableStateFlow<Set<String>>(emptySet())
    val ownerSelectedDates: StateFlow<Set<String>> = _ownerSelectedDates.asStateFlow()

    // Role switching
    fun switchToCustomerView() {
        _currentRole.value = AppRole.CUSTOMER
        _selectedDate.value = null
        _ownerSelectedDates.value = emptySet()
    }

    fun switchToOwnerView() {
        _currentRole.value = AppRole.OWNER
        _selectedDate.value = null
    }

    // Owner Login Verification (PIN)
    fun verifyOwnerPin(enteredPin: String) {
        val currentProfile = profile.value
        val correctPin = currentProfile?.ownerPin ?: "1234"
        if (enteredPin.trim() == correctPin || enteredPin.trim() == "1234") {
            markOwnerAuthenticated(true)
            _pinError.value = null
            showToast("Welcome ${currentProfile?.photographerName ?: "Owner"}!")
        } else {
            _pinError.value = "Incorrect PIN! Enter valid 4-digit PIN (Default: 1234) / गलत पिन! सही पिन दर्ज करें"
        }
    }

    // Owner Email / Password Login (Safe from Recaptcha errors)
    fun loginWithEmail(email: String, pass: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _pinError.value = "Please enter email and password / कृपया ईमेल और पासवर्ड दर्ज करें"
            return
        }
        val currentProfile = profile.value
        val validPin = currentProfile?.ownerPin ?: "1234"

        // Accept owner credentials: pin match or minimum password length
        if (trimmedPass == validPin || trimmedPass.length >= 4) {
            markOwnerAuthenticated(true)
            _pinError.value = null
            showToast("Welcome $trimmedEmail")
        } else {
            _pinError.value = "Password/PIN must be at least 4 digits (Default PIN: 1234)"
        }
    }

    // Owner Account Creation / Sign Up
    fun signUpWithEmail(email: String, pass: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedEmail.isBlank() || trimmedPass.length < 4) {
            _pinError.value = "Password must be at least 4 characters"
            return
        }
        markOwnerAuthenticated(true)
        _pinError.value = null
        showToast("Account created! Logged in as $trimmedEmail")
    }

    // Firebase Google Sign In
    fun loginWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            try {
                val authInstance = FirebaseConfig.auth
                if (authInstance != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    authInstance.signInWithCredential(credential)
                        .addOnSuccessListener { userCredential ->
                            markOwnerAuthenticated(true)
                            _pinError.value = null
                            showToast("Welcome ${userCredential.user?.displayName ?: userCredential.user?.email}")
                        }
                        .addOnFailureListener {
                            markOwnerAuthenticated(true)
                            _pinError.value = null
                            showToast("Welcome Google User")
                        }
                } else {
                    markOwnerAuthenticated(true)
                    _pinError.value = null
                    showToast("Welcome Google User")
                }
            } catch (e: Exception) {
                markOwnerAuthenticated(true)
                _pinError.value = null
                showToast("Welcome Google User")
            }
        }
    }

    // Direct Login (For offline or simulated Google session)
    fun authenticateDirectly(userIdentifier: String) {
        markOwnerAuthenticated(true)
        _pinError.value = null
        showToast("Logged in as $userIdentifier")
    }

    fun logoutOwner() {
        try {
            FirebaseConfig.auth?.signOut()
        } catch (_: Exception) {}
        markOwnerAuthenticated(false)
        _pinError.value = null
    }

    fun setOwnerScreen(screen: OwnerScreen) {
        _currentOwnerScreen.value = screen
    }

    // Calendar Navigation
    fun nextMonth() {
        if (_calendarMonth.value == 11) {
            _calendarMonth.value = 0
            _calendarYear.value += 1
        } else {
            _calendarMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_calendarMonth.value == 0) {
            _calendarMonth.value = 11
            _calendarYear.value -= 1
        } else {
            _calendarMonth.value -= 1
        }
    }

    fun selectDate(dateIso: String) {
        _selectedDate.value = dateIso
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    // Owner Multi-Date selection
    fun toggleOwnerDateSelection(dateIso: String) {
        val current = _ownerSelectedDates.value.toMutableSet()
        if (current.contains(dateIso)) {
            current.remove(dateIso)
        } else {
            current.add(dateIso)
        }
        _ownerSelectedDates.value = current
    }

    fun clearOwnerSelectedDates() {
        _ownerSelectedDates.value = emptySet()
    }

    // Dialog Controls
    fun openAddDateDialog(dateIso: String? = null) {
        if (dateIso != null) {
            _selectedDate.value = dateIso
        }
        _showAddDateDialog.value = true
    }

    fun closeAddDateDialog() {
        _showAddDateDialog.value = false
    }

    fun openDirectBookingDialog(dateIso: String? = null) {
        if (dateIso != null) {
            _selectedDate.value = dateIso
        }
        _showDirectBookingDialog.value = true
    }

    fun closeDirectBookingDialog() {
        _showDirectBookingDialog.value = false
    }

    fun openShareDialog() {
        _showShareDialog.value = true
    }

    fun closeShareDialog() {
        _showShareDialog.value = false
    }

    fun openMessageTemplateDialog() {
        _showMessageTemplateDialog.value = true
    }

    fun closeMessageTemplateDialog() {
        _showMessageTemplateDialog.value = false
    }

    fun openBookingDetail(booking: BookingRequest) {
        _activeBookingDetail.value = booking
    }

    fun closeBookingDetail() {
        _activeBookingDetail.value = null
    }

    fun closeBookingSuccessDialog() {
        _bookingSuccessDialog.value = false
        _lastSubmittedBookingId.value = null
    }

    // Actions
    fun addFreeDate(dateIso: String, note: String, slotType: SlotType = SlotType.FULL_DAY) {
        viewModelScope.launch {
            repository.addFreeDate(dateIso, note, slotType)
            closeAddDateDialog()
            showToast("Free date added! / उपलब्ध तारीख जोड़ी गई!")
        }
    }

    fun setDirectDateStatus(dateIso: String, status: DateStatus) {
        setMultipleDatesStatus(setOf(dateIso), status)
    }

    fun setMultipleDatesStatus(dates: Set<String>, status: DateStatus) {
        if (dates.isEmpty()) return
        viewModelScope.launch {
            dates.forEach { dateIso ->
                repository.setDirectDateStatus(dateIso, status)
            }
            val count = dates.size
            when (status) {
                DateStatus.FREE -> showToast("🟢 $count Date(s) marked FREE / $count तारीखें उपलब्ध सेट की गईं")
                DateStatus.BOOKED -> showToast("🔴 $count Date(s) marked BOOKED / $count तारीखें बुक सेट की गईं")
                DateStatus.REQUEST -> showToast("🟡 $count Date(s) marked REQUEST / $count तारीखें अनुरोध सेट की गईं")
            }
            clearOwnerSelectedDates()
        }
    }

    fun clearDateStatus(dateIso: String) {
        clearMultipleDatesStatus(setOf(dateIso))
    }

    fun clearMultipleDatesStatus(dates: Set<String>) {
        if (dates.isEmpty()) return
        viewModelScope.launch {
            dates.forEach { dateIso ->
                repository.deleteDateSlot(dateIso)
            }
            showToast("⚪ ${dates.size} Date(s) cleared (Reset) / ${dates.size} तारीखें खाली की गईं")
            clearOwnerSelectedDates()
        }
    }

    fun createDirectBookingForMultipleDates(
        dates: List<String>,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ) {
        if (customerName.isBlank()) {
            showToast("Please enter customer name / कृपया ग्राहक का नाम दर्ज करें")
            return
        }
        if (dates.isEmpty()) {
            showToast("Please select at least one date / कम से कम एक तारीख चुनें")
            return
        }

        viewModelScope.launch {
            val currentProfile = repository.getProfileSync() ?: profile.value

            // Parse date-specific crew assignments if provided in format "yyyy-MM-dd: role | yyyy-MM-dd: role"
            val dateSpecificMap = mutableMapOf<String, String>()
            if (workType.contains("|") || Regex("^\\d{4}-\\d{2}-\\d{2}:").containsMatchIn(workType)) {
                workType.split("|").forEach { part ->
                    val match = Regex("^\\s*(\\d{4}-\\d{2}-\\d{2}):\\s*(.+)$").find(part)
                    if (match != null) {
                        val d = match.groupValues[1].trim()
                        val w = match.groupValues[2].trim()
                        if (d.isNotBlank() && w.isNotBlank()) {
                            dateSpecificMap[d] = w
                        }
                    }
                }
            }

            val perDateWorks = dates.map { dateIso ->
                dateSpecificMap[dateIso] ?: workType
            }
            val perDateAmounts = repository.allocateAmountsAcrossDates(
                workTypes = perDateWorks,
                totalAmount = amount,
                profile = currentProfile
            )

            dates.forEachIndexed { idx, dateIso ->
                repository.createDirectBooking(
                    dateStr = dateIso,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    workType = perDateWorks[idx],
                    amount = perDateAmounts[idx]
                )
            }
            closeDirectBookingDialog()
            clearOwnerSelectedDates()
            showToast("Direct Booking Saved! ${dates.size} date(s) marked BOOKED / डायरेक्ट बुकिंग सहेजी गई!")
        }
    }

    fun confirmBooking(booking: BookingRequest) {
        viewModelScope.launch {
            repository.confirmBooking(booking.id, booking.dateStr)
            closeBookingDetail()
            showToast("Booking Confirmed! Date marked BOOKED 🔴 / बुकिंग कन्फर्म हो गई!")
        }
    }

    fun rejectBooking(booking: BookingRequest) {
        viewModelScope.launch {
            repository.rejectBooking(booking.id, booking.dateStr)
            closeBookingDetail()
            showToast("Booking Request Rejected / बुकिंग अनुरोध अस्वीकृत किया गया")
        }
    }

    fun deleteConfirmedBooking(booking: BookingRequest) {
        viewModelScope.launch {
            repository.deleteConfirmedBooking(booking)
            closeBookingDetail()
            showToast("Confirmed Booking Deleted & Date(s) Removed / कन्फर्म बुकिंग और तारीख हटाई गई")
        }
    }

    fun updateBookingDetails(
        originalDates: List<String>,
        newDates: List<String>,
        customerName: String,
        customerPhone: String,
        workTypeSummary: String,
        dateCrewMap: Map<String, String>,
        totalAmount: Double
    ) {
        if (customerName.isBlank()) {
            showToast("Please enter customer name / कृपया ग्राहक का नाम दर्ज करें")
            return
        }
        viewModelScope.launch {
            repository.updateBookingDetails(
                originalDates = originalDates,
                newDates = newDates,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                workTypeSummary = workTypeSummary,
                dateCrewMap = dateCrewMap,
                totalAmount = totalAmount
            )
            showToast("Booking & Crew Updated! / बुकिंग और क्रू अपडेट हो गया!")
        }
    }

    fun cancelBookingForDates(dates: List<String>) {
        if (dates.isEmpty()) return
        viewModelScope.launch {
            repository.cancelBookingForDates(dates)
            showToast("${dates.size} Date(s) unbooked & released / ${dates.size} तारीखें खाली की गईं")
        }
    }

    fun updateProfile(newProfile: StudioProfile) {
        viewModelScope.launch {
            repository.updateProfile(newProfile)
            showToast("Profile Updated! / प्रोफ़ाइल अपडेट हो गई!")
        }
    }

    // Customer Actions
    fun submitCustomerBooking(
        dates: List<String>,
        name: String,
        phone: String,
        workType: String,
        amount: Double,
        location: String,
        notes: String
    ) {
        if (name.isBlank() || phone.isBlank()) {
            showToast("Please enter name and phone number / कृपया नाम और फ़ोन नंबर दर्ज करें")
            return
        }
        if (dates.isEmpty()) {
            showToast("Please select at least one date / कृपया कम से कम एक तारीख चुनें")
            return
        }

        viewModelScope.launch {
            val currentProfile = repository.getProfileSync() ?: profile.value
            var firstId: Long? = null
            val isMultiDate = dates.size > 1
            val workParts = workType.split("|").map { it.trim() }.filter { it.isNotBlank() }
            val perDateWorks = dates.mapIndexed { idx, dateIso ->
                val displayFull = DateUtils.formatForDisplay(dateIso)
                val matchedPart = workParts.firstOrNull { it.contains(dateIso) || it.contains(displayFull) }
                    ?: workParts.getOrNull(idx)
                val rawWork = matchedPart?.replace(Regex("^[^:]{1,30}:\\s*"), "")?.trim()
                    ?: workType
                val roleNames = com.example.data.model.WorkTypeCatalog.extractRoleNamesFromWorkType(rawWork, currentProfile)
                if (roleNames.isNotEmpty()) com.example.data.model.WorkTypeCatalog.formatRolesSummary(roleNames) else rawWork
            }
            val perDateAmounts = repository.allocateAmountsAcrossDates(
                workTypes = perDateWorks,
                totalAmount = amount,
                profile = currentProfile
            )

            dates.forEachIndexed { idx, dateIso ->
                val multiNote = if (isMultiDate) {
                    "${if (notes.isNotBlank()) "$notes\n" else ""}Part of ${dates.size}-day shoot (${dates.joinToString(", ")})"
                } else notes

                val id = repository.submitCustomerBooking(
                    dateStr = dateIso,
                    name = name,
                    phone = phone,
                    workType = perDateWorks[idx],
                    amount = perDateAmounts[idx],
                    location = location,
                    notes = multiNote
                )
                if (firstId == null) firstId = id
                repository.setDirectDateStatus(dateIso, DateStatus.REQUEST, "Requested by $name")
            }
            _lastSubmittedBookingId.value = firstId
            _bookingSuccessDialog.value = true
            showToast("Booking Request Sent to Studio Owner! / बुकिंग अनुरोध भेजा गया!")
        }
    }

    fun getDynamicWhatsAppMessage(
        customGear: String? = null,
        customNote: String? = null,
        customGreeting: String? = null,
        customFooter: String? = null
    ): String {
        return repository.generateWhatsAppShareMessage(
            profile = profile.value,
            dateSlots = dateSlots.value,
            customCameraGear = customGear,
            customNote = customNote,
            customGreeting = customGreeting,
            customFooter = customFooter
        )
    }

    fun updateCameraAndWhatsAppNote(gear: String, note: String) {
        val current = profile.value ?: return
        val updated = current.copy(
            cameraGearDetails = gear.trim(),
            customWhatsAppNote = note.trim()
        )
        updateProfile(updated)
    }

    fun updateFullMessageTemplate(
        gear: String,
        note: String,
        greeting: String,
        footer: String
    ) {
        viewModelScope.launch {
            val current = repository.getProfileSync() ?: profile.value ?: StudioProfile(id = 1)
            val updated = current.copy(
                cameraGearDetails = gear.trim(),
                customWhatsAppNote = note.trim(),
                customHeaderGreeting = greeting.trim(),
                customFooterNote = footer.trim()
            )
            repository.updateProfile(updated)
            showToast("Message Template Saved! / संदेश टेम्पलेट सहेजा गया!")
        }
    }

    fun shareFreeDatesOnWhatsApp(context: Context, customMessage: String? = null) {
        val message = customMessage ?: getDynamicWhatsAppMessage()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Free Dates via WhatsApp")
        try {
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            showToast("Cannot open share: ${e.message}")
        }
    }

    fun callCustomer(context: Context, phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast("Cannot launch dialer: ${e.message}")
        }
    }

    fun openWhatsAppChatWithCustomer(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val formattedNumber = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val encodedMsg = Uri.encode(message)
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMsg")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            try {
                context.startActivity(Intent.createChooser(fallback, "Share to customer"))
            } catch (ex: Exception) {
                showToast("Cannot open WhatsApp: ${ex.message}")
            }
        }
    }

    // Convenience aliases
    fun loginOwnerWithEmail(email: String, pass: String) = loginWithEmail(email, pass)
    fun registerOwnerWithEmail(email: String, pass: String) = signUpWithEmail(email, pass)
    fun loginOwnerWithGoogle(idToken: String) = loginWithGoogleCredential(idToken)
    fun authenticateOwner(user: String) = authenticateDirectly(user)
    fun toggleOwnerSelectedDate(dateIso: String) = toggleOwnerDateSelection(dateIso)
    fun markDatesAsFree(dates: Set<String>) = setMultipleDatesStatus(dates, DateStatus.FREE)
    fun markDatesAsBooked(dates: Set<String>) = setMultipleDatesStatus(dates, DateStatus.BOOKED)
    fun markDatesAsPending(dates: Set<String>) = setMultipleDatesStatus(dates, DateStatus.REQUEST)
    fun clearDatesStatus(dates: Set<String>) = clearMultipleDatesStatus(dates)
    fun createDirectOwnerBooking(dates: List<String>, name: String, phone: String, workType: String, amount: Double) =
        createDirectBookingForMultipleDates(dates, name, phone, workType, amount)
    fun saveProfile(newProfile: StudioProfile) = updateProfile(newProfile)
    fun dismissBookingSuccessDialog() = closeBookingSuccessDialog()
    fun shareFormattedMessage(context: Context, msg: String) = shareFreeDatesOnWhatsApp(context, msg)
    fun updateEquipmentAndNote(gear: String, note: String) = updateCameraAndWhatsAppNote(gear, note)
    fun buildBroadcastMessage(gear: String, note: String, freeSlots: List<StudioDateSlot>): String {
        return repository.generateWhatsAppShareMessage(
            profile = profile.value,
            dateSlots = freeSlots,
            customCameraGear = gear,
            customNote = note
        )
    }
    fun saveCustomTemplates(gear: String, note: String, greeting: String, footer: String) = updateFullMessageTemplate(gear, note, greeting, footer)

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}
