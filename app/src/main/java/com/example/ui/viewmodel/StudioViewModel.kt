package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.repository.StudioRepository
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

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = StudioRepository(database.studioDao())
    }

    // Role state
    private val _currentRole = MutableStateFlow(AppRole.OWNER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Owner Auth state
    private val _isOwnerAuthenticated = MutableStateFlow(false)
    val isOwnerAuthenticated: StateFlow<Boolean> = _isOwnerAuthenticated.asStateFlow()

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
        initialValue = null
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
        if (enteredPin == correctPin) {
            _isOwnerAuthenticated.value = true
            _pinError.value = null
        } else {
            _pinError.value = "Incorrect PIN! Enter valid 4-digit PIN (Default: 1234) / गलत पिन! सही पिन दर्ज करें"
        }
    }

    // Firebase Email / Password Login
    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _pinError.value = "Please enter email and password / कृपया ईमेल और पासवर्ड दर्ज करें"
            return
        }
        viewModelScope.launch {
            try {
                com.example.data.firebase.FirebaseConfig.auth.signInWithEmailAndPassword(email.trim(), pass)
                    .addOnSuccessListener { userCredential ->
                        _isOwnerAuthenticated.value = true
                        _pinError.value = null
                        showToast("Welcome ${userCredential.user?.displayName ?: userCredential.user?.email}")
                    }
                    .addOnFailureListener { exception ->
                        _pinError.value = exception.localizedMessage ?: "Login failed"
                    }
            } catch (e: Exception) {
                _pinError.value = e.localizedMessage ?: "Login error"
            }
        }
    }

    // Firebase Email / Password Sign Up
    fun signUpWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank() || pass.length < 6) {
            _pinError.value = "Password must be at least 6 characters / पासवर्ड कम से कम 6 अक्षरों का होना चाहिए"
            return
        }
        viewModelScope.launch {
            try {
                com.example.data.firebase.FirebaseConfig.auth.createUserWithEmailAndPassword(email.trim(), pass)
                    .addOnSuccessListener { userCredential ->
                        _isOwnerAuthenticated.value = true
                        _pinError.value = null
                        showToast("Account created! Logged in as ${userCredential.user?.email}")
                    }
                    .addOnFailureListener { exception ->
                        _pinError.value = exception.localizedMessage ?: "Sign up failed"
                    }
            } catch (e: Exception) {
                _pinError.value = e.localizedMessage ?: "Sign up error"
            }
        }
    }

    // Firebase Google Sign In
    fun loginWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                com.example.data.firebase.FirebaseConfig.auth.signInWithCredential(credential)
                    .addOnSuccessListener { userCredential ->
                        _isOwnerAuthenticated.value = true
                        _pinError.value = null
                        showToast("Welcome ${userCredential.user?.displayName ?: userCredential.user?.email}")
                    }
                    .addOnFailureListener { exception ->
                        _pinError.value = exception.localizedMessage ?: "Google Sign-In failed"
                    }
            } catch (e: Exception) {
                _pinError.value = e.localizedMessage ?: "Google sign-in error"
            }
        }
    }

    // Direct Login (For offline or simulated Google session)
    fun authenticateDirectly(userIdentifier: String) {
        _isOwnerAuthenticated.value = true
        _pinError.value = null
        showToast("Logged in as $userIdentifier")
    }

    fun logoutOwner() {
        try {
            com.example.data.firebase.FirebaseConfig.auth.signOut()
        } catch (_: Exception) {}
        _isOwnerAuthenticated.value = false
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
        _ownerSelectedDates.value = emptySet()
    }

    fun toggleOwnerSelectedDate(dateIso: String) {
        val current = _ownerSelectedDates.value.toMutableSet()
        if (current.contains(dateIso)) {
            current.remove(dateIso)
        } else {
            current.add(dateIso)
        }
        _ownerSelectedDates.value = current
        _selectedDate.value = current.lastOrNull()
    }

    fun clearOwnerSelectedDates() {
        _ownerSelectedDates.value = emptySet()
        _selectedDate.value = null
    }

    // Dialog controls
    fun openAddDateDialog(dateIso: String? = null) {
        if (dateIso != null) {
            _selectedDate.value = dateIso
            _ownerSelectedDates.value = setOf(dateIso)
        }
        _showAddDateDialog.value = true
    }

    fun closeAddDateDialog() {
        _showAddDateDialog.value = false
    }

    fun openDirectBookingDialog(dateIso: String? = null) {
        if (dateIso != null) {
            _selectedDate.value = dateIso
            _ownerSelectedDates.value = setOf(dateIso)
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

    fun openBookingDetail(request: BookingRequest) {
        _activeBookingDetail.value = request
    }

    fun closeBookingDetail() {
        _activeBookingDetail.value = null
    }

    fun dismissBookingSuccess() {
        _bookingSuccessDialog.value = false
    }

    // ==========================================
    // CALENDAR MULTI-DATE CONTROL
    // ==========================================

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
                DateStatus.FREE -> showToast("🟢 $count Date(s) marked FREE (Calendar & WhatsApp updated) / $count तारीखें उपलब्ध सेट की गईं")
                DateStatus.BOOKED -> showToast("🔴 $count Date(s) marked BOOKED (Calendar & WhatsApp updated) / $count तारीखें बुक सेट की गईं")
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
            showToast("⚪ ${dates.size} Date(s) cleared (Reset) / ${dates.size} तारीखें खाली (रीसेट) की गईं")
            clearOwnerSelectedDates()
        }
    }

    fun createDirectBooking(
        dateIso: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ) {
        createDirectBookingForMultipleDates(
            dates = listOf(dateIso),
            customerName = customerName,
            customerPhone = customerPhone,
            workType = workType,
            amount = amount
        )
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
            val isMulti = dates.size > 1
            val perDateAmount = if (isMulti) amount / dates.size else amount
            dates.forEach { dateIso ->
                repository.createDirectBooking(
                    dateStr = dateIso,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    workType = workType,
                    amount = perDateAmount
                )
            }
            closeDirectBookingDialog()
            clearOwnerSelectedDates()
            showToast("✅ Booking confirmed for ${dates.size} date(s) (BOOKED) / ${dates.size} तारीखों की बुकिंग सुरक्षित हो गई!")
        }
    }

    fun deleteFreeDate(dateIso: String) {
        viewModelScope.launch {
            repository.deleteDateSlot(dateIso)
            showToast("Date removed / तारीख हटा दी गई")
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
            var firstId: Long? = null
            val isMultiDate = dates.size > 1
            val perDateAmount = if (isMultiDate) amount / dates.size else amount
            dates.forEach { dateIso ->
                val multiNote = if (isMultiDate) {
                    "$notes [Multi-date Shoot: ${dates.joinToString(", ")}]".trim()
                } else notes

                val id = repository.submitCustomerBooking(
                    dateStr = dateIso,
                    customerName = name,
                    customerPhone = phone,
                    workType = workType,
                    amount = perDateAmount,
                    location = location,
                    notes = multiNote
                )
                if (firstId == null) firstId = id
            }
            _lastSubmittedBookingId.value = firstId
            _bookingSuccessDialog.value = true
            _selectedDate.value = null
        }
    }

    fun submitCustomerBooking(
        dateIso: String,
        name: String,
        phone: String,
        workType: String,
        amount: Double,
        location: String,
        notes: String
    ) {
        submitCustomerBooking(listOf(dateIso), name, phone, workType, amount, location, notes)
    }

    // Dynamic WhatsApp generator from current calendar data
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

    // WhatsApp Intents
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
            showToast("Sharing failed: ${e.message}")
        }
    }

    fun openWhatsAppChatWithCustomer(context: Context, customerPhone: String, message: String) {
        try {
            val cleanPhone = customerPhone.replace("+", "").replace(" ", "").replace("-", "")
            val url = "https://api.whatsapp.com/send?phone=91$cleanPhone&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast("WhatsApp not installed or could not be opened")
        }
    }

    fun callCustomer(context: Context, customerPhone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$customerPhone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast("Cannot dial phone")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}
