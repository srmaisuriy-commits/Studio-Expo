package com.example

import com.example.data.local.DateUtils
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.repository.StudioRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioLogicTest {

    @Test
    fun testDateUtilsFormatting() {
        val today = DateUtils.todayIso()
        assertNotNull(today)
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val display = DateUtils.formatForDisplay("2026-04-25")
        assertTrue(display.contains("Apr 2026"))

        val fullDisplay = DateUtils.formatFullForDisplay("2026-04-25")
        assertTrue(fullDisplay.contains("April 2026"))
    }

    @Test
    fun testMonthGridGeneration() {
        // April (month 3) 2026 has 30 days
        val days = DateUtils.generateMonthDays(2026, 3)
        // Grid should always be multiple of 7
        assertEquals(0, days.size % 7)
        assertTrue(days.size in listOf(28, 35, 42))

        val currentMonthDays = days.filter { it.isCurrentMonth }
        assertEquals(30, currentMonthDays.size)
    }

    @Test
    fun testWhatsAppShareMessageGenerationWithAutoSync() {
        val repo = StudioRepository(MockStudioDao())
        val profile = StudioProfile(
            studioName = "Studio Expo",
            photographerName = "Rajesh Maisuriya",
            whatsapp = "9876543210"
        )

        // Scenario 1: 22, 23, 25 are FREE, 24 is BOOKED
        val slots1 = listOf(
            StudioDateSlot(dateStr = "2026-04-22", status = DateStatus.FREE.name),
            StudioDateSlot(dateStr = "2026-04-23", status = DateStatus.FREE.name),
            StudioDateSlot(dateStr = "2026-04-24", status = DateStatus.BOOKED.name),
            StudioDateSlot(dateStr = "2026-04-25", status = DateStatus.FREE.name)
        )

        val message1 = repo.generateWhatsAppShareMessage(profile, slots1)
        assertTrue(message1.contains("STUDIO EXPO"))
        assertTrue(message1.contains("22"))
        assertTrue(message1.contains("23"))
        assertTrue(message1.contains("25"))
        assertFalse(message1.contains("24 Apr")) // 24 is BOOKED, must be excluded!

        // Scenario 2: Owner taps 25 and sets to BOOKED -> 25 automatically removed!
        val slots2 = listOf(
            StudioDateSlot(dateStr = "2026-04-22", status = DateStatus.FREE.name),
            StudioDateSlot(dateStr = "2026-04-23", status = DateStatus.FREE.name),
            StudioDateSlot(dateStr = "2026-04-24", status = DateStatus.BOOKED.name),
            StudioDateSlot(dateStr = "2026-04-25", status = DateStatus.BOOKED.name) // turned to BOOKED
        )

        val message2 = repo.generateWhatsAppShareMessage(profile, slots2)
        assertTrue(message2.contains("22"))
        assertTrue(message2.contains("23"))
        assertFalse(message2.contains("25 Apr")) // 25 is now BOOKED, automatically removed!

        // Scenario 3: Custom camera gear & equipment details in message
        val message3 = repo.generateWhatsAppShareMessage(
            profile = profile,
            dateSlots = slots1,
            customCameraGear = "Sony FX3 + DJI Mavic 3 Drone",
            customNote = "Special Wedding Discount!"
        )
        assertTrue(message3.contains("Camera & Gear Details:"))
        assertTrue(message3.contains("Sony FX3 + DJI Mavic 3 Drone"))
        assertTrue(message3.contains("Special Wedding Discount!"))
    }
}

// Minimal mock DAO for logic test
private class MockStudioDao : com.example.data.local.StudioDao {
    override fun getProfileFlow() = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun getProfileSync() = null
    override suspend fun insertOrUpdateProfile(profile: StudioProfile) {}
    override fun getAllDateSlotsFlow() = kotlinx.coroutines.flow.flowOf(emptyList<StudioDateSlot>())
    override fun getDateSlotFlow(dateStr: String) = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun getDateSlotSync(dateStr: String) = null
    override suspend fun insertDateSlot(slot: StudioDateSlot) = 1L
    override suspend fun insertDateSlots(slots: List<StudioDateSlot>) {}
    override suspend fun deleteDateSlotById(id: Long) {}
    override suspend fun deleteDateSlotByDate(dateStr: String) {}
    override suspend fun updateDateSlotStatus(dateStr: String, status: String, bookingId: Long?, updatedAt: Long) {}
    override fun getAllBookingsFlow() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.model.BookingRequest>())
    override fun getPendingBookingsFlow() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.model.BookingRequest>())
    override fun getBookingsForDateFlow(dateStr: String) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.model.BookingRequest>())
    override fun getBookingByIdFlow(id: Long) = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun getBookingByIdSync(id: Long) = null
    override fun getCurrentCustomerBookingsFlow() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.model.BookingRequest>())
    override suspend fun insertBooking(booking: com.example.data.model.BookingRequest) = 1L
    override suspend fun updateBookingStatus(id: Long, status: String) {}
    override suspend fun rejectOtherPendingBookingsForDate(dateStr: String, exceptBookingId: Long) {}
    override suspend fun setDateStatusDirectly(dateStr: String, status: String) {}
    override suspend fun createDirectBookingTransaction(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ) = 1L
    override suspend fun confirmBookingTransaction(bookingId: Long, dateStr: String) {}
    override suspend fun rejectBookingTransaction(bookingId: Long, dateStr: String) {}
}
