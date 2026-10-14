package com.example.aigymapp.utils

import kotlinx.datetime.*
import kotlin.math.abs

/**
 * Date and time utility class
 * Helper methods for date operations
 */
object DateUtils {
    // Date format patterns
    const val FORMAT_DATE_DISPLAY: String = "dd MMMM yyyy"
    const val FORMAT_DATE_SHORT: String = "dd/MM/yyyy"
    const val FORMAT_TIME: String = "HH:mm"
    const val FORMAT_DATETIME: String = "dd/MM/yyyy HH:mm"
    const val FORMAT_MONTH_YEAR: String = "MMMM yyyy"
    const val FORMAT_DAY_MONTH: String = "dd MMM"

    // Türkçe ay isimleri
    private val TURKISH_MONTHS = arrayOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    // Türkçe gün isimleri
    private val TURKISH_DAYS = arrayOf(
        "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    )

    /**
     * Formats workout date
     * @param timestamp Date to format (epoch milliseconds)
     * @return Formatted date string
     */
    fun formatWorkoutDate(timestamp: Long?): String {
        if (timestamp == null) return ""

        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return "${dateTime.dayOfMonth.toString().padStart(2, '0')} ${TURKISH_MONTHS[dateTime.monthNumber - 1]} ${dateTime.year}"
    }

    /**
     * Formats time (HH:mm)
     * @param timestamp Date to format (epoch milliseconds)
     * @return Formatted time string
     */
    fun formatTime(timestamp: Long?): String {
        if (timestamp == null) return ""

        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * Formats date in short format
     * @param timestamp Date to format (epoch milliseconds)
     * @return Formatted date (dd/MM/yyyy)
     */
    fun formatDateShort(timestamp: Long?): String {
        if (timestamp == null) return ""

        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return "${dateTime.dayOfMonth.toString().padStart(2, '0')}/${dateTime.monthNumber.toString().padStart(2, '0')}/${dateTime.year}"
    }

    /**
     * Returns the start and end times of the day
     * @param timestamp Target date (epoch milliseconds)
     * @return {start, end} dates (epoch milliseconds)
     */
    fun getWeekStartEnd(timestamp: Long): Array<Long> {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Start of the week (Monday)
        val dayOfWeek = date.dayOfWeek.ordinal // 0=Monday, 6=Sunday
        val weekStart = date.minus(dayOfWeek, DateTimeUnit.DAY)
        val weekStartTimestamp = weekStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        // End of the week (Sunday)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
        val weekEndTimestamp = weekEnd.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return arrayOf(weekStartTimestamp, weekEndTimestamp)
    }

    /**
     * Calculates the number of days between two dates
     * @param startTimestamp Start date (epoch milliseconds)
     * @param endTimestamp End date (epoch milliseconds)
     * @return Number of days
     */
    fun calculateDaysBetween(startTimestamp: Long?, endTimestamp: Long?): Long {
        if (startTimestamp == null || endTimestamp == null) return 0

        val diffInMillis = abs(endTimestamp - startTimestamp)
        return diffInMillis / (24 * 60 * 60 * 1000) // Convert milliseconds to days
    }

    /**
     * Date range for filtering this week's workouts
     * @return {week start, week end} dates (epoch milliseconds)
     */
    val currentWeekWorkouts: Array<Long>
        get() = getWeekStartEnd(Clock.System.now().toEpochMilliseconds())

    /**
     * Check if date is today
     * @param timestamp Date to check (epoch milliseconds)
     * @return True if today
     */
    fun isToday(timestamp: Long?): Boolean {
        if (timestamp == null) return false

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val checkDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date

        return now == checkDate
    }

    /**
     * Check if date is yesterday
     * @param timestamp Date to check (epoch milliseconds)
     * @return True if yesterday
     */
    fun isYesterday(timestamp: Long?): Boolean {
        if (timestamp == null) return false

        val yesterday = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(1, DateTimeUnit.DAY)
        val checkDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date

        return yesterday == checkDate
    }

    /**
     * Returns date in relative format (Today, Yesterday, etc.)
     * @param timestamp Date to format (epoch milliseconds)
     * @return Relative date string
     */
    fun getRelativeDate(timestamp: Long?): String {
        if (timestamp == null) return ""

        if (isToday(timestamp)) {
            return "Bugün"
        } else if (isYesterday(timestamp)) {
            return "Dün"
        } else {
            val daysAgo = calculateDaysBetween(timestamp, Clock.System.now().toEpochMilliseconds())
            if (daysAgo < 7) {
                return "$daysAgo gün önce"
            } else if (daysAgo < 30) {
                val weeksAgo = daysAgo / 7
                return "$weeksAgo hafta önce"
            } else {
                return formatDateShort(timestamp)
            }
        }
    }

    /**
     * Returns month start and end dates
     * @param timestamp Target date (epoch milliseconds)
     * @return {month start, month end} dates (epoch milliseconds)
     */
    fun getMonthStartEnd(timestamp: Long): Array<Long> {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Start of the month
        val monthStart = LocalDate(date.year, date.month, 1)
        val monthStartTimestamp = monthStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        // End of the month
        val daysInMonth = getDaysInMonth(date.year, date.monthNumber)
        val monthEnd = LocalDate(date.year, date.month, daysInMonth)
        val monthEndTimestamp = monthEnd.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return arrayOf(monthStartTimestamp, monthEndTimestamp)
    }

    /**
     * Formats duration (milliseconds to hours:minutes)
     * @param durationMillis Duration (milliseconds)
     * @return Formatted duration
     */
    fun formatDuration(durationMillis: Long): String {
        if (durationMillis <= 0) return "0 min"

        val hours = durationMillis / (60 * 60 * 1000)
        val minutes = (durationMillis / (60 * 1000)) % 60

        return if (hours > 0) {
            "${hours}sa ${minutes}dk"
        } else {
            "$minutes minutes"
        }
    }

    /**
     * Formats stopwatch (HH:MM:SS)
     * @param durationMillis Duration (milliseconds)
     * @return Stopwatch format
     */
    fun formatStopwatch(durationMillis: Long): String {
        if (durationMillis <= 0) return "00:00:00"

        val hours = durationMillis / (60 * 60 * 1000)
        val minutes = (durationMillis / (60 * 1000)) % 60
        val seconds = (durationMillis / 1000) % 60

        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Calculates age
     * @param birthTimestamp Birth date (epoch milliseconds)
     * @return Age
     */
    fun calculateAge(birthTimestamp: Long?): Int {
        if (birthTimestamp == null) return 0

        val birthDate = Instant.fromEpochMilliseconds(birthTimestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        var age = now.year - birthDate.year

        // Subtract one from age if birthday hasn't occurred yet
        if (now.monthNumber < birthDate.monthNumber || 
            (now.monthNumber == birthDate.monthNumber && now.dayOfMonth < birthDate.dayOfMonth)) {
            age--
        }

        return age
    }

    /**
     * Returns day name of the week
     * @param timestamp Date (epoch milliseconds)
     * @return Day name (English)
     */
    fun getDayName(timestamp: Long?): String {
        if (timestamp == null) return ""

        val instant = Instant.fromEpochMilliseconds(timestamp)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        return TURKISH_DAYS[date.dayOfWeek.ordinal]
    }

    /**
     * Returns month name
     * @param timestamp Date (epoch milliseconds)
     * @return Month name (English)
     */
    fun getMonthName(timestamp: Long?): String {
        if (timestamp == null) return ""

        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return TURKISH_MONTHS[dateTime.monthNumber - 1]
    }

    /**
     * Helper function to get days in month
     * @param year Year
     * @param month Month (1-12)
     * @return Number of days in month
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month: $month")
        }
    }

    /**
     * Check if year is leap year
     * @param year Year
     * @return True if leap year
     */
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
