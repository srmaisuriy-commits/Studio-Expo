package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudioProfile
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@Composable
fun OwnerProfileScreen(
    profile: StudioProfile?,
    onSaveProfile: (StudioProfile) -> Unit,
    onLogout: () -> Unit,
    remainingTrialDays: Int = 30,
    isSubscribed: Boolean = false,
    onUpgradeSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val baseProfile = profile ?: StudioProfile()

    var studioName by remember(profile) { mutableStateOf(baseProfile.studioName) }
    var photographerName by remember(profile) { mutableStateOf(baseProfile.photographerName) }
    val initialPhone = if (baseProfile.phone == "9876543210" || baseProfile.phone == "9898012345" || baseProfile.phone.isBlank()) "+91 XXXXX XX520" else baseProfile.phone
    val initialWhatsapp = if (baseProfile.whatsapp == "9876543210" || baseProfile.whatsapp == "9898012345" || baseProfile.whatsapp.isBlank()) "+91 XXXXX XX520" else baseProfile.whatsapp
    var phone by remember(profile) { mutableStateOf(initialPhone) }
    var whatsapp by remember(profile) { mutableStateOf(initialWhatsapp) }
    var city by remember(profile) { mutableStateOf(baseProfile.city) }
    var bio by remember(profile) { mutableStateOf(baseProfile.bio) }
    var perDayRateText by remember(profile) { mutableStateOf(baseProfile.perDayRate.toLong().toString()) }
    var rateTraditionalPhotoText by remember(profile) { mutableStateOf(baseProfile.rateTraditionalPhoto.toLong().toString()) }
    var rateTraditionalVideoText by remember(profile) { mutableStateOf(baseProfile.rateTraditionalVideo.toLong().toString()) }
    var rateCandidPhotoText by remember(profile) { mutableStateOf(baseProfile.rateCandidPhoto.toLong().toString()) }
    var rateSemiCandidPhotoText by remember(profile) { mutableStateOf(baseProfile.rateSemiCandidPhoto.toLong().toString()) }
    var rateCinematographerText by remember(profile) { mutableStateOf(baseProfile.rateCinematographer.toLong().toString()) }
    var rateDronePilotText by remember(profile) { mutableStateOf(baseProfile.rateDronePilot.toLong().toString()) }
    var rateSupportingCrewText by remember(profile) { mutableStateOf(baseProfile.rateSupportingCrew.toLong().toString()) }
    var showTradPhoto by remember(profile) { mutableStateOf(baseProfile.showRateTraditionalPhoto) }
    var showTradVideo by remember(profile) { mutableStateOf(baseProfile.showRateTraditionalVideo) }
    var showCandidPhoto by remember(profile) { mutableStateOf(baseProfile.showRateCandidPhoto) }
    var showSemiCandidPhoto by remember(profile) { mutableStateOf(baseProfile.showRateSemiCandidPhoto) }
    var showCinematographer by remember(profile) { mutableStateOf(baseProfile.showRateCinematographer) }
    var showDronePilot by remember(profile) { mutableStateOf(baseProfile.showRateDronePilot) }
    var showSupportingCrew by remember(profile) { mutableStateOf(baseProfile.showRateSupportingCrew) }
    var cameraGear by remember(profile) { mutableStateOf(baseProfile.cameraGearDetails) }
    var customWhatsAppNote by remember(profile) { mutableStateOf(baseProfile.customWhatsAppNote) }

    fun buildUpdatedProfile(
        tradPhotoOn: Boolean = showTradPhoto,
        tradVideoOn: Boolean = showTradVideo,
        candidOn: Boolean = showCandidPhoto,
        semiCandidOn: Boolean = showSemiCandidPhoto,
        cinemaOn: Boolean = showCinematographer,
        droneOn: Boolean = showDronePilot,
        supportOn: Boolean = showSupportingCrew
    ): StudioProfile {
        val rate = perDayRateText.toDoubleOrNull() ?: baseProfile.perDayRate
        return baseProfile.copy(
            studioName = studioName.trim(),
            photographerName = photographerName.trim(),
            phone = phone.trim(),
            whatsapp = whatsapp.trim(),
            city = city.trim(),
            bio = bio.trim(),
            ownerPin = baseProfile.ownerPin,
            perDayRate = rate,
            rateTraditionalPhoto = rateTraditionalPhotoText.toDoubleOrNull() ?: baseProfile.rateTraditionalPhoto,
            rateTraditionalVideo = rateTraditionalVideoText.toDoubleOrNull() ?: baseProfile.rateTraditionalVideo,
            rateCandidPhoto = rateCandidPhotoText.toDoubleOrNull() ?: baseProfile.rateCandidPhoto,
            rateSemiCandidPhoto = rateSemiCandidPhotoText.toDoubleOrNull() ?: baseProfile.rateSemiCandidPhoto,
            rateCinematographer = rateCinematographerText.toDoubleOrNull() ?: baseProfile.rateCinematographer,
            rateDronePilot = rateDronePilotText.toDoubleOrNull() ?: baseProfile.rateDronePilot,
            rateSupportingCrew = rateSupportingCrewText.toDoubleOrNull() ?: baseProfile.rateSupportingCrew,
            showRateTraditionalPhoto = tradPhotoOn,
            showRateTraditionalVideo = tradVideoOn,
            showRateCandidPhoto = candidOn,
            showRateSemiCandidPhoto = semiCandidOn,
            showRateCinematographer = cinemaOn,
            showRateDronePilot = droneOn,
            showRateSupportingCrew = supportOn,
            cameraGearDetails = cameraGear.trim(),
            customWhatsAppNote = customWhatsAppNote.trim()
        )
    }

    fun countEnabled(vararg flags: Boolean): Int = flags.count { it }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚙️ Studio Profile & Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextLightPrimary
            )
            Text(
                text = "Configure studio branding, crew package rates, and contact info",
                fontSize = 12.sp,
                color = TextLightSecondary
            )
        }

        // VIP Membership & Trial Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSubscribed) Color(0xFF1B2A1E) else Color(0xFF231B0E)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSubscribed) Color(0xFF4ADE80) else StudioAmber
                ),
                modifier = Modifier.testTag("profile_membership_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (isSubscribed) "👑 " else "⏳ ", fontSize = 16.sp)
                            Text(
                                text = if (isSubscribed) "VIP Membership Active (1 Year)" else "Free Trial ($remainingTrialDays Days Left)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubscribed) Color(0xFF4ADE80) else StudioAmber
                            )
                        }
                        if (!isSubscribed) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE65100).copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                            ) {
                                Text(
                                    text = "LIMITED OFFER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFB74D),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isSubscribed) "Your VIP plan is active. All premium features unlocked / आपका 1 साल का VIP प्लान सक्रिय है।"
                        else "Special 1-Year VIP Plan available at ₹149 (सीमित समय के लिए केवल ₹149 में 1 साल का एक्सेस)",
                        fontSize = 11.sp,
                        color = TextLightSecondary
                    )

                    if (!isSubscribed) {
                        Button(
                            onClick = onUpgradeSubscription,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_upgrade_profile"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioAmber,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "💳 Get 1 Year Access @ ₹149 (85% OFF)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Studio Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🏢 Branding Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )

                    OutlinedTextField(
                        value = studioName,
                        onValueChange = { studioName = it },
                        label = { Text("Studio Name / स्टूडियो का नाम") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_studio_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = photographerName,
                        onValueChange = { photographerName = it },
                        label = { Text("Owner / Photographer Name / फोटोग्राफर का नाम") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_photographer_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / Location / शहर का नाम") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_city"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio / Tagline / विवरण") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_bio"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )
                }
            }
        }

        // Pricing & Rates Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💰 Crew Package Rates / क्रू पैकेज रेट्स",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )
                    Text(
                        text = "Turn ON only the crew rates you want visible on the customer web link / ग्राहक वेब लिंक पर जो क्रू रेट दिखाना हो उसे ON रखें",
                        fontSize = 11.sp,
                        color = TextLightSecondary
                    )

                    CrewRateToggleField(
                        value = rateTraditionalPhotoText,
                        onValueChange = { rateTraditionalPhotoText = it },
                        label = "📸 Traditional Photographer Rate / ट्रेडिशनल फोटोग्राफर (₹)",
                        isEnabledOnLink = showTradPhoto,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(false, showTradVideo, showCandidPhoto, showSemiCandidPhoto, showCinematographer, showDronePilot, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showTradPhoto = newVal
                                onSaveProfile(buildUpdatedProfile(tradPhotoOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_trad_photo",
                        switchTag = "toggle_rate_trad_photo"
                    )

                    CrewRateToggleField(
                        value = rateTraditionalVideoText,
                        onValueChange = { rateTraditionalVideoText = it },
                        label = "📹 Traditional Videographer Rate / ट्रेडिशनल वीडियोग्राफर (₹)",
                        isEnabledOnLink = showTradVideo,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, false, showCandidPhoto, showSemiCandidPhoto, showCinematographer, showDronePilot, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showTradVideo = newVal
                                onSaveProfile(buildUpdatedProfile(tradVideoOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_trad_video",
                        switchTag = "toggle_rate_trad_video"
                    )

                    CrewRateToggleField(
                        value = rateCandidPhotoText,
                        onValueChange = { rateCandidPhotoText = it },
                        label = "📷 Candid Photographer Rate / कैंडिड फोटोग्राफर (₹)",
                        isEnabledOnLink = showCandidPhoto,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, showTradVideo, false, showSemiCandidPhoto, showCinematographer, showDronePilot, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showCandidPhoto = newVal
                                onSaveProfile(buildUpdatedProfile(candidOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_candid_photo",
                        switchTag = "toggle_rate_candid_photo"
                    )

                    CrewRateToggleField(
                        value = rateSemiCandidPhotoText,
                        onValueChange = { rateSemiCandidPhotoText = it },
                        label = "🎞️ Semi-Candid Photographer Rate / सेमी-कैंडिड फोटोग्राफर (₹)",
                        isEnabledOnLink = showSemiCandidPhoto,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, showTradVideo, showCandidPhoto, false, showCinematographer, showDronePilot, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showSemiCandidPhoto = newVal
                                onSaveProfile(buildUpdatedProfile(semiCandidOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_semicandid_photo",
                        switchTag = "toggle_rate_semicandid_photo"
                    )

                    CrewRateToggleField(
                        value = rateCinematographerText,
                        onValueChange = { rateCinematographerText = it },
                        label = "🎬 Cinematographer Rate / सिनेमैटोग्राफर (₹)",
                        isEnabledOnLink = showCinematographer,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, showTradVideo, showCandidPhoto, showSemiCandidPhoto, false, showDronePilot, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showCinematographer = newVal
                                onSaveProfile(buildUpdatedProfile(cinemaOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_cinematographer",
                        switchTag = "toggle_rate_cinematographer"
                    )

                    CrewRateToggleField(
                        value = rateDronePilotText,
                        onValueChange = { rateDronePilotText = it },
                        label = "🛸 Drone Pilot Rate / ड्रोन पायलट (₹)",
                        isEnabledOnLink = showDronePilot,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, showTradVideo, showCandidPhoto, showSemiCandidPhoto, showCinematographer, false, showSupportingCrew) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showDronePilot = newVal
                                onSaveProfile(buildUpdatedProfile(droneOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_drone_pilot",
                        switchTag = "toggle_rate_drone_pilot"
                    )

                    CrewRateToggleField(
                        value = rateSupportingCrewText,
                        onValueChange = { rateSupportingCrewText = it },
                        label = "👥 Supporting Crew Rate / सपोर्टिंग क्रू / असिस्टेंट (₹)",
                        isEnabledOnLink = showSupportingCrew,
                        onToggleLink = { newVal ->
                            if (!newVal && countEnabled(showTradPhoto, showTradVideo, showCandidPhoto, showSemiCandidPhoto, showCinematographer, showDronePilot, false) == 0) {
                                Toast.makeText(context, "Keep at least 1 crew ON / कम से कम 1 क्रू ON रखें", Toast.LENGTH_SHORT).show()
                            } else {
                                showSupportingCrew = newVal
                                onSaveProfile(buildUpdatedProfile(supportOn = newVal))
                            }
                        },
                        fieldTag = "profile_rate_supporting_crew",
                        switchTag = "toggle_rate_supporting_crew"
                    )

                    OutlinedTextField(
                        value = cameraGear,
                        onValueChange = { cameraGear = it },
                        label = { Text("Camera Gear / कैमरा और उपकरण") },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_camera_gear"),
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )
                }
            }
        }

        // Contact & WhatsApp Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📞 Contact & WhatsApp Integration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone / संपर्क फोन नंबर") },
                        placeholder = { Text("+91 XXXXX XX520") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StudioAmber) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp Number / व्हाट्सएप नंबर") },
                        placeholder = { Text("+91 XXXXX XX520") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StudioAmber) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("profile_whatsapp"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = customWhatsAppNote,
                        onValueChange = { customWhatsAppNote = it },
                        label = { Text("Default WhatsApp Broadcast Message / डिफ़ॉल्ट व्हाट्सएप ब्रॉडकास्ट संदेश") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_whatsapp_note"),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )
                }
            }
        }

        // Action Buttons
        item {
            Button(
                onClick = {
                    val updated = buildUpdatedProfile()
                    onSaveProfile(updated)
                    Toast.makeText(context, "Profile saved successfully! / प्रोफाइल सेव हो गई है!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Profile / प्रोफाइल सेव करें",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_owner_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusBookedRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusBookedRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out Owner / लॉग आउट",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = StatusBookedRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CrewRateToggleField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEnabledOnLink: Boolean,
    onToggleLink: (Boolean) -> Unit,
    fieldTag: String,
    switchTag: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Text(
                    "₹",
                    color = StudioAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag(fieldTag),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudioAmber,
                cursorColor = StudioAmber
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnabledOnLink) "Show on Link: ON / लिंक पर दिखेगा" else "Show on Link: OFF / लिंक पर नहीं दिखेगा",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabledOnLink) Color(0xFF4ADE80) else TextLightSecondary
            )
            Switch(
                checked = isEnabledOnLink,
                onCheckedChange = onToggleLink,
                modifier = Modifier.testTag(switchTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = StudioAmber
                )
            )
        }
    }
}
