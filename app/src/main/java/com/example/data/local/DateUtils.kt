package com.example.data.local

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

    fun formatForDisplay(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getDayOfWeek(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate) ?: return ""
            dayOfWeekFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun getMonthYearDisplay(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.format(cal.time)
    }

    fun toIsoDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, day)
    }

    fun getDaysInMonth(year: Int, month: Int): List<CalendarDay> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<CalendarDay>()

        // Padding before first day of month
        for (i in 1 until firstDayOfWeek) {
            days.add(CalendarDay(day = 0, isCurrentMonth = false, dateIso = ""))
        }

        // Days of current month
        for (d in 1..maxDays) {
            val dateIso = toIsoDate(year, month, d)
            days.add(CalendarDay(day = d, isCurrentMonth = true, dateIso = dateIso))
        }

        return days
    }
}

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val dateIso: String
)
