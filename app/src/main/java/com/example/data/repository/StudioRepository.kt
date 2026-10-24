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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

    suspend fun getProfileSync(): StudioProfile? = dao.getProfileSync()

    suspend fun updateProfile(profile: StudioProfile) {
        dao.updateProfile(profile)
        syncProfileToFirestore(profile)
    }

    suspend fun addFreeDate(
        dateStr: String,
        note: String,
        slotType: SlotType = SlotType.FULL_DAY
    ) {
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
        val existing = dao.getDateSlot(dateStr)
        val slot = existing?.copy(status = status, note = note)
            ?: StudioDateSlot(dateStr = dateStr, status = status, note = note)
        dao.insertOrUpdateDateSlot(slot)
        syncSlotToFirestore(slot)
    }

    suspend fun deleteDateSlot(dateStr: String) {
        dao.deleteDateSlot(dateStr)
        deleteSlotFromFirestore(dateStr)
    }

    suspend fun createDirectBooking(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ) {
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

        // Record confirmed booking request
        val booking = BookingRequest(
            dateStr = dateStr,
            customerName = customerName,
            customerPhone = customerPhone,
            workType = workType,
            amount = amount,
            status = BookingStatus.CONFIRMED
        )
        dao.insertBooking(booking)
        syncBookingToFirestore(booking)
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
        val slot = StudioDateSlot(
            dateStr = dateStr,
            status = DateStatus.BOOKED,
            note = booking?.workType ?: "Booked",
            bookedCustomerName = booking?.customerName ?: "",
            bookedCustomerPhone = booking?.customerPhone ?: "",
            workType = booking?.workType ?: "",
            amount = booking?.requestedAmount ?: 0.0
        )
        dao.insertOrUpdateDateSlot(slot)
        syncSlotToFirestore(slot)
        booking?.let { syncBookingToFirestore(it.copy(status = BookingStatus.CONFIRMED)) }
    }

    suspend fun rejectBooking(bookingId: Long, dateStr: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.REJECTED)
        val booking = dao.getBookingById(bookingId)
        booking?.let { syncBookingToFirestore(it.copy(status = BookingStatus.REJECTED)) }
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
        val removedDates = originalDates.filterNot { newDates.contains(it) }
        removedDates.forEach { removedDate ->
            dao.deleteDateSlot(removedDate)
            dao.deleteBookingByDate(removedDate)
            deleteSlotFromFirestore(removedDate)
        }

        if (newDates.isNotEmpty()) {
            val perDateAmount = totalAmount / newDates.size
            newDates.forEach { dateStr ->
                val dateWorkType = dateCrewMap[dateStr]?.takeIf { it.isNotBlank() } ?: workTypeSummary
                val slot = StudioDateSlot(
                    dateStr = dateStr,
                    status = DateStatus.BOOKED,
                    note = "Direct Booking: $dateWorkType",
                    bookedCustomerName = customerName,
                    bookedCustomerPhone = customerPhone,
                    workType = dateWorkType,
                    amount = perDateAmount
                )
                dao.insertOrUpdateDateSlot(slot)
                syncSlotToFirestore(slot)

                val booking = BookingRequest(
                    dateStr = dateStr,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    workType = dateWorkType,
                    amount = perDateAmount,
                    status = BookingStatus.CONFIRMED
                )
                val id = dao.insertBooking(booking)
                syncBookingToFirestore(booking.copy(id = id))
            }
        }
    }

    suspend fun cancelBookingForDates(dates: List<String>) {
        dates.forEach { dateStr ->
            dao.deleteDateSlot(dateStr)
            dao.deleteBookingByDate(dateStr)
            deleteSlotFromFirestore(dateStr)
        }
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
        val phone = profile?.whatsapp ?: profile?.phone ?: "9876543210"

        val cameraGear = customCameraGear ?: profile?.cameraGearDetails ?: "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights"
        val note = customNote ?: profile?.customWhatsAppNote ?: "Latest 4K Cinematic Camera & Drone Coverage Available / नवीनतम 4K सिनेमैटिक कैमरा और ड्रोन कवरेज उपलब्ध है।"
        val greeting = customGreeting ?: profile?.customHeaderGreeting ?: "Hello! Here are our available (FREE) dates for upcoming shoots & events / नमस्ते! आगामी शूट और इवेंट्स के लिए हमारी उपलब्ध (FREE) तारीखें:"
        val footer = customFooter ?: profile?.customFooterNote ?: "Reserve your special date today! Contact us on WhatsApp for bookings & details / अपनी तारीख आज ही बुक करें!"

        val freeSlots = dateSlots.filter { it.status == DateStatus.FREE }
        val sortedFreeSlots = freeSlots.sortedBy { it.dateStr }

        val datesText = if (sortedFreeSlots.isEmpty()) {
          "• Currently no dates available. Contact us for custom bookings / वर्तमान में कोई तारीख उपलब्ध नहीं है।"
        } else {
            sortedFreeSlots.joinToString("\n") { slot ->
                val displayDate = DateUtils.formatForDisplay(slot.dateStr)
                val dayOfWeek = DateUtils.getDayOfWeek(slot.dateStr)
                "🟢 $displayDate ($dayOfWeek)"
            }
        }

        return """
📸 *$studioName - Live Availability Calendar*
━━━━━━━━━━━━━━━━━━━━━━━━━━
$greeting

$datesText

🎥 *Gear / Cameras:*
$cameraGear

💡 *Special Note:*
$note

━━━━━━━━━━━━━━━━━━━━━━━━━━
$footer
📲 *Contact / WhatsApp:* +91 $phone
👤 *Lead Artist:* $photographer
🌐 *Live Web Booking:* https://studioexpo.app/book
        """.trimIndent()
    }

    // --- Firebase Firestore Synchronization ---

    fun startRealtimeFirestoreSync(scope: CoroutineScope) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            db.collection("studio_date_slots")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        for (doc in snapshot.documents) {
                            val dateStr = doc.getString("dateStr") ?: doc.id
                            val statusStr = doc.getString("status") ?: DateStatus.FREE.name
                            val status = try { DateStatus.valueOf(statusStr) } catch (_: Exception) { DateStatus.FREE }
                            val slotTypeStr = doc.getString("slotType") ?: SlotType.FULL_DAY.name
                            val slotType = try { SlotType.valueOf(slotTypeStr) } catch (_: Exception) { SlotType.FULL_DAY }
                            val note = doc.getString("note") ?: ""
                            val name = doc.getString("bookedCustomerName") ?: ""
                            val phone = doc.getString("bookedCustomerPhone") ?: ""
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
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore listener setup error: ${e.message}")
        }
    }

    private fun syncSlotToFirestore(slot: StudioDateSlot) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val data = hashMapOf(
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
            db.collection("studio_date_slots").document(slot.dateStr).set(data)
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore slot sync error: ${e.message}")
        }
    }

    private fun deleteSlotFromFirestore(dateStr: String) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            db.collection("studio_date_slots").document(dateStr).delete()
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore slot delete error: ${e.message}")
        }
    }

    private fun syncProfileToFirestore(profile: StudioProfile) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val data = hashMapOf(
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
                "perDayRate" to profile.perDayRate
            )
            db.collection("studio_profile").document("default").set(data)
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore profile sync error: ${e.message}")
        }
    }

    private fun syncBookingToFirestore(booking: BookingRequest) {
        val ctx = context ?: return
        try {
            val db = FirebaseConfig.getFirestore(ctx) ?: return
            val data = hashMapOf(
                "id" to booking.id,
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
            val docId = if (booking.id > 0) booking.id.toString() else "${booking.dateStr}_${booking.customerPhone}"
            db.collection("booking_requests").document(docId).set(data)
        } catch (e: Exception) {
            Log.e("StudioRepository", "Firestore booking sync error: ${e.message}")
        }
    }
}
