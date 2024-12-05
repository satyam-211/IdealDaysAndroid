package com.example.myapplication.viewmodel


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AttachmentInfo
import com.example.myapplication.data.repo.TaskRepository
import com.example.myapplication.model.Task
import com.example.myapplication.model.toTask
import com.example.myapplication.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
) :
    ViewModel() {

    // Lazy initialization to parse Task from JSON only when needed
    val taskToBeEdited: Task? by lazy<Task?> {
        savedStateHandle.get<String>("task")?.toTask()
    }

    // StateFlow holding the list of attachments for the current task
    private val _attachments: MutableStateFlow<List<AttachmentInfo>> =
        MutableStateFlow(taskToBeEdited?.attachments ?: emptyList())
    val attachments: StateFlow<List<AttachmentInfo>> = _attachments.asStateFlow()

    fun addAttachments(attachments: List<AttachmentInfo>) {
        _attachments.value += attachments
    }

    fun updateAttachments(attachments: List<AttachmentInfo>) {
        _attachments.value = attachments
    }

    /**
     * Removes an attachment and updates the StateFlow.
     */
    fun removeAttachment(attachmentToRemove: AttachmentInfo) {
        _attachments.value -= attachmentToRemove
    }

    fun addAttachment(attachmentToRemove: AttachmentInfo) {
        _attachments.value += attachmentToRemove
    }

    // Task Type State
    private val _selectedTaskType = MutableStateFlow(
        when (taskToBeEdited) {
            is Task.BinaryTask -> TaskType.Binary
            is Task.PartialTask -> TaskType.Partial
            else -> TaskType.Binary
        }
    )
    val selectedTaskType: StateFlow<TaskType> = _selectedTaskType

    fun onTaskTypeSelected(taskType: TaskType) {
        _selectedTaskType.value = taskType
    }

    val description = MutableStateFlow(taskToBeEdited?.description ?: "")

    val currentTimeinMillis = MutableStateFlow(taskToBeEdited?.alarmTimeInMillis)

    // Function to add the task
    fun addTask() {
        viewModelScope.launch {
            val task = when (_selectedTaskType.value) {
                TaskType.Binary -> {
                    Task.BinaryTask(
                        binaryTaskId = taskToBeEdited?.id,
                        description = description.value,
                        alarmTimeInMillis = currentTimeinMillis.value,
                        attachments = attachments.value,
                        binaryTaskCreatedDate = taskToBeEdited?.createdDate,
                        binaryTaskScheduledDate = taskToBeEdited?.scheduledDate,
                    )
                }

                TaskType.Partial -> {
                    Task.PartialTask(
                        partialTaskId = taskToBeEdited?.id,
                        description = description.value,
                        completionHistory = (taskToBeEdited as Task.PartialTask?)?.completionHistory
                            ?: mutableListOf(),
                        alarmTimeInMillis = currentTimeinMillis.value,
                        attachments = attachments.value,
                        partialTaskCreatedDate = taskToBeEdited?.createdDate,
                        partialTaskScheduledDate = taskToBeEdited?.scheduledDate,
                    )
                }
            }
            if (currentTimeinMillis.value != null) {
                if (taskToBeEdited != null && taskToBeEdited!!.alarmTimeInMillis != null) alarmScheduler.cancelTaskAlarm(
                    task = taskToBeEdited!!
                )
                alarmScheduler.scheduleTaskAlarm(task = task)
            }
            taskRepository.addTask(task = task)
        }
    }
}

enum class TaskType {
    Binary,
    Partial
}
