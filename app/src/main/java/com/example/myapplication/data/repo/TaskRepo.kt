package com.example.myapplication.data.repo

import com.example.myapplication.model.DayTasks
import com.example.myapplication.model.Task
import kotlinx.datetime.LocalDate

interface TaskRepository {
    suspend fun addTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getTasksForDateRange(startDate: LocalDate, endDate: LocalDate): List<DayTasks>
    suspend fun moveTaskToCurrentDay(task: Task)
    suspend fun toggleBinaryTask(task: Task.BinaryTask)
    suspend fun updatePartialTaskCompletion(task: Task.PartialTask)
}

