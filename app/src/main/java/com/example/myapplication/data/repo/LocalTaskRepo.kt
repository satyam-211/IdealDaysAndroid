package com.example.myapplication.data.repo

import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entities.TaskEntity
import com.example.myapplication.model.CompletionEntry
import com.example.myapplication.model.DayTasks
import com.example.myapplication.model.Task
import com.example.myapplication.utils.now
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class LocalTaskRepository @Inject constructor(private val taskDao: TaskDao) : TaskRepository {

    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task = task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTaskById(taskId = task.id)
    }

    override suspend fun getTasksForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DayTasks> {
        val tasks = taskDao.getTasksForDateRange(
            startDate,
            endDate,
            limit = 50,
            offset = 0
        )
        val groupedTasks = tasks.groupBy { it.scheduledDate }
        return groupedTasks.map { (date, tasksForDate) ->
            // Calculate the completion percentage for the day
            val completionPercentage = calculateCompletionPercentage(tasksForDate)
            DayTasks(date, tasksForDate.map { it.toTask() }, completionPercentage)
        }.sortedByDescending { it.date }
    }


    private fun calculateCompletionPercentage(tasks: List<TaskEntity>): Double {
        val totalTasks = tasks.size
        if (totalTasks == 0) return 0.0

        val totalCompletionPercentage = tasks.sumOf { task ->
            when (task.taskType) {
                0 -> if (task.isCompleted == true) 100 else 0
                1 -> task.completionHistoryJson?.let { parseCompletionHistory(it).lastOrNull()?.percentageCompleted }
                    ?: 0

                else -> throw Exception("Invalid task type")
            }
        }
        return totalCompletionPercentage.toDouble() / totalTasks
    }

    private fun parseCompletionHistory(json: String): List<CompletionEntry> {
        val gson = Gson()
        val type = object : TypeToken<List<CompletionEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    override suspend fun moveTaskToCurrentDay(task: Task) {
        val currentDate = LocalDate.now()
        taskDao.updateTaskDate(taskId = task.id, currentDate)
    }

    override suspend fun toggleBinaryTask(task: Task.BinaryTask) {
        taskDao.toggleBinaryTask(task.id)
    }

    override suspend fun updatePartialTaskCompletion(task: Task.PartialTask) {
        val gson = Gson()
        val completionHistoryJson = gson.toJson(task.completionHistory)
        taskDao.updatePartialTaskCompletion(task.id, completionHistoryJson)
    }
}
