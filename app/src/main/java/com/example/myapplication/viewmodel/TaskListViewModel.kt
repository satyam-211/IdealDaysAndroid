package com.example.myapplication.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repo.TaskRepository
import com.example.myapplication.model.DayTasks
import com.example.myapplication.model.Task
import com.example.myapplication.utils.now
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(private val taskRepository: TaskRepository) :
    ViewModel() {
    private val _allTaskList = MutableStateFlow<List<DayTasks>>(emptyList())

    private val _dayTaskList = MutableStateFlow<List<DayTasks>>(emptyList())
    val dayTaskList: StateFlow<List<DayTasks>> = _dayTaskList

    private var currentStartDate: LocalDate =
        LocalDate.now() // Keep track of the end date for pagination
    private var currentEndDate: LocalDate =
        LocalDate.now()
    private var isMoreDataAvailable: Boolean = true


    private val _angerMode = MutableStateFlow(false)
    val angerMode: StateFlow<Boolean> = _angerMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _scheduleNowTasks = mutableStateOf<Set<Task>>(emptySet())
    val scheduleNowTasks: State<Set<Task>> = _scheduleNowTasks

    fun toggleSchedule(task: Task, checked: Boolean) {
        _scheduleNowTasks.value = if (checked) {
            _scheduleNowTasks.value + task
        } else {
            _scheduleNowTasks.value - task
        }
    }

    init {
        loadInitialTasks()
    }

    fun toggleAngerMode() {
        _angerMode.value = !_angerMode.value
        if (angerMode.value) {
            _dayTaskList.value = _dayTaskList.value.mapNotNull { dayTasks ->
                if (dayTasks.date != LocalDate.now()) {
                    val incompleteTasks = dayTasks.tasks.filter { task -> !task.isCompleted() }
                    if (incompleteTasks.isNotEmpty()) {
                        return@mapNotNull dayTasks.copy(tasks = incompleteTasks)
                    }
                }
                null
            }
        } else {
            _scheduleNowTasks.value = emptySet()
            _dayTaskList.value = _allTaskList.value
        }
    }

    fun loadInitialTasks(reset: Boolean = false) {
        if (reset) {
            currentStartDate = LocalDate.now()
            currentEndDate = LocalDate.now() // Reset to today
            _dayTaskList.value = emptyList()
            isMoreDataAvailable = true
        }
        _isLoading.value = true
        viewModelScope.launch {
            currentStartDate =
                currentEndDate.minus(7, DateTimeUnit.DAY) // Adjust as needed for initial range
            val tasks = withContext(Dispatchers.IO) {
                taskRepository.getTasksForDateRange(
                    currentStartDate,
                    currentEndDate
                )
            }
            _allTaskList.value = tasks
            _dayTaskList.value = tasks
            isMoreDataAvailable = tasks.isNotEmpty()
            _isLoading.value = false
        }
    }

    fun loadMoreTasks() {
        if (!isMoreDataAvailable || isLoading.value) return

        _isLoading.value = true

        viewModelScope.launch {
            currentEndDate = currentStartDate
            currentStartDate = currentStartDate.minus(7, DateTimeUnit.DAY) // Adjust as needed
            val tasks = withContext(Dispatchers.IO) {
                taskRepository.getTasksForDateRange(
                    currentStartDate,
                    currentEndDate
                )
            }
            if (tasks.isNotEmpty()) {
                _allTaskList.value += tasks
                _dayTaskList.value += tasks
            } else {
                isMoreDataAvailable = false // No more data to load
            }
            _isLoading.value = false
        }
    }

    private fun loadCurrentTasks() {
        if (isLoading.value) return

        _isLoading.value = true

        viewModelScope.launch {
            val tasks = taskRepository.getTasksForDateRange(currentStartDate, LocalDate.now())
            if (tasks.isNotEmpty()) {
                _allTaskList.value = tasks
                _dayTaskList.value = tasks
            } else {
                isMoreDataAvailable = false // No more data to load
            }
            _isLoading.value = false
        }
    }

    fun updateTaskCompletion(task: Task) {
        viewModelScope.launch {
            when (task) {
                is Task.BinaryTask -> {
                    taskRepository.toggleBinaryTask(task = task)
                }

                is Task.PartialTask -> {
                    taskRepository.updatePartialTaskCompletion(task = task)
                }
            }
        }
    }

    fun scheduleTasksNow() {
        viewModelScope.launch(Dispatchers.IO) {
            for (task in _scheduleNowTasks.value) {
                taskRepository.moveTaskToCurrentDay(task)
            }
        }
        _angerMode.value = !_angerMode.value
        loadCurrentTasks()
    }


}