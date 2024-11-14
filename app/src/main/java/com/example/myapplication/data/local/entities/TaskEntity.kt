package com.example.myapplication.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.data.local.type_converters.Converters
import com.example.myapplication.model.CompletionEntry
import com.example.myapplication.model.Task
import com.google.gson.Gson
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
class TaskEntity(
    @PrimaryKey val id: UUID,
    val description: String,
    val createdDate: LocalDate,
    val scheduledDate: LocalDate,
    val taskType: Int,
    val completionHistoryJson: String? = null,
    val isCompleted: Boolean? = null,
){
    fun toTask() : Task {
       return when(taskType){
            0 -> Task.BinaryTask(
                binaryTaskId = id,
                binaryTaskDesc = description,
                binaryTaskCreatedDate = createdDate,
                scheduledDate = scheduledDate,
                isThisCompleted = isCompleted ?: false,
            )
           1 -> {
               val completionHistory = completionHistoryJson?.let {
                   Gson().fromJson(it, Array<CompletionEntry>::class.java).toList()
               } ?: emptyList()

              return Task.PartialTask(
                  partialTaskId = id,
                  partialTaskDesc = description,
                  partialTaskCreatedDate = createdDate,
                  scheduledDate = scheduledDate,
                  completionHistory = completionHistory.toMutableList(),
              )
           }

           else -> throw IllegalArgumentException("Invalid task type")
       }
    }
}