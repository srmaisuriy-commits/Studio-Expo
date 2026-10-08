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

class StudioRepository(private val studioDao: StudioDao) {

    val profileFlow: Flow<StudioProfile?> = studioDao.getProfileFlow()
    val allDateSlotsFlow: Flow<List<StudioDateSlot>> = studioDao.getAllDateSlotsFlow()
    val allBookingsFlow: Flow<List<BookingRequest>> = studioDao.getAllBookingsFlow()
    val pendingBookingsFlow: Flow<List<BookingRequest>> = studioDao.getPendingBookingsFlow()
    val currentCustomerBookingsFlow: Flow<List<BookingRequest>> = studioDao.getCurrentCustomerBookingsFlow()

    suspend fun updateProfile(profile: StudioProfile) {
        studioDao.insertOrUpdateProfile(profile)
    }

    suspend fun getProfileSync(): StudioProfile? {
        return studioDao.getProfileSync()
    }

    // Direct Calendar Date Control: 🟢 SET FREE DATE or 🔴 SET BOOKED DATE
    suspend fun setDirectDateStatus(dateStr: String, status: DateStatus) {
        studioDao.setDateStatusDirectly(dateStr, status.name)
    }

    // Direct Booking on Calendar Date: FREE -> BOOKED with Customer details
    suspend fun createDirectBooking(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ): Long {
        return studioDao.createDirectBookingTransaction(
            dateStr = dateStr,
            customerName = customerName,
            customerPhone = customerPhone,
            workType = workType,
            amount = amount
        )
    }

    suspend fun addOrUpdateFreeDate(
        dateStr: String,
        slotType: String = SlotType.FULL_DAY.name,
        basePrice: Double = 25000.0,
        note: String = ""
    ): Long {
        val slot = StudioDateSlot(
            dateStr = dateStr,
            status = DateStatus.FREE.name,
            slotType = slotType,
            basePrice = basePrice,
            note = note,
            bookedBookingId = null
        )
        return studioDao.insertDateSlot(slot)
    }

    suspend fun deleteDateSlot(dateStr: String) {
        studioDao.deleteDateSlotByDate(dateStr)
    }

    suspend fun setDateSlotStatus(dateStr: String, status: DateStatus) {
        studioDao.updateDateSlotStatus(dateStr, status.name, null)
    }

    suspend fun submitCustomerBooking(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double,
        location: String,
        notes: String
    ): Long {
        val booking = BookingRequest(
            dateStr = dateStr,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            workType = workType,
            amount = amount,
            location = location.trim(),
            notes = notes.trim(),
            status = BookingStatus.PENDING.name,
            isSubmittedByCurrentCustomer = true
        )
        return studioDao.insertBooking(booking)
    }

    suspend fun confirmBooking(bookingId: Long, dateStr: String) {
        studioDao.confirmBookingTransaction(bookingId, dateStr)
    }

    suspend fun rejectBooking(bookingId: Long, dateStr: String) {
        studioDao.rejectBookingTransaction(bookingId, dateStr)
    }

    /**
     * Dynamically reads all current FREE dates directly from the calendar.
     * When any date is set to BOOKED, it is instantly and automatically excluded!
     */
    fun generateWhatsAppShareMessage(
        profile: StudioProfile?,
        dateSlots: List<StudioDateSlot>,
        customCameraGear: String? = null,
        customNote: String? = null,
        customGreeting: String? = null,
        customFooter: String? = null
    ): String {
        val studioName = profile?.studioName ?: "Studio Expo"
        val photographer = profile?.photographerName ?: "Photographer"
        val phone = profile?.whatsapp ?: profile?.phone ?: ""
        val cameraGear = customCameraGear ?: profile?.cameraGearDetails ?: "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Setup"
        val note = customNote ?: profile?.customWhatsAppNote ?: "લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન કવરેજ ઉપલબ્ધ છે."
        val greeting = customGreeting ?: profile?.customHeaderGreeting ?: "નમસ્તે! આગામી ઇવેન્ટ્સ અને લગ્ન સીઝન માટે અમારી ઉપલબ્ધ (FREE) તારીખો નીચે મુજબ છે:"
        val footer = customFooter ?: profile?.customFooterNote ?: "તમારી સ્પેશ્યલ ડેટ સમયસર રિઝર્વ કરાવો! વધુ વિગત માટે સંપર્ક કરો."

        val sortedFreeSlots = dateSlots
            .filter { it.status == DateStatus.FREE.name }
            .sortedBy { it.dateStr }

        val datesText = if (sortedFreeSlots.isEmpty()) {
            "• હાલમાં કોઈ તારીખ ઉપલબ્ધ નથી. નવી તારીખો માટે સંપર્ક કરો."
        } else {
            sortedFreeSlots.joinToString("\n") { slot ->
                val displayDate = DateUtils.formatForDisplay(slot.dateStr)
                val dayOfWeek = DateUtils.getDayOfWeek(slot.dateStr)
                "🟢 $displayDate ($dayOfWeek)"
            }
        }

        // Short summary line like: "22 / 23 / 25"
        val shortDatesSummary = if (sortedFreeSlots.isEmpty()) {
            "None"
        } else {
            sortedFreeSlots.joinToString(" / ") { slot ->
                val day = slot.dateStr.split("-").lastOrNull() ?: slot.dateStr
                day
            }
        }

        val gearBlock = if (cameraGear.isNotBlank()) {
            "\n🎥 *Camera & Gear Details:*\n$cameraGear\n"
        } else ""

        val noteBlock = if (note.isNotBlank()) {
            "\n✨ *Note / સુવિધા:*\n$note\n"
        } else ""

        val greetingBlock = if (greeting.isNotBlank()) {
            "\n$greeting\n"
        } else ""

        return """
📸 *${studioName.uppercase()}* 
✨ *Photographer: $photographer*
📍 ${profile?.city ?: "Gujarat"}
$gearBlock$noteBlock$greetingBlock
📅 *FREE DATES / ઉપલબ્ધ તારીખો:*
$datesText

🗓️ *Quick Summary:* $shortDatesSummary

🔗 *ઓનલાઇન તારીખ બુક કરવા લિંક (Booking Link):*
https://studioexpo.app/book/${phone.ifBlank { "studio" }}

📞 *સંપર્ક / WhatsApp:* +91 $phone

_${footer}_
        """.trimIndent()
    }
}
