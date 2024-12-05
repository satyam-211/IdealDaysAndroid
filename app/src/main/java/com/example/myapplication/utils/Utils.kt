package com.example.myapplication.utils

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utils {

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}

fun LocalDate.Companion.now(): LocalDate {
    val currentInstant = Clock.System.now()
    return currentInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}

/**
 * Extension function to format kotlinx.datetime.LocalDate to a string like "14th Sept 2024".
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.formatWithOrdinal(): String {
    val day = this.dayOfMonth
    val suffix = getDayOfMonthSuffix(day)
    val month =
        this.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
    val year = this.year
    return "$day$suffix $month $year"
}

/**
 * Helper function to get the ordinal suffix for a given day.
 */
private fun getDayOfMonthSuffix(n: Int): String {
    return if (n in 11..13) {
        "th"
    } else {
        when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}