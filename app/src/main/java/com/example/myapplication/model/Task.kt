package com.example.myapplication.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.data.local.entities.TaskEntity
import com.google.gson.Gson
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID


sealed class Task(
    open val id: UUID,
    open val description: String,
    open val createdDate: LocalDate = LocalDate.now(),
    // Alarm-related fields
    open var alarmTimeInMillis: Long? = null,
) {
    abstract fun toEntity(): TaskEntity

    abstract fun isCompleted(): Boolean

    abstract var scheduledDate: LocalDate

    data class BinaryTask(
        override val id: UUID = UUID.randomUUID(),
        override val createdDate: LocalDate = LocalDate.now(),
        override val description : String,
        var isThisCompleted: Boolean = false,
        override var alarmTimeInMillis: Long? = null,
        override var scheduledDate: LocalDate  = createdDate,
    ) : Task(id = id, description = description, createdDate = createdDate, alarmTimeInMillis = alarmTimeInMillis) {
        override fun toEntity(): TaskEntity {
            return TaskEntity(
                id = id,
                description = description,
                createdDate = createdDate,
                scheduledDate = scheduledDate,
                taskType = 0,
                isCompleted = isThisCompleted,
            )
        }

        override fun isCompleted(): Boolean {
            return isThisCompleted
        }
    }

    data class PartialTask(
        override val id: UUID = UUID.randomUUID(),
        override val createdDate: LocalDate = LocalDate.now(),
        override val description : String,
        override var alarmTimeInMillis: Long? = null,
        var completionHistory: MutableList<CompletionEntry> = mutableListOf(),
        override var scheduledDate: LocalDate = createdDate,
    ) : Task(id = id, description = description, createdDate = createdDate) {
       override fun toEntity() : TaskEntity {
           val completionHistoryJson = convertToJson(completionHistory)

           return TaskEntity(
               id = id,
               description = description,
               createdDate = createdDate,
               scheduledDate = scheduledDate,
               taskType = 1,
               completionHistoryJson = completionHistoryJson,
           )
       }
        private fun convertToJson(completionHistory: List<CompletionEntry>): String {
            return Gson().toJson(completionHistory)
        }

        override fun isCompleted(): Boolean {
            return getLatestCompletionPercentage() == 100
        }

        fun getLatestCompletionPercentage(): Int {
            return completionHistory.lastOrNull()?.percentageCompleted ?: 0
        }

        fun saveCompletionEntry(percentage: Int, reason: String?) {
            val newEntry = CompletionEntry(
                date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                percentageCompleted = percentage,
                reasonIncomplete = reason
            )
            completionHistory.add(newEntry)
        }
    }
}

data class CompletionEntry(
    val date: LocalDate,
    val percentageCompleted: Int,
    val reasonIncomplete: String? = null
)

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
    val month = this.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
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

