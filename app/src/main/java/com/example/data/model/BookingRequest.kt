package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booking_requests")
data class BookingRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // "yyyy-MM-dd"
    val customerName: String,
    val customerPhone: String,
    val workType: String, // e.g. "Wedding / લગ્ન"
    val amount: Double = 0.0,
    val location: String = "",
    val notes: String = "",
    val status: String = BookingStatus.PENDING.name, // PENDING, CONFIRMED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val isSubmittedByCurrentCustomer: Boolean = false
)
