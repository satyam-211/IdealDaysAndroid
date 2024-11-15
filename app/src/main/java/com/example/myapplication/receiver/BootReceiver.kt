package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.di.BootReceiverEntryPoint
import com.example.myapplication.model.now
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Access Hilt dependencies via Entry Point
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                BootReceiverEntryPoint::class.java
            )
            val alarmScheduler = entryPoint.alarmScheduler()
            val appDatabase = entryPoint.taskDatabase()
            // Launch a coroutine to handle database operations off the main thread
            CoroutineScope(Dispatchers.IO).launch {
                // Assuming you have a DAO to fetch tasks with alarms
                val tasksWithAlarms = appDatabase.taskDao().getTasksForDateRange(startDate = LocalDate.now(), endDate = LocalDate.now(), limit = 50)

                tasksWithAlarms.forEach { task ->
                    alarmScheduler.scheduleTaskAlarm(task.toTask())
                }
            }
        }
    }

}