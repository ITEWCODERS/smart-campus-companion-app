package com.example.smartcompanionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcompanionapp.ui.screens.TaskScreen
import com.example.unisync.ui.screens.DashboardScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")

    object CampusInformation : Screen("campusInfo")
    object Task : Screen("task")

}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(Screen.Task.route){
            TaskScreen(navController)
        }

    }
}