package com.example.myapplication.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.data.AttachmentInfo
import com.example.myapplication.data.local.type_converters.Converters
import com.example.myapplication.model.CompletionEntry
import com.example.myapplication.model.Task
import com.google.gson.Gson
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class TaskEntity(
    @PrimaryKey val id: UUID,
    val description: String,
    val createdDate: LocalDate,
    val scheduledDate: LocalDate,
    val taskType: Int,
    val completionHistoryJson: String? = null,
    val isCompleted: Boolean? = null,
    // Alarm-related fields
    val alarmTimeInMillis: Long? = null,
    // Attachments
    val attachments: List<AttachmentInfo> = emptyList()
){
    fun toTask() : Task {
       return when(taskType){
            0 -> Task.BinaryTask(
                binaryTaskId = id,
                description = description,
                binaryTaskCreatedDate = createdDate,
                binaryTaskScheduledDate = scheduledDate,
                isThisCompleted = isCompleted ?: false,
                alarmTimeInMillis = alarmTimeInMillis,
                attachments = attachments,
            )
           1 -> {
               val completionHistory = completionHistoryJson?.let {
                   Gson().fromJson(it, Array<CompletionEntry>::class.java).toList()
               } ?: emptyList()

              return Task.PartialTask(
                  partialTaskId = id,
                  description = description,
                  partialTaskCreatedDate = createdDate,
                  partialTaskScheduledDate = scheduledDate,
                  completionHistory = completionHistory.toMutableList(),
                  alarmTimeInMillis = alarmTimeInMillis,
                  attachments = attachments,
              )
           }

           else -> throw IllegalArgumentException("Invalid task type")
       }
    }
}