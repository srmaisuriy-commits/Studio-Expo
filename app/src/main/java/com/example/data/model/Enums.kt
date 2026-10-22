package com.example.data.model

enum class BookingStatus(val label: String, val hindiLabel: String) {
    PENDING("Pending", "प्रतीक्षारत (Pending)"),
    CONFIRMED("Confirmed", "पुष्टि की गई (Confirmed)"),
    REJECTED("Rejected", "अस्वीकृत (Rejected)")
}

enum class DateStatus(val label: String, val hindiLabel: String) {
    FREE("Free", "उपलब्ध (Free)"),
    BOOKED("Booked", "बुक (Booked)"),
    REQUEST("Request", "अनुरोध (Request)")
}

enum class SlotType(val title: String, val hindiTitle: String) {
    FULL_DAY("Full Day Shoot", "पूरा दिन (Full Day)"),
    MORNING("Morning Slot", "सुबह का स्लॉट (Morning)"),
    EVENING("Evening Slot", "शाम का स्लॉट (Evening)")
}
