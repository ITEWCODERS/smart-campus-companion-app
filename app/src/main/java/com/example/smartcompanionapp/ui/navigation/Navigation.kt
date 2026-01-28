package com.example.unisync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.ui.screens.TaskScreen
import com.example.smartcompanionapp.ui.screens.CampusInfoScreen
import com.example.smartcompanionapp.ui.screens.DashboardScreen

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

        composable("dash") {
            DashboardScreen(
                onNavigateToInfo  = {
                    navController.navigate("infoModule")
                }
            )
        }

        composable("infoModule") {
            CampusInfoScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
