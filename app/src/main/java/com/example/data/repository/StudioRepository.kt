package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseConfig
import com.example.data.local.DateUtils
import com.example.data.local.StudioDao
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.model.WorkTypeCatalog
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class StudioRepository(
    private val dao: StudioDao,
    private val context: Context? = null
) {

    val profileFlow: Flow<StudioProfile?> = dao.getProfile()
    val allDateSlotsFlow: Flow<List<StudioDateSlot>> = dao.getAllDateSlots()
    val freeDateSlotsFlow: Flow<List<StudioDateSlot>> = dao.getDateSlotsByStatus(DateStatus.FREE)
    val allBookingsFlow: Flow<List<BookingRequest>> = dao.getAllBookings()
    val pendingBookingsFlow: Flow<List<BookingRequest>> = dao.getBookingsByStatus(BookingStatus.PENDING)

    val currentCustomerBookingsFlow: Flow<List<BookingRequest>> = dao.getAllBookings().map { list ->
        list.filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED }
    }

    private var cachedProfile: StudioProfile? = null
    private val deletedDatesGuard = ConcurrentHashMap<String, Long>()
    private val deletedBookingsGuard = ConcurrentHashMap<String, Long>()

    private fun markDateDeleted(dateStr: String) {
        deletedDatesGuard[dateStr.trim()] = System.currentTimeMillis() + 8000L
    }

    private fun unmarkDateDeleted(dateStr: String) {
        deletedDatesGuard.remove(dateStr.trim())
    }

    private fun isDateDeleteGuarded(dateStr: String): Boolean {
        val expiry = deletedDatesGuard[dateStr.trim()] ?: return false
        if (System.currentTimeMillis() < expiry) return true
        deletedDatesGuard.remove(dateStr.trim())
        return false
    }

    private fun markBookingDeleted(booking: BookingRequest) {
        val nowPlus = System.currentTimeMillis() + 8000L
        deletedBookingsGuard["${booking.customerPhone}|${booking.dateStr}"] = nowPlus
        deletedBookingsGuard["created:${booking.createdAt}"] = nowPlus
    }

    private fun isBookingDeleteGuarded(phone: String, dateStr: String, createdAt: Long): Boolean {
        val now = System.currentTimeMillis()
        val k1 = "$phone|$dateStr"
        val k2 = "created:$createdAt"
        val e1 = deletedBookingsGuard[k1]
        if (e1 != null) {
            if (now < e1) return true else deletedBookingsGuard.remove(k1)
        }
        val e2 = deletedBookingsGuard[k2]
        if (e2 != null) {
            if (now < e2) return true else deletedBookingsGuard.remove(k2)
        }
        return false
    }

    suspend fun getProfileSync(): StudioProfile? {
        val p = dao.getProfileSync()
        if (p != null) cachedProfile = p
        return p
    }

    suspend fun updateProfile(profile: StudioProfile) {
        cachedProfile = profile
        dao.updateProfile(profile)
        syncProfileToFirestore(profile)
    }

    suspend fun addFreeDate(
        dateStr: String,
        note: String,
        slotType: SlotType = SlotType.FULL_DAY
    ) {
        unmarkDateDeleted(dateStr)
        val slot = StudioDateSlot(
            dateStr = dateStr,
            status = DateStatus.FREE,
            note = note.trim(),
            slotType = slotType
        )
        dao.insertOrUpdateDateSlot(slot)
        syncSlotToFirestore(slot)
    }

    suspend fun setDirectDateStatus(
        dateStr: String,
        status: DateStatus,
        note: String = ""
    ) {
        unmarkDateDeleted(dateStr)
        val existing = dao.getDateSlot(dateStr)
        val slot = existing?.copy(status = status, note = note)
            ?: StudioDateSlot(dateStr = dateStr, status = status, note = note)
        dao.insertOrUpdateDateSlot(slot)
        syncSlotToFirestore(slot)
    }

    suspend fun deleteDateSlot(dateStr: String) {
        markDateDeleted(dateStr)
        val existingSlot = dao.getDateSlot(dateStr)
        dao.deleteDateSlot(dateStr)
        deleteSlotFromFirestore(dateStr)
        if (existingSlot != null && existingSlot.status == DateStatus.BOOKED) {
            cleanupConfirmedBookingsForDates(listOf(dateStr))
        }
    }

    suspend fun createDirectBooking(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ) {
        unmarkDateDeleted(dateStr)
        // Mark slot as booked
        val slot = StudioDateSlot(
            dateStr = dateStr,
            status = DateStatus.BOOKED,
            note = "Direct Booking: $workType",
            bookedCustomerName = customerName,
            bookedCustomerPhone = customerPhone,
            workType = workType,
            amount = amount
        )
        dao.insertOrUpdateDateSlot(slot)
        syncSlotToFirestore(slot)

        // Record confirmed booking request with persisted Room ID
        val booking = BookingRequest(
            dateStr = dateStr,
            customerName = customerName,
            customerPhone = customerPhone,
            workType = workType,
            amount = amount,
            status = BookingStatus.CONFIRMED
        )
        val newId = dao.insertBooking(booking)
        syncBookingToFirestore(booking.copy(id = newId))
    }

    suspend fun submitCustomerBooking(
        dateStr: String,
        name: String,
        phone: String,
        workType: String,
        amount: Double,
        location: String,
        notes: String
    ): Long {
        val booking = BookingRequest(
            dateStr = dateStr,
            customerName = name.trim(),
            customerPhone = phone.trim(),
            workType = workType,
            amount = amount,
            location = location.trim(),
            notes = notes.trim(),
            status = BookingStatus.PENDING
        )
        val id = dao.insertBooking(booking)
        syncBookingToFirestore(booking.copy(id = id))
        return id
    }

    suspend fun confirmBooking(bookingId: Long, dateStr: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
        val booking = dao.getBookingById(bookingId)
        val profile = getProfileSync() ?: cachedProfile
        val dates = dateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf(dateStr) }

        // Extract date-specific workType if workType is multi-date formatted ("date1: crew1 | date2: crew2")
        val workParts = (booking?.workType ?: "").split("|").map { it.trim() }.filter { it.isNotBlank() }
        val perDateWorks = dates.mapIndexed { idx, singleDate ->
            val displayFull = DateUtils.formatForDisplay(singleDate)
            val displayShort = displayFull.split(" ").take(2).joinToString(" ")
            val matchedPart = workParts.firstOrNull { part ->
                part.contains(singleDate) || part.contains(displayFull) || (displayShort.isNotBlank() && part.contains(displayShort))
            } ?: workParts.getOrNull(idx)
            val rawWork = matchedPart?.replace(Regex("^[^:]{1,30}:\\s*"), "")?.trim()
                ?: booking?.workType?.takeIf { it.isNotBlank() }
                ?: "Booked"
            val parsedNames = WorkTypeCatalog.extractRoleNamesFromWorkType(rawWork, profile)
            if (parsedNames.isNotEmpty()) WorkTypeCatalog.formatRolesSummary(parsedNames) else rawWork
        }

        val perDateAmounts = allocateAmountsAcrossDates(
            workTypes = perDateWorks,
            totalAmount = booking?.requestedAmount ?: 0.0,
            profile = profile
        )

        dates.forEachIndexed { idx, singleDate ->
            unmarkDateDeleted(singleDate)
            val slot = StudioDateSlot(
                dateStr = singleDate,
                status = DateStatus.BOOKED,
                note = perDateWorks[idx],
                bookedCustomerName = booking?.customerName ?: "",
                bookedCustomerPhone = booking?.customerPhone ?: "",
                workType = perDateWorks[idx],
                amount = perDateAmounts[idx]
            )
            dao.insertOrUpdateDateSlot(slot)
            syncSlotToFirestore(slot)
        }
        booking?.let {
            val confirmedBooking = it.copy(status = BookingStatus.CONFIRMED)
            updateOrSyncConfirmedBookingInFirestore(confirmedBooking)
        }
    }

    suspend fun rejectBooking(bookingId: Long, dateStr: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.REJECTED)
        val booking = dao.getBookingById(bookingId)
        val dates = dateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
        dates.forEach { singleDate ->
            val existingSlot = dao.getDateSlot(singleDate)
            if (existingSlot != null && existingSlot.status == DateStatus.REQUEST) {
                markDateDeleted(singleDate)
                dao.deleteDateSlot(singleDate)
                deleteSlotFromFirestore(singleDate)
            }
        }
        booking?.let {
            val rejectedBooking = it.copy(status = BookingStatus.REJECTED)
            updateMatchingWebBookingStatusInFirestore(rejectedBooking, BookingStatus.REJECTED)
        }
    }

    suspend fun deleteConfirmedBooking(booking: BookingRequest) {
        val dates = booking.dateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf(booking.dateStr) }
        markBookingDeleted(booking)
        dates.forEach { markDateDeleted(it) }

        dao.deleteBooking(booking.id)
        deleteBookingFromFirestore(booking)

        dates.forEach { singleDate ->
            dao.deleteDateSlot(singleDate)
            dao.deleteConfirmedBookingsByDate(singleDate)
            deleteSlotFromFirestore(singleDate)
            deleteConfirmedBookingsByDateFromFirestore(singleDate)
        }
    }

    suspend fun updateBookingDetails(
        originalDates: List<String>,
        newDates: List<String>,
        customerName: String,
        customerPhone: String,
        workTypeSummary: String,
        dateCrewMap: Map<String, String>,
        totalAmount: Double
    ) {
        val profile = getProfileSync() ?: cachedProfile
        val allAffectedDates = (originalDates + newDates).distinct()
        allAffectedDates.forEach { d ->
            dao.deleteConfirmedBookingsByDate(d)
        }
        val removedDates = originalDates.filterNot { newDates.contains(it) }
        removedDates.forEach { removedDate ->
            markDateDeleted(removedDate)
            dao.deleteDateSlot(removedDate)
            deleteSlotFromFirestore(removedDate)
            deleteConfirmedBookingsByDateFromFirestore(removedDate)
        }
        cleanupConfirmedBookingsForDates(allAffectedDates)

        if (newDates.isNotEmpty()) {
            val perDateWorks = newDates.map { dateStr ->
                dateCrewMap[dateStr]?.takeIf { it.isNotBlank() } ?: workTypeSummary
            }
            val perDateAmounts = allocateAmountsAcrossDates(
                workTypes = perDateWorks,
                totalAmount = totalAmount,
                profile = profile
            )
            newDates.forEachIndexed { idx, dateStr ->
                unmarkDateDeleted(dateStr)
                val dateWorkType = perDateWorks[idx]
                val dateAmount = perDateAmounts[idx]
                val slot = StudioDateSlot(
                    dateStr = dateStr,
                    status = DateStatus.BOOKED,
                    note = "Direct Booking: $dateWorkType",
                    bookedCustomerName = customerName,
                    bookedCustomerPhone = customerPhone,
                    workType = dateWorkType,
                    amount = dateAmount
                )
                dao.insertOrUpdateDateSlot(slot)
                syncSlotToFirestore(slot)

                val booking = BookingRequest(
                    dateStr = dateStr,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    workType = dateWorkType,
                    amount = dateAmount,
                    status = BookingStatus.CONFIRMED
                )
                val id = dao.insertBooking(booking)
                syncBookingToFirestore(booking.copy(id = id))
            }
        }
    }

    suspend fun cancelBookingForDates(dates: List<String>) {
        dates.forEach { dateStr ->
            markDateDeleted(dateStr)
            dao.deleteDateSlot(dateStr)
            dao.deleteConfirmedBookingsByDate(dateStr)
            deleteSlotFromFirestore(dateStr)
            deleteConfirmedBookingsByDateFromFirestore(dateStr)
        }
        cleanupConfirmedBookingsForDates(dates)
    }

    private suspend fun cleanupConfirmedBookingsForDates(targetDates: List<String>) {
        if (targetDates.isEmpty()) return
        val targetSet = targetDates.map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val allConfirmed = dao.getAllBookingsSync().filter { it.status == BookingStatus.CONFIRMED }
        allConfirmed.forEach { booking ->
            val bookingDates = booking.dateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (bookingDates.any { targetSet.contains(it) }) {
                val remaining = bookingDates.filterNot { targetSet.contains(it) }
                if (remaining.isEmpty()) {
                    markBookingDeleted(booking)
                    dao.deleteBooking(booking.id)
                    deleteBookingFromFirestore(booking)
                } else {
                    val updatedBooking = booking.copy(dateStr = remaining.joinToString(", "))
                    dao.updateBooking(updatedBooking)
                    updateOrSyncConfirmedBookingInFirestore(updatedBooking)
                }
            }
        }
    }

    fun allocateAmountsAcrossDates(
        workTypes: List<String>,
        totalAmount: Double,
        profile: StudioProfile?
    ): List<Double> {
        if (workTypes.isEmpty()) return emptyList()
        val estimates = workTypes.map { WorkTypeCatalog.calculateEstimateForDate(it, profile) }
        val sumEstimates = estimates.sum()
        if (totalAmount <= 0.0) {
            return estimates
        }
        if (sumEstimates > 0.0) {
            // Proportional allocation preserves exact per-date crew rates when totalAmount == sumEstimates,
            // and adjusts any rounding remainder on the last date so the sum ALWAYS equals totalAmount.
            val allocated = estimates.map { est ->
                kotlin.math.round((est / sumEstimates) * totalAmount)
            }.toMutableList()
            val diff = kotlin.math.round(totalAmount - allocated.sum())
            if (diff != 0.0 && allocated.isNotEmpty()) {
                allocated[allocated.lastIndex] = (allocated.last() + diff).coerceAtLeast(0.0)
            }
            return allocated
        }
        val evenShare = kotlin.math.round(totalAmount / workTypes.size)
        val allocated = MutableList(workTypes.size) { evenShare }
        val diff = kotlin.math.round(totalAmount - allocated.sum())
        if (diff != 0.0 && allocated.isNotEmpty()) {
            allocated[allocated.lastIndex] = (allocated.last() + diff).coerceAtLeast(0.0)
        }
        return allocated
    }

    fun generateWhatsAppShareMessage(
        profile: StudioProfile?,
        dateSlots: List<StudioDateSlot>,
        customCameraGear: String? = null,
        customNote: String? = null,
        customGreeting: String? = null,
        customFooter: String? = null
    ): String {
        val studioName = profile?.studioName ?: "Studio Expo"
        val photographer = profile?.photographerName ?: "Rajesh Maisuriya"
        val phone = profile?.whatsapp ?: profile?.phone ?: "+91 XXXXX XX520"
        val formattedPhone = if (phone.startsWith("+91")) phone else "+91 $phone"
        val studioSlug = profile?.photographerName?.trim()?.lowercase()?.replace(Regex("[^a-z0-9]+"), "_")?.trim('_')?.ifBlank { "rajesh_maisuriya" } ?: "rajesh_maisuriya"
        val webBookingUrl = "https://srmaisuriy-commits.github.io/Studio-Expo/?studio=$studioSlug"

        val cameraGear = (customCameraGear ?: profile?.cameraGearDetails)?.takeIf { it.isNotBlank() }
            ?: StudioProfile.DEFAULT_CAMERA_GEAR
        val note = (customNote ?: profile?.customWhatsAppNote)?.takeIf { it.isNotBlank() }
            ?: StudioProfile.DEFAULT_WHATSAPP_NOTE
        val greeting = (customGreeting ?: profile?.customHeaderGreeting)?.takeIf { it.isNotBlank() }
            ?: StudioProfile.DEFAULT_HEADER_GREETING
        val footer = (customFooter ?: profile?.customFooterNote)?.takeIf { it.isNotBlank() }
            ?: StudioProfile.DEFAULT_FOOTER_NOTE

        val freeSlots = dateSlots.filter { it.status == DateStatus.FREE }
        val sortedFreeSlots = freeSlots.sortedBy { it.dateStr }

        val datesText = if (sortedFreeSlots.isEmpty()) {
            "• Check live calendar link below for latest availability / नीचे दिए गए लाइव कैलेंडर लिंक पर उपलब्ध तारीखें देखें।"
        } else {
            sortedFreeSlots.joinToString("\n") { slot ->
                val displayDate = DateUtils.formatForDisplay(slot.dateStr)
                val dayOfWeek = DateUtils.getDayOfWeek(slot.dateStr)
                "🟢 $displayDate ($dayOfWeek)"
            }
        }

        return """
📸 *$studioName - Wedding 2025 - 2026*
━━━━━━━━━━━━━━━━━━━━━━━━━━
$greeting

📅 *My Free Dates / मेरी उपलब्ध (FREE) तारीखें:*
$datesText

🎥 *Camera & Gear / कैमरा और उपकरण:*
$cameraGear

📢 *Broadcast Message / मुख्य संदेश:*
$note

━━━━━━━━━━━━━━━━━━━━━━━━━━
$footer
📲 *Contact / WhatsApp:* $formattedPhone
👤 *Lead Artist:* $photographer

🌐 *Live Calendar Booking Link / लाइव कैलेंडर लिंक:*
$webBookingUrl
        """.trimIndent()
    }

    suspend fun resolveStudioId(): String {
        val p = cachedProfile ?: dao.getProfileSync()?.also { cachedProfile = it }
        val clean = p?.photographerName?.trim()?.lowercase()?.replace(Regex("[^a-z0-9]+"), "_")?.trim('_')
        return if (!clean.isNullOrBlank()) clean else "rajesh_maisuriya"
    }

    fun getStudioId(): String {
        val p = cachedProfile
        val clean = p?.photographerName?.trim()?.lowercase()?.replace(Regex("[^a-z0-9]+"), "_")?.trim('_')
        return if (!clean.isNullOrBlank()) clean else "rajesh_maisuriya"
    }

    fun getWebBookingUrl(): String {
        return "https://srmaisuriy-commits.github.io/Studio-Expo/?studio=${getStudioId()}"
    }

    // --- Firebase Firestore Synchronization ---

    fun startRealtimeFirestoreSync(scope: CoroutineScope) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return

            // 1. Listen for real-time calendar slot updates & deletions from Firestore
            db.collection("studio_date_slots")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val currentProfile = dao.getProfileSync()
                        if (currentProfile != null) cachedProfile = currentProfile
                        val studioId = getStudioId()

                        // Handle deletions from Firestore first
                        val activeDatesForStudio = mutableSetOf<String>()
                        for (doc in snapshot.documents) {
                            val docStudio = doc.getString("studioId")
                            if (docStudio != null && docStudio != studioId && !doc.id.startsWith("${studioId}_")) {
                                continue
                            }
                            val dStr = doc.getString("dateStr") ?: doc.id.replace("${studioId}_", "")
                            if (dStr.isNotBlank()) activeDatesForStudio.add(dStr)
                        }

                        for (change in snapshot.documentChanges) {
                            if (change.type == DocumentChange.Type.REMOVED) {
                                val doc = change.document
                                val docStudio = doc.getString("studioId")
                                if (docStudio != null && docStudio != studioId && !doc.id.startsWith("${studioId}_")) {
                                    continue
                                }
                                val removedDate = doc.getString("dateStr") ?: doc.id.replace("${studioId}_", "")
                                if (removedDate.isNotBlank() && !activeDatesForStudio.contains(removedDate)) {
                                    dao.deleteDateSlot(removedDate)
                                }
                            }
                        }

                        for (doc in snapshot.documents) {
                            val docStudio = doc.getString("studioId")
                            // Filter by studioId (or unassigned/default for Rajesh Maisuriya)
                            if (docStudio != null && docStudio != studioId && !doc.id.startsWith("${studioId}_")) {
                                continue
                            }
                            val dateStr = doc.getString("dateStr") ?: doc.id.replace("${studioId}_", "")
                            if (dateStr.isBlank() || isDateDeleteGuarded(dateStr)) {
                                continue
                            }
                            val statusStr = doc.getString("status") ?: DateStatus.FREE.name
                            val status = try { DateStatus.valueOf(statusStr) } catch (_: Exception) { DateStatus.FREE }
                            val slotTypeStr = doc.getString("slotType") ?: SlotType.FULL_DAY.name
                            val slotType = try { SlotType.valueOf(slotTypeStr) } catch (_: Exception) { SlotType.FULL_DAY }
                            val note = doc.getString("note") ?: ""
                            val name = doc.getString("bookedCustomerName") ?: ""
                            val phoneRaw = doc.getString("bookedCustomerPhone") ?: ""
                            val phone = if (phoneRaw == "9898012345" || phoneRaw == "9876543210") "+91 XXXXX XX520" else phoneRaw
                            val workType = doc.getString("workType") ?: ""
                            val amount = doc.getDouble("amount") ?: 0.0

                            val slot = StudioDateSlot(
                                dateStr = dateStr,
                                status = status,
                                note = note,
                                slotType = slotType,
                                bookedCustomerName = name,
                                bookedCustomerPhone = phone,
                                workType = workType,
                                amount = amount
                            )
                            dao.insertOrUpdateDateSlot(slot)
                        }
                    }
                }

            // 2. Listen for incoming customer booking requests & deletions from Firestore
            db.collection("booking_requests")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val currentProfile = dao.getProfileSync()
                        if (currentProfile != null) cachedProfile = currentProfile
                        val studioId = getStudioId()

                        // Handle deletions from Firestore
                        for (change in snapshot.documentChanges) {
                            if (change.type == DocumentChange.Type.REMOVED) {
                                val doc = change.document
                                val docStudio = doc.getString("studioId")
                                if (docStudio != null && docStudio != studioId && !doc.id.startsWith("${studioId}_")) {
                                    continue
                                }
                                val rDate = doc.getString("dateStr") ?: ""
                                val rPhoneRaw = doc.getString("customerPhone") ?: ""
                                val rPhone = if (rPhoneRaw == "9898012345" || rPhoneRaw == "9876543210") "+91 XXXXX XX520" else rPhoneRaw
                                val stillExistsInSnapshot = snapshot.documents.any { rem ->
                                    val rs = rem.getString("studioId")
                                    (rs == null || rs == studioId || rem.id.startsWith("${studioId}_")) &&
                                        rem.getString("dateStr") == rDate &&
                                        (rem.getString("customerPhone") ?: "") == rPhoneRaw
                                }
                                if (!stillExistsInSnapshot && rDate.isNotBlank()) {
                                    dao.getBookingByPhoneAndDate(rPhone, rDate)?.let {
                                        dao.deleteBooking(it.id)
                                    }
                                }
                            }
                        }

                        for (doc in snapshot.documents) {
                            val docStudio = doc.getString("studioId")
                            // Strictly isolate: Only receive bookings submitted for THIS studio owner!
                            if (docStudio != null && docStudio != studioId && !doc.id.startsWith("${studioId}_")) {
                                continue
                            }
                            val dateStr = doc.getString("dateStr") ?: ""
                            val customerName = doc.getString("customerName") ?: ""
                            val customerPhoneRaw = doc.getString("customerPhone") ?: ""
                            val customerPhone = if (customerPhoneRaw == "9898012345" || customerPhoneRaw == "9876543210") "+91 XXXXX XX520" else customerPhoneRaw
                            if (dateStr.isBlank() || customerName.isBlank()) continue

                            val workType = doc.getString("workType") ?: "Traditional Photographer"
                            val amount = doc.getDouble("amount") ?: 0.0
                            val location = doc.getString("location") ?: ""
                            val notes = doc.getString("notes") ?: ""
                            val statusStr = doc.getString("status") ?: BookingStatus.PENDING.name
                            val status = try { BookingStatus.valueOf(statusStr) } catch (_: Exception) { BookingStatus.PENDING }
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                            if (isBookingDeleteGuarded(customerPhone, dateStr, createdAt)) {
                                continue
                            }

                            val existing = dao.getBookingByPhoneAndDate(customerPhone, dateStr)
                            if (existing == null) {
                                val booking = BookingRequest(
                                    dateStr = dateStr,
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    workType = workType,
                                    amount = amount,
                                    location = location,
                                    notes = notes,
                                    status = status,
                                    createdAt = createdAt
                                )
                                dao.insertBooking(booking)

                                // Also ensure calendar shows yellow REQUEST for pending web requests
                                if (status == BookingStatus.PENDING) {
                                    dateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { singleDate ->
                                        if (!isDateDeleteGuarded(singleDate)) {
                                            val existingSlot = dao.getDateSlot(singleDate)
                                            if (existingSlot == null || existingSlot.status == DateStatus.FREE) {
                                                dao.insertOrUpdateDateSlot(
                                                    StudioDateSlot(
                                                        dateStr = singleDate,
                                                        status = DateStatus.REQUEST,
                                                        note = "Requested by $customerName"
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (existing.status != status && existing.status == BookingStatus.PENDING) {
                                dao.updateBookingStatus(existing.id, status)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore listener setup error: ${e.message}")
        }
    }

    private suspend fun syncSlotToFirestore(slot: StudioDateSlot) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            val data = hashMapOf(
                "studioId" to studioId,
                "dateStr" to slot.dateStr,
                "status" to slot.status.name,
                "slotType" to slot.slotType.name,
                "note" to slot.note,
                "bookedCustomerName" to slot.bookedCustomerName,
                "bookedCustomerPhone" to slot.bookedCustomerPhone,
                "workType" to slot.workType,
                "amount" to slot.amount,
                "updatedAt" to System.currentTimeMillis()
            )
            // Write single canonical doc per studio+date and remove any legacy unprefixed doc atomically
            val batch = db.batch()
            batch.set(db.collection("studio_date_slots").document("${studioId}_${slot.dateStr}"), data)
            batch.delete(db.collection("studio_date_slots").document(slot.dateStr))
            batch.commit()
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore slot sync error: ${e.message}")
        }
    }

    private suspend fun deleteSlotFromFirestore(dateStr: String) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            val batch = db.batch()
            batch.delete(db.collection("studio_date_slots").document("${studioId}_${dateStr}"))
            batch.delete(db.collection("studio_date_slots").document(dateStr))
            batch.commit()

            db.collection("studio_date_slots")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("dateStr", dateStr)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val extraBatch = db.batch()
                        for (doc in snapshot.documents) {
                            extraBatch.delete(doc.reference)
                        }
                        extraBatch.commit()
                    }
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore slot delete error: ${e.message}")
        }
    }

    private suspend fun syncProfileToFirestore(profile: StudioProfile) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            val data = hashMapOf(
                "studioId" to studioId,
                "studioName" to profile.studioName,
                "photographerName" to profile.photographerName,
                "phone" to profile.phone,
                "whatsapp" to profile.whatsapp,
                "city" to profile.city,
                "bio" to profile.bio,
                "instagramHandle" to profile.instagramHandle,
                "cameraGearDetails" to profile.cameraGearDetails,
                "customWhatsAppNote" to profile.customWhatsAppNote,
                "customHeaderGreeting" to profile.customHeaderGreeting,
                "customFooterNote" to profile.customFooterNote,
                "perDayRate" to profile.perDayRate,
                "rateTraditionalPhoto" to profile.rateTraditionalPhoto,
                "rateTraditionalVideo" to profile.rateTraditionalVideo,
                "rateCandidPhoto" to profile.rateCandidPhoto,
                "rateSemiCandidPhoto" to profile.rateSemiCandidPhoto,
                "rateCinematographer" to profile.rateCinematographer,
                "rateDronePilot" to profile.rateDronePilot,
                "rateSupportingCrew" to profile.rateSupportingCrew,
                "showRateTraditionalPhoto" to profile.showRateTraditionalPhoto,
                "showRateTraditionalVideo" to profile.showRateTraditionalVideo,
                "showRateCandidPhoto" to profile.showRateCandidPhoto,
                "showRateSemiCandidPhoto" to profile.showRateSemiCandidPhoto,
                "showRateCinematographer" to profile.showRateCinematographer,
                "showRateDronePilot" to profile.showRateDronePilot,
                "showRateSupportingCrew" to profile.showRateSupportingCrew,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("studio_profile").document(studioId).set(data)
            db.collection("studio_profile").document("default").set(data)
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore profile sync error: ${e.message}")
        }
    }

    private suspend fun syncBookingToFirestore(booking: BookingRequest) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            val data = hashMapOf(
                "id" to booking.id,
                "studioId" to studioId,
                "dateStr" to booking.dateStr,
                "customerName" to booking.customerName,
                "customerPhone" to booking.customerPhone,
                "workType" to booking.workType,
                "amount" to booking.amount,
                "location" to booking.location,
                "notes" to booking.notes,
                "status" to booking.status.name,
                "createdAt" to booking.createdAt
            )
            val docId = if (booking.id > 0) "${studioId}_${booking.id}" else "${studioId}_${booking.dateStr}_${booking.customerPhone}"
            db.collection("booking_requests").document(docId).set(data)
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore booking sync error: ${e.message}")
        }
    }

    private suspend fun updateOrSyncConfirmedBookingInFirestore(booking: BookingRequest) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            db.collection("booking_requests")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("createdAt", booking.createdAt)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val batch = db.batch()
                        for (doc in snapshot.documents) {
                            batch.update(
                                doc.reference,
                                mapOf(
                                    "status" to booking.status.name,
                                    "dateStr" to booking.dateStr
                                )
                            )
                        }
                        batch.commit()
                    } else {
                        val data = hashMapOf(
                            "id" to booking.id,
                            "studioId" to studioId,
                            "dateStr" to booking.dateStr,
                            "customerName" to booking.customerName,
                            "customerPhone" to booking.customerPhone,
                            "workType" to booking.workType,
                            "amount" to booking.amount,
                            "location" to booking.location,
                            "notes" to booking.notes,
                            "status" to booking.status.name,
                            "createdAt" to booking.createdAt
                        )
                        val docId = if (booking.id > 0) "${studioId}_${booking.id}" else "${studioId}_${booking.dateStr}_${booking.customerPhone}"
                        db.collection("booking_requests").document(docId).set(data)
                    }
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore confirmed booking update error: ${e.message}")
        }
    }

    private suspend fun updateMatchingWebBookingStatusInFirestore(booking: BookingRequest, newStatus: BookingStatus) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            db.collection("booking_requests")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("createdAt", booking.createdAt)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val batch = db.batch()
                        for (doc in snapshot.documents) {
                            batch.update(doc.reference, "status", newStatus.name)
                        }
                        batch.commit()
                    } else {
                        val data = hashMapOf(
                            "id" to booking.id,
                            "studioId" to studioId,
                            "dateStr" to booking.dateStr,
                            "customerName" to booking.customerName,
                            "customerPhone" to booking.customerPhone,
                            "workType" to booking.workType,
                            "amount" to booking.amount,
                            "location" to booking.location,
                            "notes" to booking.notes,
                            "status" to newStatus.name,
                            "createdAt" to booking.createdAt
                        )
                        val docId = if (booking.id > 0) "${studioId}_${booking.id}" else "${studioId}_${booking.dateStr}_${booking.customerPhone}"
                        db.collection("booking_requests").document(docId).set(data)
                    }
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore web booking status update error: ${e.message}")
        }
    }

    private suspend fun deleteBookingFromFirestore(booking: BookingRequest) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            val directBatch = db.batch()
            if (booking.id > 0) {
                directBatch.delete(db.collection("booking_requests").document("${studioId}_${booking.id}"))
            }
            directBatch.delete(db.collection("booking_requests").document("${studioId}_${booking.dateStr}_${booking.customerPhone}"))
            directBatch.commit()

            db.collection("booking_requests")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("createdAt", booking.createdAt)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val b = db.batch()
                        for (doc in snapshot.documents) {
                            b.delete(doc.reference)
                        }
                        b.commit()
                    }
                }

            db.collection("booking_requests")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("dateStr", booking.dateStr)
                .whereEqualTo("customerName", booking.customerName)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val b = db.batch()
                        for (doc in snapshot.documents) {
                            b.delete(doc.reference)
                        }
                        b.commit()
                    }
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore booking delete error: ${e.message}")
        }
    }

    private suspend fun deleteConfirmedBookingsByDateFromFirestore(dateStr: String) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val studioId = resolveStudioId()
            db.collection("booking_requests")
                .whereEqualTo("studioId", studioId)
                .whereEqualTo("status", BookingStatus.CONFIRMED.name)
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    var hasChanges = false
                    for (doc in snapshot.documents) {
                        val docDateStr = doc.getString("dateStr") ?: ""
                        val datesList = docDateStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (datesList.contains(dateStr)) {
                            val remaining = datesList.filterNot { it == dateStr }
                            if (remaining.isEmpty()) {
                                batch.delete(doc.reference)
                            } else {
                                batch.update(doc.reference, "dateStr", remaining.joinToString(", "))
                            }
                            hasChanges = true
                        }
                    }
                    if (hasChanges) batch.commit()
                }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore confirmed booking delete by date error: ${e.message}")
        }
    }

    suspend fun sanitizeOldDemoNumbers() {
        try {
            val allSlots = dao.getAllDateSlotsSync()
            allSlots.forEach { slot ->
                if (slot.bookedCustomerPhone == "9898012345" || slot.bookedCustomerPhone == "9876543210") {
                    dao.insertOrUpdateDateSlot(slot.copy(bookedCustomerPhone = "+91 XXXXX XX520"))
                }
            }
            val allBookings = dao.getAllBookingsSync()
            allBookings.forEach { booking ->
                if (booking.customerPhone == "9898012345" || booking.customerPhone == "9876543210") {
                    dao.insertBooking(booking.copy(customerPhone = "+91 XXXXX XX520"))
                }
            }
        } catch (e: Exception) {
            Log.e("StudioRepository", "Error sanitizing demo numbers: ${e.message}")
        }
    }
}
