package com.example.smartcompanionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme
import com.example.unisync.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCompanionAppTheme {
                // 1 Create NavController
                val navController = rememberNavController()

                // 2 Pass it to AppNavigation
                AppNavigation(navController)
            }
        }
    }
}
