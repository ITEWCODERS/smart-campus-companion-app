package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Task
import com.example.smartcompanionapp.ui.theme.AppSurface

@Composable
fun TaskScreen(navController: NavController) {
    val tasks = listOf(
        Task("Finish Android Assignment", "Jan 20"),
        Task("Prepare for Exam", "Jan 22"),
        Task("Submit Project Report", "Jan 25")
    )

    Scaffold(
        bottomBar = { BottomNavWithController(navController) },
        containerColor = Color(0xFFF0F0F0)
    ) { innerPadding ->

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tasks",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tasks) { task ->
                TaskItem(task)
            }
        }
    }
}


@Composable
fun TaskItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun BottomTaskNavWithController(navController: NavController) {
    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        // 1. Home
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("dashboard") },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )


        //changed to nav controller to navigate
        // 2. Schedule
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("schedule") },
            icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )

        // 3. Grades
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.School, contentDescription = "Academics") },
            label = { Text("Grades") }
        )
        // 4. Task
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("task") },
            icon = { Icon(Icons.Rounded.Task, contentDescription = "Campus Info") },
            label = { Text("Task") }
        )

        // 5. Campus Info
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("campusInfo") },
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Campus Info") },
            label = { Text("Info") }
        )
    }

    @Composable
    fun BottomNavWithController(navController: NavController) {
        NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
            // 1. Home
            NavigationBarItem(
                selected = true,
                onClick = { navController.navigate("dashboard") },
                icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )


            //changed to nav controller to navigate
            // 2. Schedule
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("schedule") },
                icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
                label = { Text("Schedule") }
            )

            // 3. Grades
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Icon(Icons.Rounded.School, contentDescription = "Academics") },
                label = { Text("Grades") }
            )
            // 4. Task
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("task") },
                icon = { Icon(Icons.Rounded.Task, contentDescription = "Campus Info") },
                label = { Text("Task") }
            )

            // 5. Campus Info
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("campusInfo") },
                icon = { Icon(Icons.Rounded.Info, contentDescription = "Campus Info") },
                label = { Text("Info") }
            )
        }
    }
}
