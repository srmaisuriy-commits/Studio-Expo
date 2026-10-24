package com.example.data.model

data class BookedCustomerGroup(
    val groupKey: String,
    val customerName: String,
    val customerPhone: String,
    val dates: List<String>,
    val dateCrewMap: Map<String, String>,
    val workTypeSummary: String,
    val totalAmount: Double,
    val slots: List<StudioDateSlot> = emptyList()
)
