package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booking_requests")
data class BookingRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val workType: String = "",
    val amount: Double = 0.0,
    val location: String = "",
    val notes: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val isSubmittedByCurrentCustomer: Boolean = false
) {
    val requestedAmount: Double
        get() = amount
}
