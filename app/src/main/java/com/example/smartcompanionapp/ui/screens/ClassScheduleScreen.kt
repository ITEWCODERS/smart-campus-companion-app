package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartcompanionapp.ui.theme.SmartCompanionAppTheme

// --------------------
// Mock Data Model
// --------------------
data class Schedule(
    val title: String,
    val time: String,
    val description: String
)

// --------------------
// Mock Data List
// --------------------
val mockSchedules = listOf(
    Schedule(
        title = "Morning Exercise",
        time = "6:00 AM - 7:00 AM",
        description = "Jogging and stretching"
    ),
    Schedule(
        title = "Online Class",
        time = "9:00 AM - 11:00 AM",
        description = "Mobile App Development"
    ),
    Schedule(
        title = "Lunch Break",
        time = "12:00 PM - 1:00 PM",
        description = "Eat and rest"
    ),
    Schedule(
        title = "Project Work",
        time = "2:00 PM - 5:00 PM",
        description = "Smart Companion App"
    )
)

// --------------------
// Schedule Screen UI
// --------------------
@Composable
fun ClassScheduleScreen() { // Renamed to avoid confusion with ScheduleScreen.kt
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Today's Schedule",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(mockSchedules) { schedule ->
                ScheduleCard(schedule)
            }
        }
    }
}

// --------------------
// Schedule Card UI
// --------------------
@Composable
fun ScheduleCard(schedule: Schedule) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = schedule.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = schedule.time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = schedule.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --------------------
// Preview
// --------------------
@Preview(showBackground = true)
@Composable
fun ClassScheduleScreenPreview() {
    SmartCompanionAppTheme {
        ClassScheduleScreen()
    }
}
