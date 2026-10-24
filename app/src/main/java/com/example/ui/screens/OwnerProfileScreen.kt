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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val baseProfile = profile ?: StudioProfile()

    var studioName by remember(profile) { mutableStateOf(baseProfile.studioName) }
    var photographerName by remember(profile) { mutableStateOf(baseProfile.photographerName) }
    var phone by remember(profile) { mutableStateOf(baseProfile.phone) }
    var whatsapp by remember(profile) { mutableStateOf(baseProfile.whatsapp) }
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
    var cameraGear by remember(profile) { mutableStateOf(baseProfile.cameraGearDetails) }
    var customWhatsAppNote by remember(profile) { mutableStateOf(baseProfile.customWhatsAppNote) }
    var pin by remember(profile) { mutableStateOf(baseProfile.ownerPin) }

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
                        text = "Set daily rates for each crew role (synced to calendar, bill & web page)",
                        fontSize = 11.sp,
                        color = TextLightSecondary
                    )

                    OutlinedTextField(
                        value = rateTraditionalPhotoText,
                        onValueChange = { rateTraditionalPhotoText = it },
                        label = { Text("📸 Traditional Photographer Rate / ट्रेडिशनल फोटोग्राफर (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_trad_photo"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateTraditionalVideoText,
                        onValueChange = { rateTraditionalVideoText = it },
                        label = { Text("📹 Traditional Videographer Rate / ट्रेडिशनल वीडियोग्राफर (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_trad_video"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateCandidPhotoText,
                        onValueChange = { rateCandidPhotoText = it },
                        label = { Text("📷 Candid Photographer Rate / कैंडिड फोटोग्राफर (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_candid_photo"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateSemiCandidPhotoText,
                        onValueChange = { rateSemiCandidPhotoText = it },
                        label = { Text("🎞️ Semi-Candid Photographer Rate / सेमी-कैंडिड फोटोग्राफर (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_semicandid_photo"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateCinematographerText,
                        onValueChange = { rateCinematographerText = it },
                        label = { Text("🎬 Cinematographer Rate / सिनेमैटोग्राफर (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_cinematographer"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateDronePilotText,
                        onValueChange = { rateDronePilotText = it },
                        label = { Text("🛸 Drone Pilot Rate / ड्रोन पायलट (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_drone_pilot"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = rateSupportingCrewText,
                        onValueChange = { rateSupportingCrewText = it },
                        label = { Text("👥 Supporting Crew Rate / सपोर्टिंग क्रू / असिस्टेंट (₹)") },
                        leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("profile_rate_supporting_crew"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    OutlinedTextField(
                        value = cameraGear,
                        onValueChange = { cameraGear = it },
                        label = { Text("Camera Gear / कैमरा और उपकरण") },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_camera_gear"),
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
                        label = { Text("Default WhatsApp Broadcast Message") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_whatsapp_note"),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )
                }
            }
        }

        // Security PIN Card
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
                        text = "🔒 Owner Security PIN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) pin = it },
                        label = { Text("Security PIN (4-6 digits)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = StudioAmber) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("profile_owner_pin"),
                        singleLine = true,
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
                    val rate = perDayRateText.toDoubleOrNull() ?: baseProfile.perDayRate
                    val updated = baseProfile.copy(
                        studioName = studioName.trim(),
                        photographerName = photographerName.trim(),
                        phone = phone.trim(),
                        whatsapp = whatsapp.trim(),
                        city = city.trim(),
                        bio = bio.trim(),
                        ownerPin = if (pin.isNotBlank()) pin.trim() else "1234",
                        perDayRate = rate,
                        rateTraditionalPhoto = rateTraditionalPhotoText.toDoubleOrNull() ?: baseProfile.rateTraditionalPhoto,
                        rateTraditionalVideo = rateTraditionalVideoText.toDoubleOrNull() ?: baseProfile.rateTraditionalVideo,
                        rateCandidPhoto = rateCandidPhotoText.toDoubleOrNull() ?: baseProfile.rateCandidPhoto,
                        rateSemiCandidPhoto = rateSemiCandidPhotoText.toDoubleOrNull() ?: baseProfile.rateSemiCandidPhoto,
                        rateCinematographer = rateCinematographerText.toDoubleOrNull() ?: baseProfile.rateCinematographer,
                        rateDronePilot = rateDronePilotText.toDoubleOrNull() ?: baseProfile.rateDronePilot,
                        rateSupportingCrew = rateSupportingCrewText.toDoubleOrNull() ?: baseProfile.rateSupportingCrew,
                        cameraGearDetails = cameraGear.trim(),
                        customWhatsAppNote = customWhatsAppNote.trim()
                    )
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
