package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudioProfile
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StudioAmber

@Composable
fun OwnerProfileScreen(
    profile: StudioProfile?,
    onSaveProfile: (StudioProfile) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var studioName by remember(profile) { mutableStateOf(profile?.studioName ?: "Studio Expo") }
    var photographerName by remember(profile) { mutableStateOf(profile?.photographerName ?: "Rajesh Maisuriya") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "9876543210") }
    var whatsapp by remember(profile) { mutableStateOf(profile?.whatsapp ?: "9876543210") }
    var city by remember(profile) { mutableStateOf(profile?.city ?: "Ahmedabad, Gujarat") }
    var pin by remember(profile) { mutableStateOf(profile?.ownerPin ?: "1234") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "Wedding & Fashion Photography") }
    var cameraGear by remember(profile) { mutableStateOf(profile?.cameraGearDetails ?: "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights") }
    var customWhatsAppNote by remember(profile) { mutableStateOf(profile?.customWhatsAppNote ?: "લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન કવરેજ ઉપલબ્ધ છે. તમારા શુભ પ્રસંગના શૂટ માટે સંપર્ક કરો!") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("owner_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(StudioAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = studioName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Photographer: $photographerName",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "📍 $city",
                            fontSize = 11.sp,
                            color = StudioAmber
                        )
                    }
                }
            }
        }

        // Edit Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚙️ Studio Information & Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = studioName,
                        onValueChange = { studioName = it },
                        label = { Text("Studio Name / સ્ટુડિયોનું નામ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = photographerName,
                        onValueChange = { photographerName = it },
                        label = { Text("Photographer / ઓનર નામ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City & State / સરનામું") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Studio Tagline / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Camera Gear & Details
                    OutlinedTextField(
                        value = cameraGear,
                        onValueChange = { cameraGear = it },
                        label = { Text("🎥 Camera & Gear Details (કેમેરા સાધનોની વિગત)") },
                        placeholder = { Text("e.g. Sony FX3 / A7 IV • DJI Drone • 4K Cinematic") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Custom WhatsApp Note
                    OutlinedTextField(
                        value = customWhatsAppNote,
                        onValueChange = { customWhatsAppNote = it },
                        label = { Text("💬 WhatsApp Message Note (મેસેજ નોંધ / ઓફર)") },
                        placeholder = { Text("e.g. લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન ઉપલબ્ધ છે.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) pin = it },
                        label = { Text("Owner Login PIN (4 digits)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StudioAmber)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val baseProfile = profile ?: StudioProfile(id = 1)
                            val updated = baseProfile.copy(
                                id = 1,
                                studioName = studioName.trim(),
                                photographerName = photographerName.trim(),
                                phone = phone.trim(),
                                whatsapp = whatsapp.trim(),
                                city = city.trim(),
                                ownerPin = if (pin.isNotBlank()) pin.trim() else "1234",
                                bio = bio.trim(),
                                cameraGearDetails = cameraGear.trim(),
                                customWhatsAppNote = customWhatsAppNote.trim()
                            )
                            onSaveProfile(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Profile / પ્રોફાઇલ સાચવો",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusBookedRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_logout_btn")
            ) {
                Text(
                    text = "🔒 Logout from Owner Dashboard",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
