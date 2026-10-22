package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusRequestYellow
import com.example.ui.theme.StudioAmber

@Composable
fun OwnerBookingRequestsScreen(
    bookings: List<BookingRequest>,
    onConfirmBooking: (BookingRequest) -> Unit,
    onRejectBooking: (BookingRequest) -> Unit,
    onOpenBookingDetail: (BookingRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Confirmed, 2: Rejected

    val filteredList = when (selectedTab) {
        0 -> bookings.filter { it.status == BookingStatus.PENDING }
        1 -> bookings.filter { it.status == BookingStatus.CONFIRMED }
        else -> bookings.filter { it.status == BookingStatus.REJECTED }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending 🟡 (${bookings.count { it.status == BookingStatus.PENDING }})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Confirmed 🔴", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Rejected ⚪", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No bookings in this tab / कोई बुकिंग नहीं मिली",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { booking ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenBookingDetail(booking) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = booking.customerName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "📞 ${booking.customerPhone}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "📅 ${DateUtils.formatForDisplay(booking.dateStr)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAmber
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Event: ${booking.workType}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (booking.requestedAmount > 0) {
                                    Text(
                                        text = "Package: ₹${booking.requestedAmount.toInt()}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusFreeGreen
                                    )
                                }

                                if (booking.status == BookingStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onRejectBooking(booking) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject", color = StatusBookedRed, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { onConfirmBooking(booking) },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Confirm 🔴", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
