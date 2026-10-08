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
    dateIso: String,
    slot: StudioDateSlot?,
    bookings: List<BookingRequest>,
    allSlots: List<StudioDateSlot>,
    onSetFreeDate: (String) -> Unit,
    onSetBookedDate: (String) -> Unit,
    onSetRequestDate: (String) -> Unit,
    onClearDate: (String) -> Unit,
    onDirectBookingDone: (date: String, customerName: String, customerPhone: String, workType: String, amount: Double) -> Unit,
    onCallCustomer: (String) -> Unit,
    onWhatsAppCustomer: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBookingForm by remember { mutableStateOf(false) }

    // Direct Booking Form fields
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedWorkType by remember { mutableStateOf(WorkTypeCatalog.allTypes.first()) }
    var amountText by remember { mutableStateOf(selectedWorkType.defaultEstimate.toInt().toString()) }

    val currentStatus = slot?.status ?: "NOT_SET"
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
            // Header: Select Date & Date Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatFullForDisplay(dateIso),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = currentStatus)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Label
            Text(
                text = "તારીખ સ્ટેટસ પસંદ કરો (Select Date Option):",
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
                        onSetFreeDate(dateIso)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFree) StatusFreeGreen else StatusFreeGreen.copy(alpha = 0.85f)
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
                            text = "FREE DATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 🔴 BOOKED DATE Button
                Button(
                    onClick = {
                        onSetBookedDate(dateIso)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBooked) StatusBookedRed else StatusBookedRed.copy(alpha = 0.85f)
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
                            text = "BOOKED DATE",
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
                        onSetRequestDate(dateIso)
                        showBookingForm = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRequest) StatusPendingAmber else StatusPendingAmber.copy(alpha = 0.9f)
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
                            text = "REQUEST DATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ⚪ / 🗑️ CLEAR DATE Button (તારીખ ખાલી કરો / Reset)
                OutlinedButton(
                    onClick = {
                        onClearDate(dateIso)
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
                            text = "CLEAR DATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 📋 BOOKING Section (For FREE date -> Tap -> BOOKING -> Form -> DONE -> FREE 🟢 -> BOOKED 🔴)
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
                        text = "📋 BOOKING (FREE 🟢 → BOOKED 🔴)",
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
                            text = "📋 Direct Booking Details",
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
                        label = { Text("Customer Name (ગ્રાહકનું નામ) *") },
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
                        label = { Text("Customer Phone (મોબાઇલ નંબર)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_booking_phone"),
                        singleLine = true
                    )

                    // Exposure / Work Type
                    Text(
                        text = "Exposure / Work Type:",
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
                                        amountText = type.defaultEstimate.toInt().toString()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${type.iconEmoji} ${type.englishName.split(" ")[0]}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount / રકમ (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_booking_amount"),
                        singleLine = true
                    )

                    // DONE Button -> FREE 🟢 -> BOOKED 🔴
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: selectedWorkType.defaultEstimate
                            onDirectBookingDone(
                                dateIso,
                                customerName,
                                customerPhone,
                                selectedWorkType.englishName,
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
                            text = "DONE (Mark as BOOKED 🔴)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIVE COUNTING ROW RIGHT BELOW IT
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

            // Existing Bookings for this date
            if (bookings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Customer Bookings on this Date:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                bookings.forEach { req ->
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
                                text = req.customerName,
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
                                            "Hello ${req.customerName}, regarding your shoot on $dateIso..."
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
