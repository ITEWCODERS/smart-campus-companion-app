package com.example.smartcompanionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.data.SessionManager
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme

class MainActivity : ComponentActivity() {
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

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()
    
    val startDestination = if (sessionManager.isLoggedIn()) {
        "home/${sessionManager.getUsername()}/${sessionManager.getRole()}"
    } else {
        "get_started"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("get_started") {
            GetStartedScreen(
                onLogin = { navController.navigate("login") },
                onSignUp = { navController.navigate("signup") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    sessionManager.saveSession(user.username, user.role.name)
                    navController.navigate("home/${user.username}/${user.role}") {
                        popUpTo("get_started") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        composable("home/{username}/{role}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "User"
            val role = backStackEntry.arguments?.getString("role") ?: "USER"
            HomeScreen(username, role) {
                sessionManager.clearSession()
                navController.navigate("login") {
                    popUpTo("home/{username}/{role}") { inclusive = true }
                }
            }
        }
    }
}
