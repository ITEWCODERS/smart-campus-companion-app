package com.example.smartcompanionapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.navigation.AppNavigation
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            val isDarkModePref by sessionManager.isDarkModeFlow.collectAsState()
            
            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()
            
            SmartCompanionAppTheme(darkTheme = useDarkTheme) {
                MainApp(sessionManager)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(sessionManager: SessionManager) {
    val navController = rememberNavController()

    // Check if user is logged in
    val startDestination = if (sessionManager.isLoggedIn()) {
        Screen.Dashboard.route
    } else {
        Screen.GetStarted.route
    }

    AppNavigation(
        navController = navController,
        startDestination = startDestination
    )
}
