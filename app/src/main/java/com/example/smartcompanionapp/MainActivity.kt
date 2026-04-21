package com.example.smartcompanionapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

/**
 * STEP 13 — MAIN ACTIVITY
 *
 * CHANGES from original:
 *
 * 1. REQUEST POST_NOTIFICATIONS PERMISSION (Android 13+)
 *    Without this permission, no notifications appear on API 33+.
 *    We use the ActivityResultContracts launcher pattern (the modern approach).
 *    The request is shown ONCE on first launch. The WorkManager still schedules
 *    even if permission is denied; it will post notifications if permission is
 *    later granted by the user in Settings.
 *
 * 2. SCHEDULE WORKMANAGER ON LAUNCH
 *    AnnouncementWorkScheduler.schedule() is called in onCreate().
 *    This ensures the periodic background sync starts immediately after install
 *    or after the app is updated/reinstalled.
 *    KEEP policy in the scheduler prevents duplicate jobs on subsequent opens.
 *
 * Everything else (dark theme, session check) is unchanged from original.
 */
class MainActivity : ComponentActivity() {

    // ── PERMISSION LAUNCHER (Android 13+) ────────────────────────────────────
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Granted → WorkManager will be able to post notifications.
        // Denied  → WorkManager still runs but notifications are suppressed by OS.
        // We don't show a rationale here to keep UX simple; users can enable in Settings.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ── 1. REQUEST NOTIFICATION PERMISSION ───────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Show system permission dialog
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // ── 2. SCHEDULE WORKMANAGER PERIODIC SYNC ────────────────────────────
        // ExistingPeriodicWorkPolicy.KEEP means this is safe to call every time.
        // If a job is already scheduled, it is left untouched.
        AnnouncementWorkScheduler.schedule(this)

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
        navController    = navController,
        startDestination = startDestination
    )
}