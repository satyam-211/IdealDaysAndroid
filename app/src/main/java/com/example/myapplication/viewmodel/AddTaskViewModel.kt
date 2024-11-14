package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repo.TaskRepository
import com.example.myapplication.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(private val taskRepository: TaskRepository) : ViewModel() {
    // Task Type State
    private val _selectedTaskType = MutableStateFlow<TaskType>(TaskType.Binary)
    val selectedTaskType: StateFlow<TaskType> = _selectedTaskType

    fun onTaskTypeSelected(taskType: TaskType) {
        _selectedTaskType.value = taskType
    }

    val description = MutableStateFlow("")

    // Function to add the task
    fun addTask() {
        viewModelScope.launch {
            val task = when (_selectedTaskType.value) {
                TaskType.Binary -> {
                    Task.BinaryTask(
                        binaryTaskDesc = description.value,
                    )
                }
                TaskType.Partial -> {
                    Task.PartialTask(
                        partialTaskDesc = description.value,
                        completionHistory = mutableListOf() // Starts with empty history
                    )
                }
            }
            taskRepository.addTask(task = task)
        }
    }

}

enum class TaskType {
    Binary,
    Partial
}
