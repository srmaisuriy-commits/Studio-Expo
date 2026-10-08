package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StudioAmber

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
    onSetMessageTemplateClick: () -> Unit = {},
    onConfirmBooking: (BookingRequest) -> Unit,
    onOpenBookingDetail: (BookingRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val freeCount = dateSlots.count { it.status == DateStatus.FREE.name }
    val bookedCount = dateSlots.count { it.status == DateStatus.BOOKED.name }
    val pendingCount = pendingRequests.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("owner_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "નમસ્તે, ${profile?.photographerName ?: "Photographer"} 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile?.studioName ?: "Studio Expo"} • ${profile?.city ?: "Gujarat"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Free Dates",
                    count = freeCount.toString(),
                    labelGuj = "🟢 ઉપલબ્ધ",
                    color = StatusFreeGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Booked",
                    count = bookedCount.toString(),
                    labelGuj = "🔴 બુક થયેલ",
                    color = StatusBookedRed,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Pending Req",
                    count = pendingCount.toString(),
                    labelGuj = "🟡 વિનંતીઓ",
                    color = StatusPendingAmber,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateRequests() }
                )
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "ઝડપી ક્રિયાઓ (Quick Actions)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Add Free Date",
                    subtitle = "➕ તારીખ ઉમેરો",
                    icon = Icons.Default.Add,
                    iconBg = StatusFreeGreen,
                    onClick = onAddFreeDateClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_add_free_date")
                )

                QuickActionCard(
                    title = "Share WhatsApp",
                    subtitle = "📲 લિંક મોકલો",
                    icon = Icons.Default.Share,
                    iconBg = StudioAmber,
                    onClick = onShareWhatsAppClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_share_whatsapp")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "View Calendar",
                    subtitle = "📅 કેલેન્ડર",
                    icon = Icons.Default.CalendarMonth,
                    iconBg = Color(0xFF3B82F6),
                    onClick = onNavigateCalendar,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_view_calendar")
                )

                QuickActionCard(
                    title = "Requests ($pendingCount)",
                    subtitle = "🔔 વિગતો જુઓ",
                    icon = Icons.Default.Notifications,
                    iconBg = StatusPendingAmber,
                    onClick = onNavigateRequests,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_view_requests")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            QuickActionCard(
                title = "Set WhatsApp Message & Gear",
                subtitle = "📝 કેમેરા વિગતો અને આખો મેસેજ સેટ કરો",
                icon = Icons.Default.EditNote,
                iconBg = StudioAmber,
                onClick = onSetMessageTemplateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_set_message_template")
            )
        }

        // Pending Booking Requests Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔔 નવી બુકિંગ વિનંતીઓ (Pending Requests)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (pendingRequests.isNotEmpty()) {
                    TextButton(onClick = onNavigateRequests) {
                        Text("બધા જુઓ (${pendingRequests.size})", color = StudioAmber, fontSize = 12.sp)
                    }
                }
            }
        }

        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 હાલમાં કોઈ પેન્ડિંગ વિનંતી નથી!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ગ્રાહકો સાથે WhatsApp પર Free Dates શેર કરો જેથી વિનંતીઓ મળી શકે.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(pendingRequests.take(3)) { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenBookingDetail(request) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = request.customerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = request.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📅 ${DateUtils.formatForDisplay(request.dateStr)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioAmber
                            )
                            Text(
                                text = "₹${request.amount.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusFreeGreen
                            )
                        }

                        Text(
                            text = "📸 ${request.workType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onConfirmBooking(request) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Confirm",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confirm / Done ✅",
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

        // Upcoming Free Dates preview
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🟢 ઉપલબ્ધ તારીખો (Available Slots)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateCalendar) {
                    Text("કેલેન્ડર જુઓ", color = StudioAmber, fontSize = 12.sp)
                }
            }
        }

        val upcomingFree = dateSlots.filter { it.status == DateStatus.FREE.name }.sortedBy { it.dateStr }
        if (upcomingFree.isEmpty()) {
            item {
                Text(
                    text = "હાલમાં કોઈ Free Date સેટ કરેલી નથી. 'Add Free Date' પર ક્લિક કરો.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(upcomingFree.take(4)) { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateUtils.formatFullForDisplay(slot.dateStr),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = slot.note.ifBlank { "Full Day Shoot Available" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = DateStatus.FREE.name)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: String,
    labelGuj: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = labelGuj,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
