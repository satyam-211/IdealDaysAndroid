package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repo.TaskRepository
import com.example.myapplication.model.Task
import com.example.myapplication.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val taskRepository: TaskRepository
) :
    ViewModel() {
    // Task Type State
    private val _selectedTaskType = MutableStateFlow(TaskType.Binary)
    val selectedTaskType: StateFlow<TaskType> = _selectedTaskType

    fun onTaskTypeSelected(taskType: TaskType) {
        _selectedTaskType.value = taskType
    }

    val description = MutableStateFlow("")

    val currentTimeinMillis = MutableStateFlow<Long?>(null)

    // Function to add the task
    fun addTask() {
        viewModelScope.launch {
            val task = when (_selectedTaskType.value) {
                TaskType.Binary -> {
                    Task.BinaryTask(
                        description = description.value,
                        alarmTimeInMillis = currentTimeinMillis.value,
                    )
                }

                TaskType.Partial -> {
                    Task.PartialTask(
                        description = description.value,
                        completionHistory = mutableListOf(),
                        alarmTimeInMillis = currentTimeinMillis.value,// Starts with empty history
                    )
                }
            }
            if (currentTimeinMillis.value != null) {
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
