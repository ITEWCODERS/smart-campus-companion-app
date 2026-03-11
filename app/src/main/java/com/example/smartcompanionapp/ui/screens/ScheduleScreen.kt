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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Tasks
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
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
        containerColor = MaterialTheme.colorScheme.background, // Theme background color
        topBar = {
            ScheduleTopAppBar(navController)
        },
        bottomBar = { CampusBottomNav(navController) }
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
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
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
        Text("<", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onPrevious() })
        Text(currentMonth, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(">", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onNext() })
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
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
            ScheduleTaskCard(task)
        }

    }
}

/** Card representing a single task */
@Composable
fun ScheduleTaskCard(task: Tasks) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small dot indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(task.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(task.dueDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text("No tasks scheduled", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    ScheduleScreen(navController = androidx.navigation.compose.rememberNavController())
}
