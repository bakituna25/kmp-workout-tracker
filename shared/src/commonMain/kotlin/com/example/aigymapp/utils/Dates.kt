package com.example.aigymapp.utils

import kotlinx.datetime.*

/**
 * Generic tarih utility'leri
 * Tüm tarih işlemleri için ISO yyyy-MM-dd formatı kullanır
 */
object Dates {
    /**
     * LocalDate'i ISO string'e çevir (yyyy-MM-dd)
     */
    fun toStr(date: LocalDate): String {
        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
    }
    
    /**
     * Yıl, ay, gün'den LocalDate oluştur
     */
    fun from(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate(year, month, day)
    }
    
    /**
     * ISO string'den LocalDate oluştur
     */
    fun fromStr(dateStr: String): LocalDate {
        val parts = dateStr.split("-")
        require(parts.size == 3) { "Date string must be in yyyy-MM-dd format" }
        
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        
        return LocalDate(year, month, day)
    }
    
    /**
     * Bugünün ISO string'i
     */
    fun today(): String {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return toStr(localDate)
    }
    
    /**
     * Ayın ilk günü (yyyy-MM-01)
     */
    fun monthStart(year: Int, month: Int): String {
        return toStr(LocalDate(year, month, 1))
    }
    
    /**
     * Ayın son günü (yyyy-MM-dd)
     */
    fun monthEnd(year: Int, month: Int): String {
        val daysInMonth = getDaysInMonth(year, month)
        return toStr(LocalDate(year, month, daysInMonth))
    }

    /**
     * Helper function to get days in month
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
     */
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
