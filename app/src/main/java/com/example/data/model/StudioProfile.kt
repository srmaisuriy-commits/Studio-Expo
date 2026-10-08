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
    val customWhatsAppNote: String = "લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન કવરેજ ઉપલબ્ધ છે. સ્પેશિયલ પેકેજ માટે સંપર્ક કરો!",
    val customHeaderGreeting: String = "નમસ્તે! આગામી ઇવેન્ટ્સ અને લગ્ન સીઝન માટે અમારી ઉપલબ્ધ (FREE) તારીખો નીચે મુજબ છે:",
    val customFooterNote: String = "તમારી સ્પેશ્યલ ડેટ સમયસર રિઝર્વ કરાવો! વધુ વિગત માટે WhatsApp પર સંપર્ક કરો."
)
