package com.example.myapplication.view.presentation.addTask

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.TaskType

@Composable
fun TaskTypeSelector(
    selectedTaskType: TaskType,
    onTaskTypeSelected: (TaskType) -> Unit
) {
    Column {
        // Binary Task Option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTaskType == TaskType.Binary,
                onClick = { onTaskTypeSelected(TaskType.Binary) }
            )
            Column {
                Text(text = "Binary Task", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Task that can be either complete or not-complete",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Partial Task Option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTaskType == TaskType.Partial,
                onClick = { onTaskTypeSelected(TaskType.Partial) }
            )
            Column {
                Text(text = "Partial Task", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Task that can be completed in parts",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
