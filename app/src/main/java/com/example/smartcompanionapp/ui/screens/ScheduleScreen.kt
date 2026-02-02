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
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
import com.example.smartcompanionapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {

    val tasks = remember {
        listOf(
            Tasks("Physics Lab", "10:00 AM", "20"),
            Tasks("Data Structures", "1:00 PM", "21"),
            Tasks("Group Meeting", "3:30 PM", "22"),
            Tasks("Math Quiz", "9:00 AM", "23")
        )
    }

    var currentMonth by remember { mutableStateOf("Jan 2026") }
    var selectedDay by remember { mutableStateOf("20") }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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

            CalendarGrid(selectedDay = selectedDay, onDaySelected = { selectedDay = it })

            Spacer(modifier = Modifier.height(16.dp))

            val filteredTasks = tasks.filter { it.date == selectedDay }

            if (filteredTasks.isEmpty()) {
                NoTasksPlaceholder()
            } else {
                TaskList(tasks = filteredTasks)
            }
        }
    }
}

@Composable
fun MonthNavigation(currentMonth: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous Month")
        }
        Text(currentMonth, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next Month")
        }
    }
}

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
fun TaskList(tasks: List<Tasks>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            ScheduleCard(task)
        }
    }
}

@Composable
fun ScheduleCard(task: Tasks) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                Text("Due: ${task.dueDate}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun NoTasksPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("No tasks scheduled", color = TextSecondary)
        }
    }
}
