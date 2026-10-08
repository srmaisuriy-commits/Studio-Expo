package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "studio_date_slots",
    indices = [Index(value = ["dateStr"], unique = true)]
)
data class StudioDateSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // Format: "yyyy-MM-dd", e.g. "2026-11-15"
    val status: String = DateStatus.FREE.name, // FREE or BOOKED
    val slotType: String = SlotType.FULL_DAY.name,
    val basePrice: Double = 25000.0,
    val note: String = "",
    val bookedBookingId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
