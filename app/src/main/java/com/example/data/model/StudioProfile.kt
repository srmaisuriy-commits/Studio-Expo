package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_profile")
data class StudioProfile(
    @PrimaryKey val id: Int = 1,
    val studioName: String = "Studio Expo",
    val photographerName: String = "Rajesh Maisuriya",
    val phone: String = "+91 XXXXX XX520",
    val whatsapp: String = "+91 XXXXX XX520",
    val city: String = "Ahmedabad, Gujarat",
    val ownerPin: String = "1234",
    val bio: String = "Professional Wedding, Pre-Wedding & Cinematic Fashion Photography",
    val instagramHandle: String = "@studioexpo.official",
    val cameraGearDetails: String = DEFAULT_CAMERA_GEAR,
    val customWhatsAppNote: String = DEFAULT_WHATSAPP_NOTE,
    val customHeaderGreeting: String = DEFAULT_HEADER_GREETING,
    val customFooterNote: String = DEFAULT_FOOTER_NOTE,
    val perDayRate: Double = 25000.0,
    val rateTraditionalPhoto: Double = 10000.0,
    val rateTraditionalVideo: Double = 12000.0,
    val rateCandidPhoto: Double = 18000.0,
    val rateSemiCandidPhoto: Double = 14000.0,
    val rateCinematographer: Double = 25000.0,
    val rateDronePilot: Double = 12000.0,
    val rateSupportingCrew: Double = 5000.0,
    val showRateTraditionalPhoto: Boolean = true,
    val showRateTraditionalVideo: Boolean = true,
    val showRateCandidPhoto: Boolean = true,
    val showRateSemiCandidPhoto: Boolean = true,
    val showRateCinematographer: Boolean = true,
    val showRateDronePilot: Boolean = true,
    val showRateSupportingCrew: Boolean = true
) {
    companion object {
        const val DEFAULT_CAMERA_GEAR =
            "Canon EOS R5 - Cinematographer\n" +
            "Canon Ultrasonic 85 MM - EF/1.8\n" +
            "Canon 50 MM - RF F/1.8\n" +
            "Sigma 35 MM - RF F/1.4\n" +
            "Canon 16 MM - RF F/2.8\n" +
            "Ronin RS4 GIMBLE\n" +
            "Godox LC500R Light\n" +
            "Zhiyun Cinepeer CX100 Light"

        const val DEFAULT_WHATSAPP_NOTE =
            "Wedding - 2025 - 2026\n" +
            "Cinematic Wedding & Event Shoot Bookings Open! Check my free dates & book directly on the live calendar link below.\n" +
            "वेडिंग 2025 - 2026 बुकिंग शुरू है! नीचे दिए गए लाइव कैलेंडर लिंक पर फ्री तारीखें देखें और सीधे बुक करें।"

        const val DEFAULT_HEADER_GREETING =
            "Wedding - 2025 - 2026\n" +
            "Hello! Here are my available (FREE) dates / नमस्ते! मेरी उपलब्ध (FREE) तारीखें:"

        const val DEFAULT_FOOTER_NOTE =
            "Book your date directly using the Live Calendar link below / नीचे दिए गए लाइव कैलेंडर लिंक से अपनी तारीख बुक करें!"
    }
}
