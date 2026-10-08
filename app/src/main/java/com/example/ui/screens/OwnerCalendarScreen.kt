package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.ui.components.StudioCalendarView
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber

@Composable
fun OwnerCalendarScreen(
    year: Int,
    monthZeroIndexed: Int,
    dateSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    selectedDate: String?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelect: (String) -> Unit,
    onSetFreeDate: (String) -> Unit,
    onSetBookedDate: (String) -> Unit,
    onSetRequestDate: (String) -> Unit,
    onClearDate: (String) -> Unit,
    onDirectBookingDone: (date: String, customerName: String, customerPhone: String, workType: String, amount: Double) -> Unit,
    onCallCustomer: (String) -> Unit,
    onWhatsAppCustomer: (String, String) -> Unit,
    onShareWhatsAppClick: () -> Unit,
    onSetMessageTemplateClick: () -> Unit = {},
    onCloseDatePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSlot = dateSlots.find { it.dateStr == selectedDate }
    val bookingsOnSelectedDate = bookings.filter { it.dateStr == selectedDate }

    val activeFreeSlots = dateSlots
        .filter { it.status == DateStatus.FREE.name }
        .sortedBy { it.dateStr }

    val freeDaysSummary = if (activeFreeSlots.isEmpty()) {
        "None"
    } else {
        activeFreeSlots.joinToString(" / ") { slot ->
            val day = slot.dateStr.split("-").lastOrNull() ?: slot.dateStr
            day
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WhatsApp Auto-Sync Live Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusFreeGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE WHATSAPP FREE DATES (${activeFreeSlots.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusFreeGreen,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Auto Sync: $freeDaysSummary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = onSetMessageTemplateClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("cal_set_msg_template_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Set WhatsApp Message Template",
                                    tint = StudioAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Msg ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = onShareWhatsAppClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("cal_share_whatsapp_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share 📲", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 Calendarમાંથી તારીખ બદલતાં જ WhatsApp મેસેજ આપોઆપ update થાય છે.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ONE UNIFIED CALENDAR
        item {
            StudioCalendarView(
                year = year,
                monthZeroIndexed = monthZeroIndexed,
                dateSlots = dateSlots,
                bookings = bookings,
                selectedDate = selectedDate,
                isOwnerMode = true,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDateClick = { isoDate, _, _ ->
                    onDateSelect(isoDate)
                }
            )
        }

        // DIRECT DATE CONTROL ACTION PANEL (Appears whenever any date is tapped)
        item {
            if (selectedDate != null) {
                DirectDateControlPanel(
                    dateIso = selectedDate,
                    slot = selectedSlot,
                    bookings = bookingsOnSelectedDate,
                    allSlots = dateSlots,
                    onSetFreeDate = onSetFreeDate,
                    onSetBookedDate = onSetBookedDate,
                    onSetRequestDate = onSetRequestDate,
                    onClearDate = onClearDate,
                    onDirectBookingDone = onDirectBookingDone,
                    onCallCustomer = onCallCustomer,
                    onWhatsAppCustomer = onWhatsAppCustomer,
                    onClose = onCloseDatePanel
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = StudioAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "👆 કોઈ પણ તારીખ પર Tap કરો",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "🟢 FREE • 🔴 BOOKED • 🟡 REQUEST • 🗑️ CLEAR",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
