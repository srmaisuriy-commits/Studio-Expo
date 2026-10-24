package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DateUtils
import com.example.data.model.BookedCustomerGroup
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
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
fun OwnerDashboardScreen(
    profile: StudioProfile?,
    dateSlots: List<StudioDateSlot>,
    pendingRequests: List<BookingRequest>,
    allBookings: List<BookingRequest>,
    onNavigateCalendar: () -> Unit,
    onNavigateRequests: () -> Unit,
    onAddFreeDateClick: () -> Unit,
    onShareWhatsAppClick: () -> Unit,
    onSetMessageTemplateClick: () -> Unit,
    onConfirmBooking: (BookingRequest) -> Unit,
    onOpenBookingDetail: (BookingRequest) -> Unit,
    onSaveEditedBooking: (
        originalDates: List<String>,
        newDates: List<String>,
        customerName: String,
        customerPhone: String,
        workTypeSummary: String,
        dateCrewMap: Map<String, String>,
        totalAmount: Double
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onCancelBookingDates: (List<String>) -> Unit = {},
    onCallCustomer: (String) -> Unit = {},
    onWhatsAppCustomer: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val freeCount = dateSlots.count { it.status == DateStatus.FREE }
    val bookedCount = dateSlots.count { it.status == DateStatus.BOOKED }

    var editingGroup by remember { mutableStateOf<BookedCustomerGroup?>(null) }

    val bookedDateSlots = remember(dateSlots) {
        dateSlots.filter { it.status == DateStatus.BOOKED }.sortedBy { it.dateStr }
    }

    val bookedGroups: List<BookedCustomerGroup> = remember(bookedDateSlots, allBookings) {
        val groupMap = linkedMapOf<String, MutableList<StudioDateSlot>>()
        bookedDateSlots.forEach { slot ->
            val matchingBooking = allBookings.firstOrNull { it.dateStr == slot.dateStr }
            val name = slot.bookedCustomerName?.takeIf { it.isNotBlank() }
                ?: matchingBooking?.customerName?.takeIf { it.isNotBlank() }
                ?: "Studio Booking"
            val phone = slot.bookedCustomerPhone?.takeIf { it.isNotBlank() }
                ?: matchingBooking?.customerPhone?.takeIf { it.isNotBlank() }
                ?: ""
            val key = "$name|$phone"
            groupMap.getOrPut(key) { mutableListOf() }.add(slot)
        }

        groupMap.map { (key, slots) ->
            val parts = key.split("|")
            val cName = parts[0]
            val cPhone = if (parts.size > 1) parts[1] else ""
            val sortedDates = slots.map { it.dateStr }.distinct().sorted()
            val totalAmt = slots.sumOf { s ->
                if (s.amount > 0) s.amount else {
                    allBookings.firstOrNull { it.dateStr == s.dateStr }?.amount ?: 0.0
                }
            }
            val dateCrewMap = slots.associate { s ->
                val w = s.workType?.takeIf { it.isNotBlank() }
                    ?: allBookings.firstOrNull { it.dateStr == s.dateStr }?.workType
                    ?: "Photography Crew"
                s.dateStr to w
            }
            val distinctCrews = dateCrewMap.values.distinct()
            val summary = if (distinctCrews.size == 1) {
                distinctCrews.first()
            } else {
                sortedDates.joinToString(" | ") { d ->
                    val shortDate = DateUtils.formatForDisplay(d).split(" ").take(2).joinToString(" ")
                    "$shortDate: ${dateCrewMap[d]}"
                }
            }
            BookedCustomerGroup(
                groupKey = key,
                customerName = cName,
                customerPhone = cPhone,
                dates = sortedDates,
                dateCrewMap = dateCrewMap,
                workTypeSummary = summary,
                totalAmount = totalAmt,
                slots = slots
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Owner Greeting Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("owner_greeting_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome, ${profile?.photographerName ?: "Owner"}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${profile?.studioName ?: "Studio Expo"} • ${profile?.city ?: "Ahmedabad"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "7 Crew Rates Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioAmber
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Available / उपलब्ध",
                        count = "$freeCount",
                        color = StatusFreeGreen,
                        icon = "🟢",
                        onClick = onNavigateCalendar,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Booked / बुक",
                        count = "$bookedCount",
                        color = StatusBookedRed,
                        icon = "🔴",
                        onClick = onNavigateCalendar,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Requests / अनुरोध",
                        count = "${pendingRequests.size}",
                        color = StatusRequestYellow,
                        icon = "🟡",
                        onClick = onNavigateRequests,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onShareWhatsAppClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Free Dates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onNavigateCalendar,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage Dates", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Booked Dates Section (Customer Name prominently FIRST, multi-dates grouped, crew shown, edit option with 7 crew list)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Booked Dates / बुक की गई तारीखें (${bookedDateSlots.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View Calendar / कैलेंडर देखें",
                        fontSize = 12.sp,
                        color = StudioAmber,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateCalendar() }
                    )
                }
            }

            if (bookedGroups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "No dates currently booked.\nवर्तमान में कोई तारीख बुक नहीं है।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(bookedGroups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booked_customer_card_${group.customerName.replace(" ", "_")}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. CUSTOMER NAME FIRST (Prominent Header)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "👤 ${group.customerName}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (group.customerPhone.isNotBlank()) {
                                        Text(
                                            text = "📞 ${group.customerPhone}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextLightSecondary
                                        )
                                    }
                                }

                                if (group.totalAmount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = StatusFreeGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "₹${group.totalAmount.toLong()}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = StatusFreeGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // 2. MULTI-DATES SELECTED BY THIS CUSTOMER
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "📅 ${group.dates.size} Day${if (group.dates.size > 1) "s" else ""} Booked / तारीखें:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioAmber
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    group.dates.forEach { dateStr ->
                                        val role = group.dateCrewMap[dateStr]
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StudioSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusBookedRed.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "🔴 ${DateUtils.formatForDisplay(dateStr)}${if (role != null && group.dates.size > 1 && group.dateCrewMap.values.distinct().size > 1) " ($role)" else ""}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextLightPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. CREW ASSIGNED
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioCard,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📸", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Crew: ${group.workTypeSummary}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextLightPrimary
                                    )
                                }
                            }

                            // 4. ACTION BUTTONS: EDIT BOOKING & CREW (with 7 crew list) + CALL / WHATSAPP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { editingGroup = group },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "✏️ Edit Booking & Crew",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                if (group.customerPhone.isNotBlank()) {
                                    IconButton(
                                        onClick = { onCallCustomer(group.customerPhone) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(StudioSurface, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = StatusFreeGreen, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val msg = "Hello ${group.customerName}, regarding your photography booking with ${profile?.studioName ?: "Studio Expo"} on ${group.dates.joinToString(", ")}."
                                            onWhatsAppCustomer(group.customerPhone, msg)
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(StudioSurface, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = "WhatsApp", tint = StatusFreeGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pending requests summary if any
            if (pendingRequests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Customer Requests / नए अनुरोध (${pendingRequests.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Review / देखें",
                            fontSize = 12.sp,
                            color = StudioAmber,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateRequests() }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Edit Booking Dialog with 7 Crew List
    if (editingGroup != null) {
        val group = editingGroup!!
        EditBookingDialog(
            group = group,
            profile = profile,
            onDismiss = { editingGroup = null },
            onSave = { oldDates, newDates, cName, cPhone, workSummary, crewMap, totalAmt ->
                onSaveEditedBooking(oldDates, newDates, cName, cPhone, workSummary, crewMap, totalAmt)
            },
            onCancelBooking = { datesToCancel ->
                onCancelBookingDates(datesToCancel)
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    color: Color,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
