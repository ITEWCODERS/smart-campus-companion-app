package com.example.smartcompanionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.ui.screens.TaskScreen
import com.example.unisync.ui.screens.CampusInfoScreen
import com.example.unisync.ui.screens.DashboardScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dash"
    ) {
        composable("tasks") {
            TaskScreen()
        }

        composable("dash") { //IGNORE, TESTING PURPOSES ONLY.
            DashboardScreen(
                // Assuming you added back button logic to CampusInfoScreen
                onNavigateToInfo  = {
                    navController.navigate("infoModule")
                }
            )
        }

        composable("infoModule") { //IGNORE, TESTING PURPOSES ONLY.
            CampusInfoScreen(
                onBackClick = {
                    navController.popBackStack() // <--- This makes the button work
                }
            )
        }
    }
}
