package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.DateStatus
import com.example.data.model.StudioDateSlot
import com.example.ui.theme.StatusBookedRed
import com.example.ui.theme.StatusFreeGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StudioAmber

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
    onDateClick: (String, StudioDateSlot?, Boolean) -> Unit, // iso, slot, isFree
    modifier: Modifier = Modifier
) {
    val days = remember(year, monthZeroIndexed) {
        DateUtils.generateMonthDays(year, monthZeroIndexed)
    }

    val slotsByDate = remember(dateSlots) {
        dateSlots.associateBy { it.dateStr }
    }

    val pendingCountByDate = remember(bookings) {
        bookings.filter { it.status == "PENDING" }.groupingBy { it.dateStr }.eachCount()
    }

    // Live counts
    val freeCount = remember(dateSlots) {
        dateSlots.count { it.status == DateStatus.FREE.name }
    }
    val bookedCount = remember(dateSlots) {
        dateSlots.count { it.status == DateStatus.BOOKED.name }
    }
    val requestCount = remember(dateSlots, bookings) {
        val slotRequests = dateSlots.count { it.status == DateStatus.REQUEST.name }
        val pendingBookingsCount = bookings.count { it.status == "PENDING" }
        maxOf(slotRequests, pendingBookingsCount)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header with Nav Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("cal_prev_month_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DateUtils.getMonthYearEnLabel(year, monthZeroIndexed),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.getMonthYearLabel(year, monthZeroIndexed).split(" ")[0],
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("cal_next_month_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of Week Header
            val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dayName == "Sun") StatusBookedRed.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { day ->
                        val slot = slotsByDate[day.isoDate]
                        val pendingCount = pendingCountByDate[day.isoDate] ?: 0
                        val isSelected = if (selectedDates.isNotEmpty()) {
                            selectedDates.contains(day.isoDate)
                        } else {
                            day.isoDate == selectedDate
                        }
                        val isFree = slot?.status == DateStatus.FREE.name
                        val isBooked = slot?.status == DateStatus.BOOKED.name
                        val isRequest = slot?.status == DateStatus.REQUEST.name || (pendingCount > 0)

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
                                onDateClick(day.isoDate, slot, isFree)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LIVE COUNTING ROW: FREE DATE, BOOKED, REQUEST COUNTING RIGHT BELOW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountingItem(
                    emoji = "🟢",
                    title = "Free",
                    gujaratiTitle = "ઉપલબ્ધ",
                    count = freeCount,
                    color = StatusFreeGreen
                )

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                )

                CountingItem(
                    emoji = "🔴",
                    title = "Booked",
                    gujaratiTitle = "બુક થયેલ",
                    count = bookedCount,
                    color = StatusBookedRed
                )

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                )

                CountingItem(
                    emoji = "🟡",
                    title = "Request",
                    gujaratiTitle = "વિનંતી",
                    count = requestCount,
                    color = StatusPendingAmber
                )
            }
        }
    }
}

@Composable
private fun CountingItem(
    emoji: String,
    title: String,
    gujaratiTitle: String,
    count: Int,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$title: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = count.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Text(
                text = gujaratiTitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    val isToday = day.isoDate == DateUtils.todayIso()

    val cellBackground = when {
        isSelected -> StudioAmber.copy(alpha = 0.25f)
        isFree -> StatusFreeGreen.copy(alpha = 0.15f)
        isBooked -> StatusBookedRed.copy(alpha = 0.14f)
        isRequest -> StatusPendingAmber.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    val cellBorderModifier = when {
        isSelected -> Modifier.border(2.dp, StudioAmber, RoundedCornerShape(10.dp))
        isFree -> Modifier.border(1.dp, StatusFreeGreen.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
        isBooked -> Modifier.border(1.dp, StatusBookedRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        isRequest -> Modifier.border(1.dp, StatusPendingAmber.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
        isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        else -> Modifier
    }

    val textColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        isFree -> StatusFreeGreen
        isBooked -> StatusBookedRed
        isRequest -> StatusPendingAmber
        day.isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cellBackground)
            .then(cellBorderModifier)
            .clickable(enabled = day.isCurrentMonth && (!day.isPast || isOwnerMode)) {
                onClick()
            }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isFree || isBooked || isRequest || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Status Indicator Dot
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StudioAmber)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (isFree) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusFreeGreen)
                    )
                } else if (isBooked) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusBookedRed)
                    )
                } else if (isRequest) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(StatusPendingAmber)
                    )
                }
            }
        }
    }
}
