package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R

object NotificationUtils {
    private const val CHANNEL_ID = "TASK_ALARM_CHANNEL"
    private const val CHANNEL_NAME = "Task Alarms"
    private const val CHANNEL_DESCRIPTION = "Notifications for Task Alarms"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the importance level of the channel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
            /**
             * Displays a notification with the given title and message.
             *
             * @param context The application context.
             * @param title The title of the notification.
             * @param message The content text of the notification.
             */
    fun showNotification(context: Context, title: String, message: String) {
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's alarm icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up notification
            .setAutoCancel(true) // Dismiss the notification when tapped

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            // Use a unique notification ID for each alarm to avoid overwriting
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
