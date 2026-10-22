package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_date_slots")
data class StudioDateSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateStr: String = "",
    val status: DateStatus = DateStatus.FREE,
    val note: String = "",
    val slotType: SlotType = SlotType.FULL_DAY,
    val basePrice: Double = 0.0,
    val bookedCustomerName: String = "",
    val bookedCustomerPhone: String = "",
    val workType: String = "",
    val amount: Double = 0.0,
    val bookedBookingId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
