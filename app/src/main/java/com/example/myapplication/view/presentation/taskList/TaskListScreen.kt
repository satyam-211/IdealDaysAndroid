package com.example.myapplication.view.presentation.taskList

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.constants.PaddingConstants
import com.example.myapplication.constants.RouteConstants
import com.example.myapplication.constants.StringConstants
import com.example.myapplication.view.presentation.taskList.views.DayTaskView
import com.example.myapplication.viewmodel.TaskListViewModel


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    navController: NavController,
) {
    val dayTaskList by viewModel.dayTaskList.collectAsState()
    val angerMode by viewModel.angerMode.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val scrollState = rememberLazyListState()
    val taskAdded = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("taskAdded", false)
        ?.collectAsState()

    // Handle task addition and reload task list
    LaunchedEffect(taskAdded?.value) {
        if (taskAdded?.value == true) {
            viewModel.loadInitialTasks(reset = true)
            // Reset the flag after handling
            navController.currentBackStackEntry?.savedStateHandle?.set("taskAdded", false)
        }
    }

    // Handle scroll and pagination
    // Check if we need to load more
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = scrollState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems in 1..lastVisibleIndex
        }
    }

    // Call onLoadMore when needed
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreTasks()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = StringConstants.TASKS_TITLE)
            },
                actions = {
                    IconButton(onClick = { navController.navigate(RouteConstants.ADDEDITTASKROUTE) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tasks")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAngerMode() },
                containerColor = if (!angerMode) Color.Red else MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = if (angerMode) Icons.Default.ThumbUp else Icons.Default.Warning,
                    tint = if (angerMode) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    contentDescription = if (angerMode) "Normal Mode" else "Anger Mode"
                )
            }
        }
    ) { padding ->
        MaterialTheme(
            colorScheme = if (angerMode) {
                MaterialTheme.colorScheme.copy(
                    primary = Color.Red,
                    secondary = Color.Red,
                    background = Color(0xFFFFE5E5), // Light red background
                    surface = Color(0xFFFFE5E5),
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color.Black,
                    onSurface = Color.Black
                )
            } else {
                MaterialTheme.colorScheme
            }
        ) {
            LazyColumn(modifier = Modifier.padding(padding), state = scrollState) {
                if (angerMode)
                    item {
                        val scheduleTasks by viewModel.scheduleNowTasks
                        val scheduleTasksCount by remember {
                            derivedStateOf {
                                scheduleTasks.size
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    PaddingConstants.PADDING.dp
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp) // Set the size of the box
                                    .background(Color.Red, shape = CircleShape) // Make it circular with a background color
                            ) {
                                Text(
                                    text = scheduleTasksCount.toString(),
                                )
                            }
                            Button(onClick = { viewModel.scheduleTasksNow() }) {
                                Text("Schedule Now")
                            }
                        }
                    }
                items(dayTaskList) { dayTasks ->
                    DayTaskView(dayTasks = dayTasks)
                }
                if (loading)
                    item {
                        CircularProgressIndicator()
                    }
            }
        }
    }

}
