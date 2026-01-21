package com.example.unisync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Task
import com.example.smartcompanionapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {

    val tasks = listOf(
        Task("Physics Lab", "10:00 AM", "20"),
        Task("Data Structures", "1:00 PM", "21"),
        Task("Group Meeting", "3:30 PM", "22"),
        Task("Math Quiz", "9:00 AM", "23")
    )

    var currentMonth by remember { mutableStateOf("Jan 2026") }
    var selectedDay by remember { mutableStateOf("20") }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        // ✅ Only include one bottom nav
        bottomBar = { BottomNavWithController(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("<", fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                    // TODO: previous month
                })
                Text(currentMonth, fontWeight = FontWeight.Bold)
                Text(">", fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                    // TODO: next month
                })
            }

            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(30) { index ->
                    val day = (index + 1).toString()
                    CalendarDay(
                        day = day,
                        isSelected = selectedDay == day,
                        onClick = { selectedDay = day }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredTasks = tasks.filter { it.day == selectedDay }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks scheduled", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks) { task ->
                        ScheduleCard(task)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isSelected) UniPrimary else AppSurface,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ScheduleCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(UniPrimary, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(task.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(task.dueDate, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

// ✅ Unified Bottom Nav
@Composable
fun BottomNavWithController(navController: NavController) {
    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("dashboard") },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("schedule") },
            icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.School, contentDescription = "Grades") },
            label = { Text("Grades") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Information") },
            label = { Text("Information") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
