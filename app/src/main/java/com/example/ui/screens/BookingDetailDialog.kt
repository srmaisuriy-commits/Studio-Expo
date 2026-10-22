package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber

@Composable
fun BookingDetailDialog(
    booking: BookingRequest,
    onDismiss: () -> Unit,
    onConfirm: (BookingRequest) -> Unit,
    onReject: (BookingRequest) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Booking Details / बुकिंग विवरण",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Customer: ${booking.customerName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Phone: ${booking.customerPhone}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Date: ${DateUtils.formatForDisplay(booking.dateStr)} (${DateUtils.getDayOfWeek(booking.dateStr)})",
                    fontSize = 14.sp,
                    color = StudioAmber,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Event: ${booking.workType}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (booking.requestedAmount > 0) {
                    Text(
                        text = "Estimated Amount: ₹${booking.requestedAmount.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusFreeGreen
                    )
                }

                if (booking.location.isNotBlank()) {
                    Text(
                        text = "Venue: ${booking.location}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (booking.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${booking.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp / Call customer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val msg = "Hello ${booking.customerName}, regarding your booking request for ${booking.workType} on ${booking.dateStr} with Studio Expo."
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${booking.customerPhone}?text=${Uri.encode(msg)}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerPhone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (booking.status == BookingStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReject(booking) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject / अस्वीकार", color = StatusBookedRed, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onConfirm(booking) },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm 🔴", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
