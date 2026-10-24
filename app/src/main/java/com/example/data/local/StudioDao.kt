package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    // Profile
    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StudioProfile?>

    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): StudioProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudioProfile)

    @Update
    suspend fun updateProfile(profile: StudioProfile)

    // Date Slots
    @Query("SELECT * FROM studio_date_slots ORDER BY dateStr ASC")
    fun getAllDateSlots(): Flow<List<StudioDateSlot>>

    @Query("SELECT * FROM studio_date_slots WHERE status = :status ORDER BY dateStr ASC")
    fun getDateSlotsByStatus(status: DateStatus): Flow<List<StudioDateSlot>>

    @Query("SELECT * FROM studio_date_slots WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDateSlot(dateStr: String): StudioDateSlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDateSlot(slot: StudioDateSlot)

    @Query("DELETE FROM studio_date_slots WHERE dateStr = :dateStr")
    suspend fun deleteDateSlot(dateStr: String)

    // Bookings
    @Query("SELECT * FROM booking_requests ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingRequest>>

    @Query("SELECT * FROM booking_requests WHERE status = :status ORDER BY dateStr ASC")
    fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingRequest): Long

    @Query("UPDATE booking_requests SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: BookingStatus)

    @Query("SELECT * FROM booking_requests WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): BookingRequest?

    @Update
    suspend fun updateBooking(booking: BookingRequest)

    @Query("DELETE FROM booking_requests WHERE id = :id")
    suspend fun deleteBooking(id: Long)

    @Query("DELETE FROM booking_requests WHERE dateStr = :dateStr")
    suspend fun deleteBookingByDate(dateStr: String)
}
