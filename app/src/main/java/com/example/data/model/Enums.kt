package com.example.data.model

enum class DateStatus(val label: String, val hindiLabel: String) {
    FREE("Free", "उपलब्ध (Free)"),
    BOOKED("Booked", "बुक (Booked)"),
    REQUEST("Request", "अनुरोध (Request)")
}

enum class SlotType(val title: String, val hindiTitle: String) {
    FULL_DAY("Full Day Shoot", "पूरा दिन (Full Day)"),
    MORNING("Morning Slot", "सुबह का स्लॉट (Morning)"),
    EVENING("Evening / Night", "शाम / रात (Evening)")
}

enum class BookingStatus(val label: String, val hindiLabel: String) {
    PENDING("Pending", "प्रतीक्षारत (Pending)"),
    CONFIRMED("Confirmed", "पुष्टि की गई (Confirmed)"),
    REJECTED("Rejected", "अस्वीकृत (Rejected)")
}

data class WorkType(
    val id: String,
    val englishName: String,
    val hindiName: String,
    val iconEmoji: String,
    val defaultEstimate: Double
) {
    val displayName: String
        get() = "$englishName ($hindiName)"
}

object WorkTypeCatalog {
    val allTypes = listOf(
        WorkType("wedding", "Wedding Ceremony", "शादी / विवाह", "💍", 50000.0),
        WorkType("pre_wedding", "Pre-Wedding Shoot", "प्री-वेडिंग शूट", "📸", 35000.0),
        WorkType("sangeet", "Sangeet / Engagement", "संगीत / सगाई", "🎵", 25000.0),
        WorkType("reception", "Reception Night", "रिसेप्शन", "✨", 30000.0),
        WorkType("baby_shower", "Baby Shower / Maternity", "बेबी शॉवर / मातृत्व", "🍼", 20000.0),
        WorkType("model_portfolio", "Model / Fashion Portfolio", "मॉडल पोर्टफोलियो", "👗", 25000.0),
        WorkType("studio_portrait", "Studio Portrait / Headshot", "स्टूडियो पोर्ट्रेट", "🖼️", 10000.0),
        WorkType("commercial", "Commercial / Corporate Event", "कमर्शियल इवेंट", "🏢", 40000.0),
        WorkType("other", "Other Photography", "अन्य फोटोग्राफी", "📷", 15000.0)
    )
}

