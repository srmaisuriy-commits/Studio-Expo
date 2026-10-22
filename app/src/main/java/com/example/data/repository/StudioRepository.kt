package com.example.data.repository

import com.example.data.local.DateUtils
import com.example.data.local.StudioDao
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.SlotType
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudioRepository(private val dao: StudioDao) {

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
    }

    suspend fun deleteDateSlot(dateStr: String) {
        dao.deleteDateSlot(dateStr)
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
        return dao.insertBooking(booking)
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
    }

    suspend fun rejectBooking(bookingId: Long, dateStr: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.REJECTED)
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
        val greeting = customGreeting ?: profile?.customHeaderGreeting ?: "Hello! Here are our available (FREE) dates for upcoming wedding & events / नमस्ते! आगामी शादी और इवेंट्स के लिए हमारी उपलब्ध (FREE) तारीखें:"
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

        val shortDatesSummary = if (sortedFreeSlots.isEmpty()) {
            "None"
        } else {
            sortedFreeSlots.joinToString(" / ") { slot ->
                slot.dateStr.split("-").lastOrNull() ?: slot.dateStr
            }
        }

        val gearBlock = if (cameraGear.isNotBlank()) {
            "\n🎥 *Camera & Gear Details:*\n$cameraGear\n"
        } else ""

        val noteBlock = if (note.isNotBlank()) {
            "\n✨ *Note / विशेष नोट:*\n$note\n"
        } else ""

        val greetingBlock = if (greeting.isNotBlank()) {
            "\n$greeting\n"
        } else ""

        return """
📸 *${studioName.uppercase()}* 
✨ *Photographer: $photographer*
📍 ${profile?.city ?: "India"}
$gearBlock$noteBlock$greetingBlock
📅 *FREE DATES / उपलब्ध तारीखें:*
$datesText

🗓️ *Quick Summary:* $shortDatesSummary

🔗 *Online Booking Link (ऑनलाइन बुकिंग लिंक):*
https://studioexpo.app/book/${phone.ifBlank { "studio" }}

🌐 *Official Website Portfolio (वेबसाइट):*
https://srmaisuriy-commits.github.io/Studio-Expo/

📞 *Contact / WhatsApp (संपर्क):* +91 $phone

_${footer}_
        """.trimIndent()
    }
}
