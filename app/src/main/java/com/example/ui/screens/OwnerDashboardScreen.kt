package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusRequestYellow
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
    onSetMessageTemplateClick: () -> Unit,
    onConfirmBooking: (BookingRequest) -> Unit,
    onOpenBookingDetail: (BookingRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val freeCount = dateSlots.count { it.status == DateStatus.FREE }
    val bookedCount = dateSlots.count { it.status == DateStatus.BOOKED }

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
                                    text = "₹${profile?.perDayRate?.toInt() ?: 25000}/Day",
                                    fontSize = 12.sp,
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

            // Pending Inquiries section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pending Inquiries (${pendingRequests.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (pendingRequests.isNotEmpty()) {
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            color = StudioAmber,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateRequests() }
                        )
                    }
                }
            }

            if (pendingRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "No pending inquiries. All caught up!\nकोई लंबित अनुरोध नहीं है।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(pendingRequests.take(4)) { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenBookingDetail(booking) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = booking.customerName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${booking.workType} • 📅 ${DateUtils.formatForDisplay(booking.dateStr)}",
                                    fontSize = 12.sp,
                                    color = StudioAmber
                                )
                                if (booking.requestedAmount > 0) {
                                    Text(
                                        text = "₹${booking.requestedAmount.toInt()}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusFreeGreen
                                    )
                                }
                            }
                            Button(
                                onClick = { onConfirmBooking(booking) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen)
                            ) {
                                Text("Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
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
