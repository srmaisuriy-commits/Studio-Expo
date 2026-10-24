package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_profile")
data class StudioProfile(
    @PrimaryKey val id: Int = 1,
    val studioName: String = "Studio Expo",
    val photographerName: String = "Rajesh Maisuriya",
    val phone: String = "9876543210",
    val whatsapp: String = "9876543210",
    val city: String = "Ahmedabad, Gujarat",
    val ownerPin: String = "1234",
    val bio: String = "Professional Wedding, Pre-Wedding & Cinematic Fashion Photography",
    val instagramHandle: String = "@studioexpo.official",
    val cameraGearDetails: String = "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights",
    val customWhatsAppNote: String = "Latest 4K Cinematic Camera & Drone Coverage Available. Special packages available! / नवीनतम 4K सिनेमैटिक कैमरा और ड्रोन कवरेज उपलब्ध है।",
    val customHeaderGreeting: String = "Hello! Here are our available (FREE) dates for upcoming wedding & events / नमस्ते! आगामी शादी और इवेंट्स के लिए हमारी उपलब्ध (FREE) तारीखें:",
    val customFooterNote: String = "Reserve your special date today! Contact us on WhatsApp for bookings & details / अपनी तारीख आज ही बुक करें!",
    val perDayRate: Double = 25000.0,
    val rateTraditionalPhoto: Double = 10000.0,
    val rateTraditionalVideo: Double = 12000.0,
    val rateCandidPhoto: Double = 18000.0,
    val rateSemiCandidPhoto: Double = 14000.0,
    val rateCinematographer: Double = 25000.0,
    val rateDronePilot: Double = 12000.0,
    val rateSupportingCrew: Double = 5000.0
)
