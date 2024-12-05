package com.example.myapplication.model

import com.example.myapplication.data.AttachmentInfo
import com.example.myapplication.data.local.entities.TaskEntity
import com.example.myapplication.utils.now
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID

sealed class Task(val id: UUID, val createdDate: LocalDate, val scheduledDate: LocalDate) {
    abstract val description: String

    // Alarm-related fields
    abstract val alarmTimeInMillis: Long?
    abstract val attachments: List<AttachmentInfo>

    abstract fun toEntity(): TaskEntity

    abstract fun isCompleted(): Boolean

    data class BinaryTask(
        val binaryTaskId: UUID?,
        val binaryTaskCreatedDate: LocalDate?,
        var binaryTaskScheduledDate: LocalDate?,
        override val description: String,
        var isThisCompleted: Boolean = false,
        override var alarmTimeInMillis: Long? = null,
        override val attachments: List<AttachmentInfo>,
    ) : Task(
        id = binaryTaskId ?: UUID.randomUUID(),
        createdDate = binaryTaskCreatedDate ?: LocalDate.now(),
        scheduledDate = binaryTaskScheduledDate ?: LocalDate.now()
    ) {
        override fun toEntity(): TaskEntity {
            return TaskEntity(
                id = id,
                description = description,
                createdDate = createdDate,
                scheduledDate = scheduledDate,
                taskType = 0,
                isCompleted = isThisCompleted,
                alarmTimeInMillis = alarmTimeInMillis,
                attachments = attachments,
            )
        }

        override fun isCompleted(): Boolean {
            return isThisCompleted
        }
    }

    data class PartialTask(
        val partialTaskId: UUID?,
        val partialTaskCreatedDate: LocalDate?,
        var partialTaskScheduledDate: LocalDate?,
        override val description: String,
        override var alarmTimeInMillis: Long? = null,
        var completionHistory: MutableList<CompletionEntry> = mutableListOf(),
        override val attachments: List<AttachmentInfo>,
    ) : Task(
        id = partialTaskId ?: UUID.randomUUID(),
        createdDate = partialTaskCreatedDate ?: LocalDate.now(),
        scheduledDate = partialTaskScheduledDate ?: LocalDate.now()
    ) {
        override fun toEntity(): TaskEntity {
            val completionHistoryJson = convertToJson(completionHistory)

            return TaskEntity(
                id = id,
                description = description,
                createdDate = createdDate,
                scheduledDate = scheduledDate,
                taskType = 1,
                completionHistoryJson = completionHistoryJson,
                alarmTimeInMillis = alarmTimeInMillis,
                attachments = attachments,
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

fun Task.toJson(): String {
    val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()
    return URLEncoder.encode(gson.toJson(this), "UTF-8")
}

fun String.toTask(): Task? {
    return try {
        val decodedJson = URLDecoder.decode(this, "UTF-8")
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
        when {
            decodedJson.contains("\"isThisCompleted\"") ->
                gson.fromJson(decodedJson, Task.BinaryTask::class.java)

            decodedJson.contains("\"completionHistory\"") ->
                gson.fromJson(decodedJson, Task.PartialTask::class.java)

            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
