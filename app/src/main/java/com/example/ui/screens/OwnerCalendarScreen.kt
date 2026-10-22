package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingRequest
import com.example.data.model.StudioDateSlot
import com.example.data.model.StudioProfile
import com.example.ui.components.StudioCalendarView
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@Composable
fun OwnerCalendarScreen(
    year: Int,
    monthZeroIndexed: Int,
    dateSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    selectedDates: Set<String>,
    profile: StudioProfile?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateToggle: (String) -> Unit,
    onClearAllSelectedDates: () -> Unit,
    onSetFreeDates: (Set<String>) -> Unit,
    onSetBookedDates: (Set<String>) -> Unit,
    onSetRequestDates: (Set<String>) -> Unit,
    onClearDates: (Set<String>) -> Unit,
    onDirectBookingDone: (List<String>, String, String, String, Double) -> Unit,
    onCallCustomer: (String) -> Unit,
    onWhatsAppCustomer: (String, String) -> Unit,
    onShareWhatsAppClick: () -> Unit,
    onMessageTemplateClick: () -> Unit = {},
    onCloseDatePanel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📅 Master Studio Calendar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLightPrimary
                )
                Text(
                    text = "Manage dates & booking availability",
                    fontSize = 12.sp,
                    color = TextLightSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShareWhatsAppClick,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFreeGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("share_whatsapp_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share FREE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        // Quick tip banner
        Surface(
            color = StudioCard,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡 ", fontSize = 16.sp)
                Text(
                    text = "Tap any date to select. Multi-select consecutive dates to calculate wedding packages automatically at ₹${(profile?.perDayRate ?: 25000.0).toLong()}/day.",
                    fontSize = 11.sp,
                    color = TextLightSecondary
                )
            }
        }

        // Calendar component
        StudioCalendarView(
            year = year,
            monthZeroIndexed = monthZeroIndexed,
            dateSlots = dateSlots,
            bookings = bookings,
            selectedDate = null,
            selectedDates = selectedDates,
            isOwnerMode = true,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onDateClick = { dateIso, _, _ ->
                onDateToggle(dateIso)
            }
        )

        // Date control panel when dates are selected
        if (selectedDates.isNotEmpty()) {
            DirectDateControlPanel(
                selectedDates = selectedDates,
                allSlots = dateSlots,
                bookings = bookings,
                profile = profile,
                onToggleDate = onDateToggle,
                onClearAllSelectedDates = onClearAllSelectedDates,
                onSetFreeDates = onSetFreeDates,
                onSetBookedDates = onSetBookedDates,
                onSetRequestDates = onSetRequestDates,
                onClearDates = onClearDates,
                onDirectBookingDone = onDirectBookingDone,
                onCallCustomer = onCallCustomer,
                onWhatsAppCustomer = onWhatsAppCustomer,
                onClose = onCloseDatePanel
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
