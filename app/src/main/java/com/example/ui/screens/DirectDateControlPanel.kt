package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.WorkTypeCatalog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StudioAmber

@Composable
fun DirectDateControlPanel(
    selectedDates: Set<String>,
    allSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    profile: com.example.data.model.StudioProfile? = null,
    onToggleDate: (String) -> Unit,
    onClearAllSelectedDates: () -> Unit,
    onSetFreeDates: (Set<String>) -> Unit,
    onSetBookedDates: (Set<String>) -> Unit,
    onSetRequestDates: (Set<String>) -> Unit,
    onClearDates: (Set<String>) -> Unit,
    onDirectBookingDone: (dates: List<String>, customerName: String, customerPhone: String, workType: String, amount: Double) -> Unit,
    onCallCustomer: (String) -> Unit,
    onWhatsAppCustomer: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedDates.isEmpty()) return

    val perDayRate = profile?.perDayRate ?: 25000.0
    val daysCount = if (selectedDates.isEmpty()) 1 else selectedDates.size

    val sortedDates = remember(selectedDates) { selectedDates.toList().sorted() }
    val isMulti = selectedDates.size > 1
    val singleDateIso = sortedDates.firstOrNull() ?: ""
    val singleSlot = remember(selectedDates, allSlots) {
        if (!isMulti) allSlots.find { it.dateStr == singleDateIso } else null
    }

    var showBookingForm by remember { mutableStateOf(false) }

    // Direct Booking Form fields
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedWorkType by remember { mutableStateOf(WorkTypeCatalog.allTypes.first()) }
    var amountText by remember(selectedDates.size, perDayRate) {
        val total = perDayRate * daysCount
        mutableStateOf(total.toInt().toString())
    }

    val currentStatus = singleSlot?.status ?: "NOT_SET"
    val isFree = currentStatus == DateStatus.FREE.name
    val isBooked = currentStatus == DateStatus.BOOKED.name
    val isRequest = currentStatus == DateStatus.REQUEST.name

    // Live counts across all dates
    val freeCount = allSlots.count { it.status == DateStatus.FREE.name }
    val bookedCount = allSlots.count { it.status == DateStatus.BOOKED.name }
    val requestCount = allSlots.count { it.status == DateStatus.REQUEST.name }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("direct_date_control_panel"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Multi-Date or Single Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isMulti) "${selectedDates.size} Dates Selected (चयनित तारीखें)" else "Selected Date (चयनित तारीख)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isMulti) "${selectedDates.size} Dates Chosen" else DateUtils.formatFullForDisplay(singleDateIso),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isMulti) {
                        StatusBadge(status = currentStatus)
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        TextButton(
                            onClick = onClearAllSelectedDates,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Clear All (साफ़ करें)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips of Selected Dates
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortedDates) { dateIso ->
                    val slot = allSlots.find { it.dateStr == dateIso }
                    val st = slot?.status
                    val chipBg = when (st) {
                        DateStatus.FREE.name -> StatusFreeGreen.copy(alpha = 0.15f)
                        DateStatus.BOOKED.name -> StatusBookedRed.copy(alpha = 0.15f)
                        DateStatus.REQUEST.name -> StatusPendingAmber.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val chipBorder = when (st) {
                        DateStatus.FREE.name -> StatusFreeGreen
                        DateStatus.BOOKED.name -> StatusBookedRed
                        DateStatus.REQUEST.name -> StatusPendingAmber
                        else -> StudioAmber
                    }
                    val emoji = when (st) {
                        DateStatus.FREE.name -> "🟢"
                        DateStatus.BOOKED.name -> "🔴"
                        DateStatus.REQUEST.name -> "🟡"
                        else -> "⚪"
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                            .clickable { onToggleDate(dateIso) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$emoji ${DateUtils.formatForDisplay(dateIso)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Label
            Text(
                text = if (isMulti) "Apply Action to all ${selectedDates.size} Dates (सभी तारीखों पर लागू करें):"
                else "Select Date Status (तारीख स्थिति चुनें):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: 🟢 FREE DATE & 🔴 BOOKED DATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 🟢 FREE DATE Button
                Button(
                    onClick = {
                        onSetFreeDates(selectedDates)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMulti && isFree) StatusFreeGreen else StatusFreeGreen.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_set_free_date")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🟢", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMulti) "FREE (${selectedDates.size})" else "FREE (उपलब्ध)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 🔴 BOOKED DATE Button
                Button(
                    onClick = {
                        onSetBookedDates(selectedDates)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMulti && isBooked) StatusBookedRed else StatusBookedRed.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_set_booked_date")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔴", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMulti) "BOOKED (${selectedDates.size})" else "BOOKED (बुक)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: 🟡 REQUEST DATE & ⚪ CLEAR DATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 🟡 REQUEST DATE Button
                Button(
                    onClick = {
                        onSetRequestDates(selectedDates)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMulti && isRequest) StatusPendingAmber else StatusPendingAmber.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_set_request_date")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🟡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMulti) "REQUEST (${selectedDates.size})" else "REQUEST (अनुरोध)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ⚪ / 🗑️ CLEAR DATE Button
                OutlinedButton(
                    onClick = {
                        onClearDates(selectedDates)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_clear_date")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMulti) "CLEAR (${selectedDates.size})" else "CLEAR (हटाएं)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 📋 DIRECT BOOKING Section
            if (!showBookingForm) {
                OutlinedButton(
                    onClick = { showBookingForm = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_direct_booking_form")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookOnline,
                        contentDescription = null,
                        tint = StudioAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMulti) "📋 DIRECT BOOKING FOR ${selectedDates.size} DATES (सीधी बुकिंग)"
                        else "📋 DIRECT BOOKING (सीधी बुकिंग: FREE 🟢 → BOOKED 🔴)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Direct Booking Form (Reveals when BOOKING is tapped)
            AnimatedVisibility(visible = showBookingForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMulti) "📋 Booking for ${selectedDates.size} Selected Dates" else "📋 Direct Booking Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { showBookingForm = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name (ग्राहक का नाम) *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = StudioAmber)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_booking_name"),
                        singleLine = true
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone (फ़ोन नंबर)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_booking_phone"),
                        singleLine = true
                    )

                    // Exposure / Work Type
                    Text(
                        text = "Photography / Work Type (फोटोग्राफी का प्रकार):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WorkTypeCatalog.allTypes) { type ->
                            val isSelected = selectedWorkType.id == type.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) StudioAmber else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        selectedWorkType = type
                                        val total = perDayRate * selectedDates.size
                                        amountText = total.toInt().toString()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${type.iconEmoji} ${type.englishName.split(" ")[0]} (${type.hindiName})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Amount / Total Package
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = {
                            Text(
                                if (daysCount > 1) "Total Estimated Package ($daysCount Days × ₹${perDayRate.toInt()}/day) / कुल राशि (₹)"
                                else "Total Estimated Package (1 Day × ₹${perDayRate.toInt()}) / कुल राशि (₹)"
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontWeight = FontWeight.Bold,
                                color = StudioAmber,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_booking_amount"),
                        singleLine = true,
                        supportingText = {
                            Text(
                                text = "💰 Rate: ₹${perDayRate.toInt()} / Day • $daysCount Day(s) = ₹${(perDayRate * daysCount).toInt()} / प्रति दिन दर ₹${perDayRate.toInt()} के आधार पर कुल पैकेज",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioAmber
                            )
                        }
                    )

                    // DONE Button -> Mark as BOOKED 🔴
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: (perDayRate * selectedDates.size)
                            onDirectBookingDone(
                                sortedDates,
                                customerName,
                                customerPhone,
                                selectedWorkType.displayName,
                                amount
                            )
                            showBookingForm = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusBookedRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_direct_booking_done")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMulti) "DONE (Book ${selectedDates.size} Dates 🔴)" else "DONE (Mark as BOOKED 🔴)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIVE COUNTING ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusFreeGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Free: $freeCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusFreeGreen
                    )
                }

                Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusBookedRed))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Booked: $bookedCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusBookedRed
                    )
                }

                Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusPendingAmber))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Request: $requestCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusPendingAmber
                    )
                }
            }

            // Existing Bookings for selected date(s)
            val bookingsOnSelectedDates = bookings.filter { selectedDates.contains(it.dateStr) }
            if (bookingsOnSelectedDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Customer Bookings on Selected Dates (${bookingsOnSelectedDates.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                bookingsOnSelectedDates.forEach { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${req.customerName} • ${DateUtils.formatForDisplay(req.dateStr)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${req.workType} • ₹${req.amount.toInt()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (req.customerPhone.isNotBlank()) {
                                IconButton(
                                    onClick = { onCallCustomer(req.customerPhone) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        onWhatsAppCustomer(
                                            req.customerPhone,
                                            "Hello ${req.customerName}, regarding your shoot on ${DateUtils.formatForDisplay(req.dateStr)}..."
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = "WhatsApp",
                                        tint = StatusFreeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
