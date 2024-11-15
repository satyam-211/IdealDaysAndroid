package com.example.myapplication.di


import com.example.myapplication.data.local.database.TaskDatabase
import com.example.myapplication.utils.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun alarmScheduler(): AlarmScheduler
    fun taskDatabase(): TaskDatabase
}
