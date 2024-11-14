package com.example.myapplication.model

import kotlinx.datetime.LocalDate

data class DayTasks(
    val date: LocalDate,
    val tasks: List<Task>,
    val completionPercentage: Double,
)