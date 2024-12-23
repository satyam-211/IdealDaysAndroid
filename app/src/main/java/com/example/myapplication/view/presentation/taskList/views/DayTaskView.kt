package com.example.myapplication.view.presentation.taskList.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.constants.PaddingConstants
import com.example.myapplication.model.DayTasks
import com.example.myapplication.utils.formatWithOrdinal
import com.example.myapplication.view.presentation.components.SwipeToDelete
import com.example.myapplication.viewmodel.TaskListViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayTaskView(dayTasks: DayTasks) {

    var isExpanded by remember { mutableStateOf(false) }

    val viewModel: TaskListViewModel = hiltViewModel()
    val angerModeEnabled by viewModel.angerMode.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize()
            .padding(PaddingConstants.PADDING.dp),
    ) {
        // Heading with date and completion percentage
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
        ) {
            Column {
                Text(
                    text = dayTasks.date.formatWithOrdinal(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${dayTasks.completionPercentage}%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isExpanded = !isExpanded }
            )
        }

        // Expandable content
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                dayTasks.tasks.forEach { task ->
                    key(task.id) {
                        SwipeToDelete(
                            enableSwipe = !angerModeEnabled,
                            modifier = Modifier.padding(4.dp),
                            swipeThreshold = 0.5f,
                            onDelete = {
                                viewModel.deleteTask(task)
                            }) {
                            TaskView(task = task)
                        }
                    }
                }
            }
        }
    }


}