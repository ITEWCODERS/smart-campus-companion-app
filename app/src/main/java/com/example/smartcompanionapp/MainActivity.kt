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
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            
            val isDarkModePref by sessionManager.isDarkModeFlow.collectAsState()
            val isNotificationsEnabled by sessionManager.isNotificationsEnabledFlow.collectAsState()
            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            // REACITIVE INTEGRATION: Enable/Disable system features based on Master Toggle
            LaunchedEffect(isNotificationsEnabled) {
                if (isNotificationsEnabled) {
                    Log.d("NotificationSync", "Notifications enabled: Subscribing and Scheduling...")
                    FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) Log.d("FCM", "Subscribed to announcements")
                        }
                    AnnouncementWorkScheduler.schedule(context)
                } else {
                    Log.d("NotificationSync", "Notifications disabled: Unsubscribing and Cancelling...")
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) Log.d("FCM", "Unsubscribed from announcements")
                        }
                    AnnouncementWorkScheduler.cancel(context)
                }
            }

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
