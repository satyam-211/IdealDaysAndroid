package com.example.myapplication.view.presentation.addTask


import PermissionHandler
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.view.presentation.components.TimePicker
import com.example.myapplication.viewmodel.AddEditTaskViewModel
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    navController: NavController,
) {
    val context = LocalContext.current
    val selectedTaskType by viewModel.selectedTaskType.collectAsState()
    val description by viewModel.description.collectAsState()
    val alarmTime by viewModel.currentTimeinMillis.collectAsState()
    var showAlarmPermissionDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isFormValid by remember {
        derivedStateOf { description.trim().isNotEmpty() }
    }
    val isEditMode by remember {
        derivedStateOf { viewModel.taskToBeEdited != null }
    }
    val attachments by viewModel.attachments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Add New Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (!isEditMode)
                Text(text = "Select Task Type", style = MaterialTheme.typography.headlineMedium)
            if (!isEditMode)
                Spacer(modifier = Modifier.height(8.dp))

            if (!isEditMode)
            // Radio Buttons for Task Type Selection
                TaskTypeSelector(
                    selectedTaskType = selectedTaskType,
                    onTaskTypeSelected = viewModel::onTaskTypeSelected
                )
            if (!isEditMode)
                Spacer(modifier = Modifier.height(16.dp))

            TaskForm(
                description = description,
                onDescriptionChange = { viewModel.description.value = it },
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Handle Notification Permission
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    if (granted) {
                        showTimePicker = true
                    } else {
                        // Handle permission denial, e.g., show a message
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AlarmButton(
                    onClick = {
                        if (PermissionHandler.hasExactAlarmPermission(context) && PermissionHandler.hasNotificationPermission(
                                context
                            )
                        ) {
                            showTimePicker = true
                        } else if (!PermissionHandler.hasExactAlarmPermission(context)) {
                            showAlarmPermissionDialog = true
                        } else if (!PermissionHandler.hasNotificationPermission(context)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    label = alarmTime,
                )
                AddAttachmentButton(
                    onAttachmentsSelected = { attachments ->
                        viewModel.addAttachments(attachments = attachments)
                    },
                )
            }

            // Alarm Permission Dialog
            if (showAlarmPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { /* Optional: Handle dismiss */ },
                    title = { Text("Permission Required") },
                    text = { Text("This app needs permission to schedule exact alarms. Please grant it in settings.") },
                    confirmButton = {
                        Button(onClick = {
                            PermissionHandler.requestExactAlarmPermission(context)
                            showAlarmPermissionDialog = false
                        }) {
                            Text("Open Settings")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showAlarmPermissionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showTimePicker) {
                TimePicker { hour, minute ->
                    showTimePicker = false

                    // Calculate the time in milliseconds for the selected time today
                    viewModel.currentTimeinMillis.value = Calendar.getInstance().let {
                        it.set(Calendar.HOUR_OF_DAY, hour)
                        it.set(Calendar.MINUTE, minute)
                        it.set(Calendar.SECOND, 0)
                        it.set(Calendar.MILLISECOND, 0)

                        // If the selected time is before the current time, set it for the next day
                        if (it.before(Calendar.getInstance())) {
                            it.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        it.timeInMillis
                    }

                }
            }

            attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onDelete = { viewModel.removeAttachment(attachment) },
                    onUpdate = { updatedAttachment ->
                        viewModel.removeAttachment(attachment)
                        viewModel.addAttachment(updatedAttachment)
                        viewModel.updateAttachments(attachments)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    viewModel.addTask()
                    navController.previousBackStackEntry?.savedStateHandle?.set("taskAdded", true)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(text = if (isEditMode) "Update Task" else "Add Task")
            }
        }
    }
}
