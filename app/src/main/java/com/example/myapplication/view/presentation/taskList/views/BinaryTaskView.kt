package com.example.myapplication.view.presentation.taskList.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.model.Task
import com.example.myapplication.viewmodel.TaskListViewModel

@Composable
fun BinaryTaskView(task: Task.BinaryTask) {
    var isChecked by remember { mutableStateOf(task.isThisCompleted) }
    val viewModel: TaskListViewModel = hiltViewModel()
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                task.isThisCompleted = checked
                viewModel.updateTaskCompletion(task = task)
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = task.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}