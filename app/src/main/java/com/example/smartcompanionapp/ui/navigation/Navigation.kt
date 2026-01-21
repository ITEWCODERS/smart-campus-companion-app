package com.example.unisync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.unisync.ui.screens.DashboardScreen
import com.example.unisync.ui.screens.ScheduleScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")
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
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController)
        }
    }
}
