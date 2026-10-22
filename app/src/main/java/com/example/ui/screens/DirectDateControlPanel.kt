package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.data.model.WorkTypeCatalog
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusRequestYellow
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectDateControlPanel(
    selectedDates: Set<String>,
    allSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    profile: StudioProfile?,
    onToggleDate: (String) -> Unit,
    onClearAllSelectedDates: () -> Unit,
    onSetFreeDates: (Set<String>) -> Unit,
    onSetBookedDates: (Set<String>) -> Unit,
    onSetRequestDates: (Set<String>) -> Unit,
    onClearDates: (Set<String>) -> Unit,
    onDirectBookingDone: (List<String>, String, String, String, Double) -> Unit,
    onCallCustomer: (String) -> Unit,
    onWhatsAppCustomer: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedDates.isEmpty()) return

    val sortedDates = remember(selectedDates) { selectedDates.sorted() }
    val daysCount = sortedDates.size
    val perDayRate = profile?.perDayRate ?: 25000.0

    // Direct booking state
    var showBookingForm by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedWorkType by remember { mutableStateOf("Wedding / लग्न") }
    var customAmount by remember { mutableDoubleStateOf(daysCount * perDayRate) }
    var amountText by remember { mutableStateOf((daysCount * perDayRate).toLong().toString()) }

    // Update calculated rate when date count changes
    LaunchedEffect(daysCount, perDayRate) {
        val calculated = daysCount * perDayRate
        customAmount = calculated
        amountText = calculated.toLong().toString()
    }

    // Check if single selected date has booking
    val singleBooking = if (selectedDates.size == 1) {
        val singleDate = selectedDates.first()
        bookings.firstOrNull { it.dateStr == singleDate }
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("direct_date_control_panel"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 $daysCount Date${if (daysCount > 1) "s" else ""} Selected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLightPrimary
                    )
                    Text(
                        text = "Est. Package: ₹$amountText (₹${perDayRate.toLong()}/day)",
                        fontSize = 12.sp,
                        color = StudioAmber
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Clear",
                        fontSize = 12.sp,
                        color = StudioAmber,
                        modifier = Modifier
                            .clickable { onClearAllSelectedDates() }
                            .padding(8.dp)
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextLightSecondary
                        )
                    }
                }
            }

            // Chips of selected dates
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sortedDates.forEach { dateIso ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatForDisplay(dateIso),
                                fontSize = 11.sp,
                                color = TextLightPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove date",
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { onToggleDate(dateIso) },
                                tint = TextLightSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Status Actions
            Text(
                text = "⚡ Quick Status Actions",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLightSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSetFreeDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_free"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🟢 Free", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Button(
                    onClick = { onSetBookedDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_booked"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBookedRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔴 Booked", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onSetRequestDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_request"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRequestYellow),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🟡 Req", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                OutlinedButton(
                    onClick = { onClearDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_clear"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("⚪ Reset", fontSize = 12.sp, color = TextLightPrimary)
                }
            }

            // Existing Booking info for single date
            if (singleBooking != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📋 Existing Booking: ${singleBooking.customerName}",
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Phone: ${singleBooking.customerPhone} • Type: ${singleBooking.workType}",
                            fontSize = 12.sp,
                            color = TextLightSecondary
                        )
                        Text(
                            text = "Amount: ₹${singleBooking.amount.toLong()} • Status: ${singleBooking.status}",
                            fontSize = 12.sp,
                            color = TextLightPrimary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCallCustomer(singleBooking.customerPhone) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp, color = Color.Black)
                            }
                            Button(
                                onClick = {
                                    val msg = "Hello ${singleBooking.customerName}, regarding your booking on ${singleBooking.dateStr} with ${profile?.studioName ?: "Studio Expo"}..."
                                    onWhatsAppCustomer(singleBooking.customerPhone, msg)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("💬 WhatsApp", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Direct Booking Section
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showBookingForm = !showBookingForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_direct_booking_form"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = StudioAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showBookingForm) "Hide Booking Form" else "📝 Book These Dates with Customer",
                    color = StudioAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = showBookingForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / ગ્રાહકનું નામ") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StudioAmber) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_customer_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Mobile / મોટો નંબર") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = StudioAmber) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_customer_phone"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Work Type Selection Chips
                    Text(
                        text = "Work Type / કામનો પ્રકાર",
                        fontSize = 12.sp,
                        color = TextLightSecondary
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (workType in WorkTypeCatalog.allTypes) {
                            val isSelected = selectedWorkType == workType.displayName
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) StudioAmber else StudioSurface,
                                modifier = Modifier.clickable { selectedWorkType = workType.displayName }
                            ) {
                                Text(
                                    text = "${workType.iconEmoji} ${workType.englishName}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else TextLightPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            customAmount = it.toDoubleOrNull() ?: (daysCount * perDayRate)
                        },
                        label = { Text("Total Package Amount (₹) / કુલ રકમ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_package_amount"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioAmber,
                            cursorColor = StudioAmber
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                onDirectBookingDone(
                                    sortedDates,
                                    customerName.trim(),
                                    customerPhone.trim(),
                                    selectedWorkType,
                                    customAmount
                                )
                                onClearAllSelectedDates()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_direct_booking"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Confirm Booking for $daysCount Days",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
