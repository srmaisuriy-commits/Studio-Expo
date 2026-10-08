package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {

    // --- Profile Queries ---
    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<StudioProfile?>

    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): StudioProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudioProfile)

    // --- Date Slot Queries ---
    @Query("SELECT * FROM studio_date_slots ORDER BY dateStr ASC")
    fun getAllDateSlotsFlow(): Flow<List<StudioDateSlot>>

    @Query("SELECT * FROM studio_date_slots WHERE dateStr = :dateStr LIMIT 1")
    fun getDateSlotFlow(dateStr: String): Flow<StudioDateSlot?>

    @Query("SELECT * FROM studio_date_slots WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDateSlotSync(dateStr: String): StudioDateSlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateSlot(slot: StudioDateSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateSlots(slots: List<StudioDateSlot>)

    @Query("DELETE FROM studio_date_slots WHERE id = :id")
    suspend fun deleteDateSlotById(id: Long)

    @Query("DELETE FROM studio_date_slots WHERE dateStr = :dateStr")
    suspend fun deleteDateSlotByDate(dateStr: String)

    @Query("UPDATE studio_date_slots SET status = :status, bookedBookingId = :bookingId, updatedAt = :updatedAt WHERE dateStr = :dateStr")
    suspend fun updateDateSlotStatus(dateStr: String, status: String, bookingId: Long?, updatedAt: Long = System.currentTimeMillis())

    // --- Booking Requests Queries ---
    @Query("SELECT * FROM booking_requests ORDER BY createdAt DESC")
    fun getAllBookingsFlow(): Flow<List<BookingRequest>>

    @Query("SELECT * FROM booking_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingBookingsFlow(): Flow<List<BookingRequest>>

    @Query("SELECT * FROM booking_requests WHERE dateStr = :dateStr ORDER BY createdAt DESC")
    fun getBookingsForDateFlow(dateStr: String): Flow<List<BookingRequest>>

    @Query("SELECT * FROM booking_requests WHERE id = :id LIMIT 1")
    fun getBookingByIdFlow(id: Long): Flow<BookingRequest?>

    @Query("SELECT * FROM booking_requests WHERE id = :id LIMIT 1")
    suspend fun getBookingByIdSync(id: Long): BookingRequest?

    @Query("SELECT * FROM booking_requests WHERE isSubmittedByCurrentCustomer = 1 ORDER BY createdAt DESC")
    fun getCurrentCustomerBookingsFlow(): Flow<List<BookingRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingRequest): Long

    @Query("UPDATE booking_requests SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: String)

    // --- Owner Confirmation Transaction ---
    @Transaction
    suspend fun confirmBookingTransaction(bookingId: Long, dateStr: String) {
        // 1. Mark this booking as CONFIRMED
        updateBookingStatus(bookingId, BookingStatus.CONFIRMED.name)

        // 2. Reject any other pending requests for this same date
        rejectOtherPendingBookingsForDate(dateStr, bookingId)

        // 3. Mark the date slot as BOOKED
        val existingSlot = getDateSlotSync(dateStr)
        if (existingSlot != null) {
            updateDateSlotStatus(dateStr, DateStatus.BOOKED.name, bookingId)
        } else {
            insertDateSlot(
                StudioDateSlot(
                    dateStr = dateStr,
                    status = DateStatus.BOOKED.name,
                    bookedBookingId = bookingId
                )
            )
        }
    }

    @Query("UPDATE booking_requests SET status = 'REJECTED' WHERE dateStr = :dateStr AND id != :exceptBookingId AND status = 'PENDING'")
    suspend fun rejectOtherPendingBookingsForDate(dateStr: String, exceptBookingId: Long)

    // Direct Date Control: set status to FREE or BOOKED
    @Transaction
    suspend fun setDateStatusDirectly(dateStr: String, status: String) {
        val existingSlot = getDateSlotSync(dateStr)
        if (existingSlot != null) {
            updateDateSlotStatus(
                dateStr = dateStr,
                status = status,
                bookingId = if (status == DateStatus.FREE.name) null else existingSlot.bookedBookingId,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            insertDateSlot(
                StudioDateSlot(
                    dateStr = dateStr,
                    status = status,
                    slotType = "FULL_DAY",
                    basePrice = 25000.0,
                    note = if (status == DateStatus.FREE.name) "Available for Booking" else "Booked Date"
                )
            )
        }
    }

    // Direct Booking (FREE -> BOOKED) with customer details
    @Transaction
    suspend fun createDirectBookingTransaction(
        dateStr: String,
        customerName: String,
        customerPhone: String,
        workType: String,
        amount: Double
    ): Long {
        val booking = BookingRequest(
            dateStr = dateStr,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            workType = workType,
            amount = amount,
            location = "",
            notes = "Direct Studio Booking",
            status = BookingStatus.CONFIRMED.name,
            isSubmittedByCurrentCustomer = false
        )
        val bookingId = insertBooking(booking)
        setDateStatusDirectly(dateStr, DateStatus.BOOKED.name)
        updateDateSlotStatus(dateStr, DateStatus.BOOKED.name, bookingId)
        rejectOtherPendingBookingsForDate(dateStr, bookingId)
        return bookingId
    }

    @Transaction
    suspend fun rejectBookingTransaction(bookingId: Long, dateStr: String) {
        updateBookingStatus(bookingId, BookingStatus.REJECTED.name)
        // If the date slot was booked by this specific booking, set it back to FREE
        val existingSlot = getDateSlotSync(dateStr)
        if (existingSlot != null && existingSlot.bookedBookingId == bookingId) {
            updateDateSlotStatus(dateStr, DateStatus.FREE.name, null)
        }
    }
}
