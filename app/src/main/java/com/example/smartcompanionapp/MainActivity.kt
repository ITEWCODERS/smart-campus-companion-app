package com.example.smartcompanionapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.data.database.tasks.TaskDatabase
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.navigation.AppNavigation
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import com.example.smartcompanionapp.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCompanionAppTheme {
                MainApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()

    val database = remember {
        TaskDatabase.getDatabase(context)
    }

    val taskViewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TaskViewModelFactory(database.taskDao())
    )

    // Changed to always start at GetStarted for fresh launches if desired.
    // Alternatively, to clear session on force stop, you'd need a different approach (e.g., non-persistent storage).
    val startDestination = Screen.GetStarted.route

    AppNavigation(
        navController = navController,
        startDestination = startDestination,
        taskViewModel = taskViewModel
    )
}
