package com.example.myapplication.view.presentation.addTask

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.utils.Utils

@Composable
fun AlarmButton(
    onClick: () -> Unit,
    label: Long?
) {
    Button(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label?.let { Utils.formatTime(it) } ?: "Set Alarm")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.Notifications, contentDescription = "Set Alarm")
        }
    }
}