package com.example.smartcompanionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcompanionapp.ui.screens.LoginScreen
import com.example.smartcompanionapp.ui.screens.CampusInfoScreen
import com.example.smartcompanionapp.ui.screens.DashboardScreen
import com.example.smartcompanionapp.ui.screens.ScheduleScreen
import com.example.smartcompanionapp.ui.screens.SettingsScreen
import com.example.smartcompanionapp.ui.screens.SignUpScreen
import com.example.smartcompanionapp.ui.screens.TaskScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")

    object CampusInformation : Screen("campusInfo")
    object Task : Screen("task")

    object login : Screen("login")
    object signup : Screen("signup")

    object task : Screen("task")

    object settings : Screen("settings")

    object notifications : Screen("notifications")

}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.login.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController)
        }
        composable(Screen.CampusInformation.route) {
            CampusInfoScreen(navController)
        }
        composable(Screen.login.route) {
            LoginScreen(navController)
        }

        composable(Screen.signup.route) {
            SignUpScreen(navController)
        }

        composable(Screen.task.route) {
            TaskScreen(navController)
        }

        composable(Screen.settings.route) {
            SettingsScreen(navController)
        }

    }
}
