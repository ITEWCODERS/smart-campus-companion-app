package com.example.smartcompanionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.navigation.AppNavigation
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme
import com.example.smartcompanionapp.worker.AnnouncementWorkScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            // Ensure Firebase is initialized
            FirebaseApp.initializeApp(this)

        setContent {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            
            val isDarkModePref by sessionManager.isDarkModeFlow.collectAsState()
            val isNotificationsEnabled by sessionManager.isNotificationsEnabledFlow.collectAsState()
            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            // Handle Notifications enabling/disabling reactively
            LaunchedEffect(isNotificationsEnabled) {
                if (isNotificationsEnabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) android.util.Log.d("FCM", "Subscribed to announcements")
                        }
                    AnnouncementWorkScheduler.schedule(context)
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) android.util.Log.d("FCM", "Unsubscribed from announcements")
                        }
                    AnnouncementWorkScheduler.cancel(context)
                }

            // Start the background sync worker safely
            AnnouncementWorkScheduler.schedule(this)
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Initialization error: ${e.message}")
        }

        setContent {
            val context        = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            val isDarkModePref by sessionManager.isDarkModeFlow.collectAsState()
            val useDarkTheme   = isDarkModePref ?: isSystemInDarkTheme()

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
