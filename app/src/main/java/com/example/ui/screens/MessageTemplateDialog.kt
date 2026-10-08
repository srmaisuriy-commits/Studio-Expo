package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudioProfile
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber

@Composable
fun MessageTemplateDialog(
    profile: StudioProfile?,
    onDismiss: () -> Unit,
    onSaveTemplate: (gear: String, note: String, greeting: String, footer: String) -> Unit,
    onShareClick: (message: String) -> Unit,
    generateMessage: (gear: String, note: String, greeting: String, footer: String) -> String
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Edit Message, 1: Live Preview

    var cameraGear by remember(profile) {
        mutableStateOf(profile?.cameraGearDetails ?: "Sony FX3 / A7 IV • DJI Drone • 4K Cinematic Video • Gimbal & Pro Lights")
    }
    var greetingText by remember(profile) {
        mutableStateOf(profile?.customHeaderGreeting ?: "નમસ્તે! આગામી ઇવેન્ટ્સ અને લગ્ન સીઝન માટે અમારી ઉપલબ્ધ (FREE) તારીખો નીચે મુજબ છે:")
    }
    var customNote by remember(profile) {
        mutableStateOf(profile?.customWhatsAppNote ?: "લેટેસ્ટ સિનેમેટિક કેમેરા અને ડ્રોન કવરેજ ઉપલબ્ધ છે. તમારા શુભ પ્રસંગના શૂટ માટે સંપર્ક કરો!")
    }
    var footerText by remember(profile) {
        mutableStateOf(profile?.customFooterNote ?: "તમારી સ્પેશ્યલ ડેટ સમયસર રિઝર્વ કરાવો! વધુ વિગત માટે WhatsApp પર સંપર્ક કરો.")
    }

    // Live preview string dynamically recomputes as user edits
    val currentFormattedMessage = remember(cameraGear, greetingText, customNote, footerText, profile) {
        generateMessage(cameraGear, customNote, greetingText, footerText)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("message_template_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📝 Set WhatsApp Message",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "આખો મેસેજ સેટ કરો (તારીખો આપોઆપ સેટ થશે)",
                            fontSize = 11.sp,
                            color = StudioAmber
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: 0: Edit Message, 1: Live Preview
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("✏️ Edit Details", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("👁️ Live Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: EDIT ALL FIELDS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Camera Details Field
                        OutlinedTextField(
                            value = cameraGear,
                            onValueChange = { cameraGear = it },
                            label = { Text("🎥 Camera Details (કેમેરા અને સાધનોની વિગત)") },
                            placeholder = { Text("Sony FX3, A7 IV, DJI Drone, 4K, Lights") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("template_camera_gear_input"),
                            maxLines = 2
                        )

                        // 2. Greeting / Header Message
                        OutlinedTextField(
                            value = greetingText,
                            onValueChange = { greetingText = it },
                            label = { Text("✨ Greeting (શરૂઆતનો મેસેજ)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        // 3. Special Offer / Note
                        OutlinedTextField(
                            value = customNote,
                            onValueChange = { customNote = it },
                            label = { Text("💬 Offers / Special Note (ખાસ નોંધ / ઓફર્સ)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        // 4. Auto Date Placeholder Info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusFreeGreen.copy(alpha = 0.12f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "📅 [FREE DATES]: કેલેન્ડરની તમામ current 🟢 Free Dates આ જગ્યાએ આપોઆપ લિસ્ટ થઈ જશે!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusFreeGreen
                            )
                        }

                        // 5. Footer Note
                        OutlinedTextField(
                            value = footerText,
                            onValueChange = { footerText = it },
                            label = { Text("📝 Footer Note (નીચેની વિગત)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                } else {
                    // TAB 1: LIVE PREVIEW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFEAE2)) // WhatsApp chat background tint
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    ) {
                        Text(
                            text = currentFormattedMessage,
                            fontSize = 12.sp,
                            color = Color(0xFF111B21),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Save Template & Share on WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            onSaveTemplate(cameraGear, customNote, greetingText, footerText)
                            Toast.makeText(context, "મેસેજ ટેમ્પલેટ સેવ થઈ ગયું! (Saved)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_template_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Template 💾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    // Share on WhatsApp Button
                    Button(
                        onClick = {
                            onShareClick(currentFormattedMessage)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("share_whatsapp_template_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share WhatsApp 📲",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
