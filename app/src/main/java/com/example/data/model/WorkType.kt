package com.example.data.model

data class WorkType(
    val id: String,
    val englishName: String,
    val hindiName: String,
    val iconEmoji: String,
    val defaultEstimate: Double
) {
    val displayName: String
        get() = "$englishName ($hindiName)"

    val emoji: String
        get() = iconEmoji

    val title: String
        get() = englishName

    val name: String
        get() = englishName
}

object WorkTypeCatalog {
    val allTypes: List<WorkType> = listOf(
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

    val list: List<WorkType>
        get() = allTypes
}
