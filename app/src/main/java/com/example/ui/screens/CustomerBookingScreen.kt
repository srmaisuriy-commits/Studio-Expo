package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.model.WorkTypeCatalog
import com.example.ui.components.StudioCalendarView
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerBookingScreen(
    profile: StudioProfile?,
    year: Int,
    monthZeroIndexed: Int,
    dateSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    selectedDate: String?,
    bookingSuccessDialog: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (String) -> Unit,
    onSubmitBooking: (List<String>, String, String, String, Double, String, String) -> Unit,
    onDismissSuccess: () -> Unit,
    onWhatsAppDirect: (String, String) -> Unit,
    onCallDirect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studio = profile ?: StudioProfile()
    val perDayRate = studio.perDayRate

    // Multi-date selection for customer
    var selectedDatesSet by remember { mutableStateOf(setOf<String>()) }

    // Synchronize with external selectedDate (e.g. from deep link)
    LaunchedEffect(selectedDate) {
        if (!selectedDate.isNullOrBlank()) {
            selectedDatesSet = setOf(selectedDate)
        }
    }

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedWorkTypes by remember {
        mutableStateOf(setOf(WorkTypeCatalog.allTypes.first().englishName))
    }
    var customPriceText by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }

    val catalogTypes = WorkTypeCatalog.getTypesForProfile(profile)
    val daysCount = maxOf(1, selectedDatesSet.size)
    val autoEstimatedPerDay = selectedWorkTypes.sumOf { typeName ->
        catalogTypes.find { it.englishName == typeName }?.defaultEstimate ?: 0.0
    }
    val defaultCalculatedTotal = daysCount * (if (autoEstimatedPerDay > 0) autoEstimatedPerDay else perDayRate)
    val calculatedTotal = customPriceText.toDoubleOrNull() ?: defaultCalculatedTotal
    val selectedWorkTypeSummary = selectedWorkTypes.joinToString(" + ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Studio Branding Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("customer_hero_card"),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(StudioSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Studio Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(60.dp).clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studio.studioName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLightPrimary
                        )
                        Text(
                            text = studio.photographerName,
                            fontSize = 13.sp,
                            color = StudioAmber,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "📍 ${studio.city}",
                            fontSize = 12.sp,
                            color = TextLightSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = studio.bio,
                    fontSize = 12.sp,
                    color = TextLightSecondary,
                    lineHeight = 16.sp
                )

                if (studio.cameraGearDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📸 ${studio.cameraGearDetails}",
                        fontSize = 11.sp,
                        color = StudioAmber.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Direct Contact buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onCallDirect(studio.phone) },
                        modifier = Modifier.weight(1f).testTag("customer_call_owner"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Studio", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val msg = "Hello ${studio.photographerName}, I would like to inquire about booking a photoshoot with ${studio.studioName}."
                            onWhatsAppDirect(studio.whatsapp, msg)
                        },
                        modifier = Modifier.weight(1f).testTag("customer_whatsapp_owner"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("💬 WhatsApp", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Pricing Rate Banner
        Surface(
            color = StudioAmber.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "💎 Standard Daily Package Rate",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )
                    Text(
                        text = "4K Cinematic Video • High-Res Photos • Drone",
                        fontSize = 11.sp,
                        color = TextLightSecondary
                    )
                }
                Text(
                    text = "₹${perDayRate.toLong()} / day",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextLightPrimary
                )
            }
        }

        // Calendar Section
        Column {
            Text(
                text = "📅 Check Availability & Select Your Date(s)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextLightPrimary
            )
            Text(
                text = "Green = Available (FREE) • Red = Already Booked",
                fontSize = 12.sp,
                color = TextLightSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            StudioCalendarView(
                year = year,
                monthZeroIndexed = monthZeroIndexed,
                dateSlots = dateSlots,
                bookings = bookings,
                selectedDate = null,
                selectedDates = selectedDatesSet,
                isOwnerMode = false,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDateClick = { dateIso, slot, _ ->
                    if (slot?.status == DateStatus.BOOKED) {
                        Toast.makeText(context, "This date is already booked! Please select another date.", Toast.LENGTH_SHORT).show()
                    } else {
                        val current = selectedDatesSet.toMutableSet()
                        if (current.contains(dateIso)) {
                            current.remove(dateIso)
                        } else {
                            current.add(dateIso)
                        }
                        selectedDatesSet = current
                        onDateSelected(dateIso)
                    }
                }
            )
        }

        // Booking Inquiry Form Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("customer_inquiry_form"),
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📝 Booking Inquiry Form",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioAmber
                )

                // Selected dates display & package calculation
                if (selectedDatesSet.isNotEmpty()) {
                    Surface(
                        color = StudioSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Selected Dates (${selectedDatesSet.size} Day${if (selectedDatesSet.size > 1) "s" else ""}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioAmber
                            )
                            Text(
                                text = selectedDatesSet.sorted().joinToString(", ") { DateUtils.formatForDisplay(it) },
                                fontSize = 12.sp,
                                color = TextLightPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estimated Package: ₹${calculatedTotal.toLong()} (₹${perDayRate.toLong()} × $daysCount days)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusFreeGreen
                            )
                        }
                    }
                } else {
                    Text(
                        text = "👉 Please tap one or more dates on the calendar above to begin.",
                        fontSize = 12.sp,
                        color = StudioAmber
                    )
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Your Full Name / पूरा नाम") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StudioAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Mobile Number / मोबाइल नंबर") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StudioAmber) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_phone"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                // Crew / Service Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Crew / फोटोग्राफी टीम (Multiple allowed)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLightSecondary
                    )
                    Text(
                        text = "₹${autoEstimatedPerDay.toInt()}/day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (workType in catalogTypes) {
                        val isSelected = selectedWorkTypes.contains(workType.englishName)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) StudioAmber else StudioSurface,
                            modifier = Modifier.clickable {
                                val current = selectedWorkTypes.toMutableSet()
                                if (current.contains(workType.englishName)) {
                                    if (current.size > 1) current.remove(workType.englishName)
                                } else {
                                    current.add(workType.englishName)
                                }
                                selectedWorkTypes = current
                            }
                        ) {
                            Text(
                                text = "${workType.iconEmoji} ${workType.englishName} • ₹${workType.defaultEstimate.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextLightPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customPriceText,
                    onValueChange = { customPriceText = it },
                    label = { Text("Set Custom Price (₹) / कस्टम पैकेज रेट [Default: ₹${defaultCalculatedTotal.toInt()}]") },
                    leadingIcon = { Text("₹", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_custom_price"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("Venue / Event Location / स्थान (Venue)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = StudioAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_location"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    label = { Text("Special Requirements / विशेष आवश्यकता (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = StudioAmber) },
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_notes"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioAmber,
                        cursorColor = StudioAmber
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (selectedDatesSet.isEmpty()) {
                            Toast.makeText(context, "Please select at least one date from the calendar!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (customerName.isBlank()) {
                            Toast.makeText(context, "Please enter your name!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (customerPhone.isBlank() || customerPhone.length < 10) {
                            Toast.makeText(context, "Please enter a valid 10-digit mobile number!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onSubmitBooking(
                            selectedDatesSet.sorted(),
                            customerName.trim(),
                            customerPhone.trim(),
                            selectedWorkTypeSummary,
                            calculatedTotal,
                            eventLocation.trim(),
                            specialNotes.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_customer_booking"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Booking Request",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Booking Success Dialog
    if (bookingSuccessDialog) {
        CustomerSuccessDialog(
            studioName = studio.studioName,
            photographerPhone = studio.phone,
            photographerWhatsApp = studio.whatsapp,
            customerName = customerName,
            onDismiss = onDismissSuccess,
            onWhatsApp = {
                val datesSummary = selectedDatesSet.sorted().joinToString(", ") { DateUtils.formatForDisplay(it) }
                val msg = "Hello ${studio.photographerName}, I ($customerName) just submitted a booking request for $datesSummary ($selectedWorkTypeSummary) with ${studio.studioName}!"
                onWhatsAppDirect(studio.whatsapp, msg)
            }
        )
    }
}

@Composable
private fun CustomerSuccessDialog(
    studioName: String,
    photographerPhone: String,
    photographerWhatsApp: String,
    customerName: String,
    onDismiss: () -> Unit,
    onWhatsApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusFreeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Booking Request Sent!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Thank you $customerName! Your booking inquiry has been received by $studioName.",
                    fontSize = 13.sp,
                    color = TextLightPrimary
                )
                Text(
                    text = "The photographer will review the request and get back to you shortly. You can also chat directly on WhatsApp for instant confirmation.",
                    fontSize = 12.sp,
                    color = TextLightSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onWhatsApp()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen)
            ) {
                Text("Chat on WhatsApp", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = TextLightSecondary)
            }
        },
        containerColor = StudioCard,
        shape = RoundedCornerShape(16.dp)
    )
}
