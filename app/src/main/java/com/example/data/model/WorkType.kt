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
        WorkType(
            id = "traditional_photo",
            englishName = "Traditional Photographer",
            hindiName = "ट्रेडिशनल फोटोग्राफर",
            iconEmoji = "📸",
            defaultEstimate = 10000.0
        ),
        WorkType(
            id = "traditional_video",
            englishName = "Traditional Videographer",
            hindiName = "ट्रेडिशनल वीडियोग्राफर",
            iconEmoji = "📹",
            defaultEstimate = 12000.0
        ),
        WorkType(
            id = "candid_photo",
            englishName = "Candid Photographer",
            hindiName = "कैंडिड फोटोग्राफर",
            iconEmoji = "📷",
            defaultEstimate = 18000.0
        ),
        WorkType(
            id = "semi_candid_photo",
            englishName = "Semi-Candid Photographer",
            hindiName = "सेमी-कैंडिड फोटोग्राफर",
            iconEmoji = "🎞️",
            defaultEstimate = 14000.0
        ),
        WorkType(
            id = "cinematographer",
            englishName = "Cinematographer",
            hindiName = "सिनेमैटोग्राफर",
            iconEmoji = "🎬",
            defaultEstimate = 25000.0
        ),
        WorkType(
            id = "drone_pilot",
            englishName = "Drone Pilot",
            hindiName = "ड्रोन पायलट",
            iconEmoji = "🛸",
            defaultEstimate = 12000.0
        ),
        WorkType(
            id = "supporting_crew",
            englishName = "Supporting Crew",
            hindiName = "सपोर्टिंग क्रू / असिस्टेंट",
            iconEmoji = "👥",
            defaultEstimate = 5000.0
        )
    )

    val list: List<WorkType>
        get() = allTypes

    fun getTypesForProfile(profile: StudioProfile?): List<WorkType> {
        if (profile == null) return allTypes
        return listOf(
            allTypes[0].copy(defaultEstimate = profile.rateTraditionalPhoto),
            allTypes[1].copy(defaultEstimate = profile.rateTraditionalVideo),
            allTypes[2].copy(defaultEstimate = profile.rateCandidPhoto),
            allTypes[3].copy(defaultEstimate = profile.rateSemiCandidPhoto),
            allTypes[4].copy(defaultEstimate = profile.rateCinematographer),
            allTypes[5].copy(defaultEstimate = profile.rateDronePilot),
            allTypes[6].copy(defaultEstimate = profile.rateSupportingCrew)
        )
    }

    fun findByNameOrId(nameOrId: String, profile: StudioProfile? = null): WorkType? {
        val types = if (profile != null) getTypesForProfile(profile) else allTypes
        return types.find {
            it.id.equals(nameOrId, ignoreCase = true) ||
            it.englishName.equals(nameOrId, ignoreCase = true) ||
            it.displayName.equals(nameOrId, ignoreCase = true)
        } ?: allTypes.find {
            it.id.equals(nameOrId, ignoreCase = true) ||
            it.englishName.equals(nameOrId, ignoreCase = true) ||
            it.displayName.equals(nameOrId, ignoreCase = true)
        }
    }
}
