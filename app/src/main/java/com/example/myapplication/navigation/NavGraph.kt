package com.example.myapplication.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.constants.RouteConstants
import com.example.myapplication.view.presentation.addTask.AddEditTaskScreen
import com.example.myapplication.view.presentation.taskList.TaskListScreen

val LocalAppNavController =
    compositionLocalOf<NavHostController> { error("NavHostController error") }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    CompositionLocalProvider(
        LocalAppNavController provides navController
    ) {
        NavHost(navController = navController, startDestination = "/") {
            composable(RouteConstants.ROOT) {
                TaskListScreen(navController = navController)
            }
            composable(
                route = "${RouteConstants.ADDEDITTASKROUTE}?task={task}",
                arguments = listOf(
                    navArgument("task") {
                        type = NavType.StringType
                        nullable = true // Allow null if no task is passed
                        defaultValue = null // Default value for optional parameter
                    }
                ),
            ) {
                AddEditTaskScreen(
                    navController = navController,
                )
            }
        }
    }
}