package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.utils.NotificationUtils

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("DESCRIPTION") ?: "Time for your alarm!"
        NotificationUtils.showNotification(context,"Task Reminder", message)
    }
}