package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
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

    val catalogTypes = remember(profile) { WorkTypeCatalog.getTypesForProfile(profile) }

    // Direct booking form states - immediately visible
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    // Crew selection: multi-crew per date or same crew
    var differentCrewPerDate by remember(sortedDates) { mutableStateOf(sortedDates.size > 1) }

    var selectedGlobalCrew by remember { mutableStateOf(catalogTypes.first().englishName) }

    var dateSpecificCrewMap by remember(sortedDates) {
        mutableStateOf(sortedDates.associateWith { catalogTypes.first().englishName })
    }

    var customAmount by remember { mutableDoubleStateOf(0.0) }
    var amountText by remember { mutableStateOf("0") }

    // Recalculate amount helper
    fun recalculate(dates: List<String>, isDateWise: Boolean, crewMap: Map<String, String>, globalCrew: String) {
        val calculated = if (dates.size > 1 && isDateWise) {
            dates.sumOf { d ->
                val role = crewMap[d] ?: globalCrew
                catalogTypes.find { it.englishName == role }?.defaultEstimate ?: 10000.0
            }
        } else {
            val role = catalogTypes.find { it.englishName == globalCrew } ?: catalogTypes.first()
            dates.size * role.defaultEstimate
        }
        customAmount = calculated
        amountText = calculated.toLong().toString()
    }

    // Initial and reactive calculation on date or profile changes
    LaunchedEffect(sortedDates, catalogTypes, differentCrewPerDate) {
        recalculate(sortedDates, differentCrewPerDate, dateSpecificCrewMap, selectedGlobalCrew)
    }

    // Check if single selected date has existing booking
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
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Calculated Package: ₹$amountText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioAmber
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        color = StudioAmber,
                        modifier = Modifier
                            .clickable { onClearAllSelectedDates() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                modifier = Modifier.fillMaxWidth(),
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

            // Quick Status Actions (Free, Booked, Request, Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSetFreeDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_free"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🟢 Free", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Button(
                    onClick = { onSetBookedDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_booked"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBookedRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔴 Booked", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onSetRequestDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_request"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRequestYellow),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🟡 Req", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                OutlinedButton(
                    onClick = { onClearDates(selectedDates) },
                    modifier = Modifier.weight(1f).testTag("action_mark_clear"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("⚪ Reset", fontSize = 11.sp, color = TextLightPrimary)
                }
            }

            // Existing Booking info if single date
            if (singleBooking != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCallCustomer(singleBooking.customerPhone) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
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
                                Text("💬 WhatsApp", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // DIRECT CREW SELECTION & BOOKING SECTION (Immediately visible beneath dates as requested)
            Surface(
                color = StudioSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 Select Crew Roles / क्रू चुनें",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber
                        )
                        if (sortedDates.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (differentCrewPerDate) StudioAmber else StudioCard,
                                modifier = Modifier.clickable {
                                    differentCrewPerDate = !differentCrewPerDate
                                    recalculate(sortedDates, differentCrewPerDate, dateSpecificCrewMap, selectedGlobalCrew)
                                }
                            ) {
                                Text(
                                    text = if (differentCrewPerDate) "Date-wise ON" else "Same Crew",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (differentCrewPerDate) Color.Black else TextLightPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (sortedDates.size > 1 && differentCrewPerDate) {
                        // Date-wise different crew selection for each date
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sortedDates.forEach { dateStr ->
                                val selectedForThisDate = dateSpecificCrewMap[dateStr] ?: catalogTypes.first().englishName
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "📅 ${DateUtils.formatForDisplay(dateStr)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = StudioAmber
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            for (workType in catalogTypes) {
                                                val isSelected = selectedForThisDate == workType.englishName
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) StudioAmber else StudioSurface,
                                                    modifier = Modifier.clickable {
                                                        val updatedMap = dateSpecificCrewMap.toMutableMap()
                                                        updatedMap[dateStr] = workType.englishName
                                                        dateSpecificCrewMap = updatedMap
                                                        recalculate(sortedDates, differentCrewPerDate, updatedMap, selectedGlobalCrew)
                                                    }
                                                ) {
                                                    Text(
                                                        text = "${workType.iconEmoji} ${workType.englishName} • ₹${workType.defaultEstimate.toInt()}",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.Black else TextLightPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Same Crew Selection Chips across all 7 types
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (workType in catalogTypes) {
                                val isSelected = selectedGlobalCrew == workType.englishName || selectedGlobalCrew == workType.displayName
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) StudioAmber else StudioCard,
                                    modifier = Modifier.clickable {
                                        selectedGlobalCrew = workType.englishName
                                        recalculate(sortedDates, differentCrewPerDate, dateSpecificCrewMap, workType.englishName)
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
                    }

                    // Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / ग्राहक का नाम") },
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

                    // Customer Mobile
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Mobile / मोबाइल नंबर") },
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

                    // Total Package Amount (pre-filled with calculated rate, and editable)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            customAmount = it.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text("Total Package Amount (₹) / कुल पैकेज राशि") },
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

                    // Confirm Booking Button
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                                val finalWorkSummary = if (sortedDates.size > 1 && differentCrewPerDate) {
                                    sortedDates.joinToString(" | ") { date ->
                                        "$date: ${dateSpecificCrewMap[date] ?: selectedGlobalCrew}"
                                    }
                                } else {
                                    selectedGlobalCrew
                                }

                                onDirectBookingDone(
                                    sortedDates,
                                    customerName.trim(),
                                    customerPhone.trim(),
                                    finalWorkSummary,
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
                            text = "Confirm Booking for $daysCount Day(s) / $daysCount दिनों के लिए बुकिंग कन्फर्म करें",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
