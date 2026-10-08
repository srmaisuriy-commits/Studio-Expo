package com.example.data.model

enum class DateStatus(val label: String, val gujaratiLabel: String) {
    FREE("Free", "મુક્ત (Free)"),
    BOOKED("Booked", "બુક થયેલ (Booked)"),
    REQUEST("Request", "વિનંતી (Request)")
}

enum class SlotType(val title: String, val gujaratiTitle: String) {
    FULL_DAY("Full Day Shoot", "આખો દિવસ (Full Day)"),
    MORNING("Morning Slot", "સવારનો સ્લોટ (Morning)"),
    EVENING("Evening / Night", "સાંજ / રાત્રિ (Evening)")
}

enum class BookingStatus(val label: String, val gujaratiLabel: String) {
    PENDING("Pending", "પ્રતીક્ષામાં (Pending)"),
    CONFIRMED("Confirmed", "કન્ફર્મ થયેલ (Confirmed)"),
    REJECTED("Rejected", "રદ થયેલ (Rejected)")
}

data class WorkType(
    val id: String,
    val englishName: String,
    val gujaratiName: String,
    val iconEmoji: String,
    val defaultEstimate: Double
)

object WorkTypeCatalog {
    val allTypes = listOf(
        WorkType("wedding", "Wedding / Lagan", "લગ્ન (Wedding)", "💍", 50000.0),
        WorkType("pre_wedding", "Pre-Wedding Shoot", "પ્રી-વેડિંગ શૂટ", "📸", 35000.0),
        WorkType("sangeet", "Sangeet / Engagement", "સંગીત / સગાઈ", "🎵", 25000.0),
        WorkType("reception", "Reception Night", "રિસેપ્શન", "✨", 30000.0),
        WorkType("baby_shower", "Baby Shower / Maternity", "શ્રીમંત / બેબી શાવર", "🍼", 20000.0),
        WorkType("model_portfolio", "Model / Fashion Portfolio", "મોડેલ / ફેશન પોર્ટફોલિયો", "👗", 25000.0),
        WorkType("studio_portrait", "Studio Portrait / Headshot", "સ્ટુડિયો પોર્ટ્રેટ", "🖼️", 10000.0),
        WorkType("commercial", "Commercial / Corporate Event", "કોમર્શિયલ ઇવેન્ટ", "🏢", 40000.0),
        WorkType("other", "Other Photography", "અન્ય શૂટ", "📷", 15000.0)
    )
}
