package com.example.myapplication.view.presentation.taskList.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.model.CompletionEntry
import com.example.myapplication.model.Task
import com.example.myapplication.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialTaskView(task: Task.PartialTask) {
    val viewModel: TaskListViewModel = hiltViewModel()
    var showHistorySheet by remember {
        mutableStateOf(false)
    }
    var showEditSheet by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (task.completionHistory.isNotEmpty())
                    Text(
                        text = "See history",
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            showHistorySheet = true
                        })

            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { showEditSheet = true }) {
                    Text("Edit")
                }
                Text(text = "Completed ${task.getLatestCompletionPercentage()}%")
            }
        }
    }

    if (showHistorySheet) {
        HistorySheet(
            completionHistory = task.completionHistory,
            onDismiss = { showHistorySheet = false },
        )
    }

    if (showEditSheet)
        PartialTaskEditView(
            task = task,
            onDismiss = { showEditSheet = false },
            onSave = { sliderValue, reason ->
                showEditSheet = false
                task.saveCompletionEntry(sliderValue, reason)
                viewModel.updateTaskCompletion(task)
            })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(completionHistory: MutableList<CompletionEntry>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(text = "History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn {
            items(completionHistory.size) { idx ->
                val history = completionHistory[idx]
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = history.reasonIncomplete.toString())
                    Text(text = "${history.percentageCompleted}%")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialTaskEditView(
    task: Task.PartialTask,
    onDismiss: () -> Unit,
    onSave: (Int, String?) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {

        var sliderValue by remember { mutableIntStateOf(task.getLatestCompletionPercentage()) }
        val showReasonText = sliderValue < 100
        var reasonText by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(6.dp)
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Slider with steps at 0, 25, 50, 100
                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = {
                        sliderValue = it.toInt()
                    },
                    valueRange = 0f..100f,
                    steps = 3, // 0, 25, 50, 75, 100 (steps - 1)
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0%")
                    Text(text = "25%")
                    Text(text = "50%")
                    Text(text = "75%")
                    Text(text = "100%")
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = showReasonText,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = 100
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 300
                        )
                    ),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = reasonText,
                            onValueChange = { reasonText = it },
                            label = { Text("Non-Completion Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Button(
                    onClick = { onSave(sliderValue, reasonText.ifEmpty { null }) },
                ) {
                    Text(text = "Save")
                }
            }
        }

    }
}
