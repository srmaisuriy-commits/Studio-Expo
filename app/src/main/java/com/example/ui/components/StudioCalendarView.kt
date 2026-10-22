package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalendarDay
import com.example.data.local.DateUtils
import com.example.data.model.BookingRequest
import com.example.data.model.BookingStatus
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusRequestYellow
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextLightPrimary
import com.example.ui.theme.TextLightSecondary

@Composable
fun StudioCalendarView(
    year: Int,
    monthZeroIndexed: Int,
    dateSlots: List<StudioDateSlot>,
    bookings: List<BookingRequest>,
    selectedDate: String?,
    selectedDates: Set<String> = emptySet(),
    isOwnerMode: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (String, StudioDateSlot?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthYearTitle = DateUtils.getMonthYearDisplay(year, monthZeroIndexed)
    val days = DateUtils.getDaysInMonth(year, monthZeroIndexed)

    // Calculate month stats
    val slotMap = dateSlots.associateBy { it.dateStr }
    val pendingByDate = bookings.filter { it.status == BookingStatus.PENDING }
        .groupBy { it.dateStr }

    var freeCount = 0
    var bookedCount = 0
    var requestCount = 0

    for (day in days) {
        if (!day.isCurrentMonth) continue
        val slot = slotMap[day.dateIso]
        val pending = pendingByDate[day.dateIso]?.size ?: 0
        if (slot?.status == DateStatus.FREE) freeCount++
        if (slot?.status == DateStatus.BOOKED) bookedCount++
        if (pending > 0 || slot?.status == DateStatus.REQUEST) requestCount++
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_calendar_card"),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("cal_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = StudioAmber
                    )
                }

                Text(
                    text = monthYearTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLightPrimary
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("cal_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = StudioAmber
                    )
                }
            }

            // Stats summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CountingItem(
                    emoji = "🟢",
                    title = "Available",
                    hindiTitle = "उपलब्ध",
                    count = freeCount,
                    color = StatusFreeGreen
                )
                CountingItem(
                    emoji = "🔴",
                    title = "Booked",
                    hindiTitle = "बुक",
                    count = bookedCount,
                    color = StatusBookedRed
                )
                CountingItem(
                    emoji = "🟡",
                    title = "Requests",
                    hindiTitle = "अनुरोध",
                    count = requestCount,
                    color = StatusRequestYellow
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday labels
            val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dayName == "Sun") StudioAmber else TextLightSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days grid (chunks of 7)
            val rows = days.chunked(7)
            for (row in rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (day in row) {
                        if (!day.isCurrentMonth || day.day == 0) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val slot = slotMap[day.dateIso]
                            val isFree = slot?.status == DateStatus.FREE
                            val isBooked = slot?.status == DateStatus.BOOKED
                            val pendingList = pendingByDate[day.dateIso]
                            val pendingCount = pendingList?.size ?: 0
                            val isRequest = pendingCount > 0 || slot?.status == DateStatus.REQUEST

                            val isSelected = if (isOwnerMode && selectedDates.isNotEmpty()) {
                                selectedDates.contains(day.dateIso)
                            } else {
                                selectedDate == day.dateIso
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                CalendarDayCell(
                                    day = day,
                                    slot = slot,
                                    isFree = isFree,
                                    isBooked = isBooked,
                                    isRequest = isRequest,
                                    pendingCount = pendingCount,
                                    isSelected = isSelected,
                                    isOwnerMode = isOwnerMode,
                                    onClick = {
                                        onDateClick(day.dateIso, slot, isSelected)
                                    }
                                )
                            }
                        }
                    }
                    // Fill remaining empty cells in row
                    if (row.size < 7) {
                        for (i in row.size until 7) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountingItem(
    emoji: String,
    title: String,
    hindiTitle: String,
    count: Int,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$emoji ", fontSize = 11.sp)
            Column {
                Text(
                    text = "$count $title",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    slot: StudioDateSlot?,
    isFree: Boolean,
    isBooked: Boolean,
    isRequest: Boolean,
    pendingCount: Int,
    isSelected: Boolean,
    isOwnerMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> StudioAmber.copy(alpha = 0.35f)
        isBooked -> StatusBookedRed.copy(alpha = 0.22f)
        isFree -> StatusFreeGreen.copy(alpha = 0.22f)
        isRequest -> StatusRequestYellow.copy(alpha = 0.22f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> StudioAmber
        isBooked -> StatusBookedRed.copy(alpha = 0.7f)
        isFree -> StatusFreeGreen.copy(alpha = 0.7f)
        isRequest -> StatusRequestYellow.copy(alpha = 0.7f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .testTag("day_cell_${day.dateIso}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isFree || isBooked || isRequest) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> StudioAmber
                    isBooked -> StatusBookedRed
                    isFree -> StatusFreeGreen
                    isRequest -> StatusRequestYellow
                    else -> TextLightPrimary
                }
            )

            // Status indicator dot or badge
            when {
                isSelected && isOwnerMode -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(10.dp),
                        tint = StudioAmber
                    )
                }
                isBooked -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusBookedRed)
                    )
                }
                isFree -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusFreeGreen)
                    )
                }
                isRequest -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusRequestYellow)
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}
