package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Tasks
import com.example.smartcompanionapp.ui.theme.*

/**
 * Main Schedule Screen displaying a calendar and tasks for the selected day.
 * @param navController Controller for navigation between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {

    // Sample tasks (in production, this should come from a repository / ViewModel)
    val tasks = remember {
        listOf(
            Tasks("Physics Lab", "10:00 AM", "20"),
            Tasks("Data Structures", "1:00 PM", "21"),
            Tasks("Group Meeting", "3:30 PM", "22"),
            Tasks("Math Quiz", "9:00 AM", "23")
        )
    }

    // State for current month and selected day
    var currentMonth by remember { mutableStateOf("Jan 2026") }
    var selectedDay by remember { mutableStateOf("20") }

    Scaffold(
        containerColor = AppBackground, // Screen background color
        topBar = {
            ScheduleTopAppBar(navController)
        },
        bottomBar = { BottomNavWithController(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            MonthNavigation(
                currentMonth = currentMonth,
                onPrevious = { /* TODO: implement previous month logic */ },
                onNext = { /* TODO: implement next month logic */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid (7 columns for 7 days a week)
            CalendarGrid(selectedDay = selectedDay, onDaySelected = { selectedDay = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Filter tasks by selected day
            val filteredTasks = tasks.filter { it.date == selectedDay }

            if (filteredTasks.isEmpty()) {
                NoTasksPlaceholder()
            } else {
                TaskList(tasks = filteredTasks)
            }
        }
    }
}

/** Top App Bar for the Schedule screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTopAppBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Schedule") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

/** Month navigation row with previous and next buttons */
@Composable
fun MonthNavigation(currentMonth: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("<", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onPrevious() })
        Text(currentMonth, fontWeight = FontWeight.Bold)
        Text(">", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNext() })
    }
}

/** Calendar grid showing 30 days and highlighting the selected day */
@Composable
fun CalendarGrid(selectedDay: String, onDaySelected: (String) -> Unit) {
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
                onClick = { onDaySelected(day) }
            )
        }
    }
}

/** Individual day in the calendar */
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

/** LazyColumn for displaying a list of tasks */
@Composable
fun TaskList(tasks: List<Tasks>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            ScheduleCard(task)
        }

    }
}

/** Card representing a single task */
@Composable
fun ScheduleCard(task: Tasks) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small dot indicator
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

/** Placeholder when no tasks are scheduled for the selected day */
@Composable
fun NoTasksPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No tasks scheduled", color = TextSecondary)
    }
}

/** Bottom navigation bar for the app */
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
