package com.example.smartcompanionapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.database.tasks.TaskDatabase
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.data.repository.TaskRepository
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.screens.*
import com.example.smartcompanionapp.ui.theme.AppSurface
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import com.example.smartcompanionapp.viewmodel.TaskViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore


sealed class Screen(val route: String) {
    object GetStarted : Screen("get_started")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")
    object CampusInformation : Screen("campusInfo")
    object Task : Screen("task")
    object Options : Screen("settings")
    object AllAnnouncements : Screen("all_announcements")

    object Notifications : Screen("notifications")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.GetStarted.route
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // ── VIEW MODEL INITIALIZATIONS ───────────────────────────────────────────
    
    // 1. Dashboard ViewModel
    val database = AppDatabase.getDatabase(context)
    val repository = remember { AnnouncementRepository(database.announcementDao()) }
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DashboardViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.GetStarted.route) {
            GetStartedScreen(
                onLogin = { navController.navigate(Screen.Login.route) },
                onSignUp = { navController.navigate(Screen.Signup.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Signup.route) {
            SignUpScreen(navController)
        }
        
        // ── TASK-RELATED SCREENS (Isolated by UserId) ────────────────────────
        
        composable(Screen.Dashboard.route) {
            val currentUserId = sessionManager.getUsername() ?: "guest"
            val taskDatabase = remember { TaskDatabase.getDatabase(context) }
            val firestore = remember { FirebaseFirestore.getInstance() }
            val taskRepository = remember { TaskRepository(taskDatabase.taskDao(), firestore) }
            val taskViewModel: TaskViewModel = viewModel(
                key = currentUserId,
                factory = TaskViewModelFactory(taskRepository, currentUserId)
            )
            DashboardScreen(
                navController = navController,
                viewModel = dashboardViewModel,
                taskViewModel = taskViewModel,
                onViewAllClick = {
                    navController.navigate(Screen.AllAnnouncements.route)
                }
            )
        }
        composable(Screen.AllAnnouncements.route) {
            AllAnnouncementsScreen(navController, dashboardViewModel)
        }
        composable(Screen.Schedule.route) {
            val currentUserId = sessionManager.getUsername() ?: "guest"
            val taskDatabase = remember { TaskDatabase.getDatabase(context) }
            val firestore = remember { FirebaseFirestore.getInstance() }
            val taskRepository = remember { TaskRepository(taskDatabase.taskDao(), firestore) }
            val taskViewModel: TaskViewModel = viewModel(
                key = currentUserId, // Creates a NEW ViewModel instance per user
                factory = TaskViewModelFactory(taskRepository, currentUserId)
            )
            ScheduleScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.CampusInformation.route) {
            CampusInfoScreen(navController)
        }
        composable(Screen.Task.route) {
            val currentUserId = sessionManager.getUsername() ?: "guest"
            val taskDatabase = remember { TaskDatabase.getDatabase(context) }
            val firestore = remember { FirebaseFirestore.getInstance() }
            val taskRepository = remember { TaskRepository(taskDatabase.taskDao(), firestore) }
            val taskViewModel: TaskViewModel = viewModel(
                key = currentUserId,
                factory = TaskViewModelFactory(taskRepository, currentUserId)
            )
            TaskScreen(navController, taskViewModel)
        }
        composable(Screen.Options.route) {
            SettingsScreen(navController)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController)
        }
    }
}

@Composable
fun CampusBottomNav(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = {
                if (currentRoute != Screen.Dashboard.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Schedule.route,
            onClick = {
                if (currentRoute != Screen.Schedule.route) {
                    navController.navigate(Screen.Schedule.route)
                }
            },
            icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Task.route,
            onClick = {
                if (currentRoute != Screen.Task.route) {
                    navController.navigate(Screen.Task.route)
                }
            },
            icon = { Icon(Icons.Rounded.Checklist, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.CampusInformation.route,
            onClick = {
                if (currentRoute != Screen.CampusInformation.route) {
                    navController.navigate(Screen.CampusInformation.route)
                }
            },
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Campus Info") },
            label = { Text("Info") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Options.route,
            onClick = {
                if (currentRoute != Screen.Options.route) {
                    navController.navigate(Screen.Options.route)
                }
            },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
