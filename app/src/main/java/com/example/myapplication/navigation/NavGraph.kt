package com.example.myapplication.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.constants.RouteConstants
import com.example.myapplication.view.presentation.addTask.AddTaskScreen
import com.example.myapplication.view.presentation.taskList.TaskListScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "/") {
        composable(RouteConstants.ROOT) {
            TaskListScreen(navController = navController)
        }
        composable(RouteConstants.ADDTASKROUTE){
           AddTaskScreen(
               navController = navController,
           )
        }
    }
}