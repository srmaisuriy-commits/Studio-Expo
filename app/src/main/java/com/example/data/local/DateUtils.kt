package com.example.data.local

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
    private val fullDayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    private val gujaratiMonths = mapOf(
        0 to "જાન્યુઆરી (January)",
        1 to "ફેબ્રુઆરી (February)",
        2 to "માર્ચ (March)",
        3 to "એપ્રિલ (April)",
        4 to "મે (May)",
        5 to "જૂન (June)",
        6 to "જુલાઇ (July)",
        7 to "ઓગસ્ટ (August)",
        8 to "સપ્ટેમ્બર (September)",
        9 to "ઓક્ટોબર (October)",
        10 to "નવેમ્બર (November)",
        11 to "ડિસેમ્બર (December)"
    )

    fun todayIso(): String {
        return isoFormat.format(Date())
    }

    fun formatToIso(calendar: Calendar): String {
        return isoFormat.format(calendar.time)
    }

    fun parseIso(isoStr: String): Date? {
        return try {
            isoFormat.parse(isoStr)
        } catch (e: Exception) {
            null
        }
    }

    fun formatForDisplay(isoStr: String): String {
        val date = parseIso(isoStr) ?: return isoStr
        return displayFormat.format(date)
    }

    fun formatFullForDisplay(isoStr: String): String {
        val date = parseIso(isoStr) ?: return isoStr
        return fullDayFormat.format(date)
    }

    fun getDayOfWeek(isoStr: String): String {
        val date = parseIso(isoStr) ?: return ""
        return dayOfWeekFormat.format(date)
    }

    fun getMonthYearLabel(year: Int, monthZeroIndexed: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroIndexed)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val enLabel = monthYearFormat.format(cal.time)
        val gujMonth = gujaratiMonths[monthZeroIndexed] ?: ""
        return "$gujMonth $year"
    }

    fun getMonthYearEnLabel(year: Int, monthZeroIndexed: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroIndexed)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.format(cal.time)
    }

    /**
     * Generates a 35 or 42 grid of calendar days for a given year and month (0-11).
     * Returns list of CalendarDay objects.
     */
    fun generateMonthDays(year: Int, monthZeroIndexed: Int): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroIndexed)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 1 = Sunday, 2 = Monday, ... 7 = Saturday
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Adjust for Sunday = 0 padding, or Monday = 0 padding. Let's use Sunday first (0 to 6)
        val leadingPaddingDays = firstDayOfWeek - 1

        // Add padding from previous month
        val prevCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -leadingPaddingDays)
        }
        for (i in 0 until leadingPaddingDays) {
            val iso = isoFormat.format(prevCal.time)
            days.add(
                CalendarDay(
                    isoDate = iso,
                    dayOfMonth = prevCal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = false,
                    isPast = isDateInPast(prevCal)
                )
            )
            prevCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Add current month days
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val iso = isoFormat.format(cal.time)
            days.add(
                CalendarDay(
                    isoDate = iso,
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isPast = isDateInPast(cal)
                )
            )
        }

        // Add trailing days to complete full weeks (multiple of 7)
        val remaining = (7 - (days.size % 7)) % 7
        val nextCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        for (i in 0 until remaining) {
            val iso = isoFormat.format(nextCal.time)
            days.add(
                CalendarDay(
                    isoDate = iso,
                    dayOfMonth = nextCal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = false,
                    isPast = isDateInPast(nextCal)
                )
            )
            nextCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun isDateInPast(cal: Calendar): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.before(today)
    }

    fun addDaysToToday(daysToAdd: Int): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, daysToAdd)
        }
        return isoFormat.format(cal.time)
    }
}

data class CalendarDay(
    val isoDate: String,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isPast: Boolean
)
