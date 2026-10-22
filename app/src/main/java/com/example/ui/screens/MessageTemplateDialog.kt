package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudioProfile
import com.example.ui.theme.StudioAmber

@Composable
fun MessageTemplateDialog(
    profile: StudioProfile?,
    onDismiss: () -> Unit,
    onSave: (gear: String, note: String, greeting: String, footer: String) -> Unit
) {
    var gear by remember(profile) { mutableStateOf(profile?.cameraGearDetails ?: "") }
    var note by remember(profile) { mutableStateOf(profile?.customWhatsAppNote ?: "") }
    var greeting by remember(profile) { mutableStateOf(profile?.customHeaderGreeting ?: "") }
    var footer by remember(profile) { mutableStateOf(profile?.customFooterNote ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("message_template_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "WhatsApp Template / टेम्पलेट",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = greeting,
                    onValueChange = { greeting = it },
                    label = { Text("Header Greeting (नमस्ते/ग्रीटिंग)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gear,
                    onValueChange = { gear = it },
                    label = { Text("Camera & Gear Details (कैमरा गियर)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Special Highlights / Note (नोट)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = { Text("Footer Call To Action (कॉल टू एक्शन)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onSave(gear, note, greeting, footer) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Template", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
