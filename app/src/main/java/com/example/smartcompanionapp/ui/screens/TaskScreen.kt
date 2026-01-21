package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcompanionapp.model.Task
import com.example.smartcompanionapp.ui.theme.AppSurface
import com.example.smartcompanionapp.ui.theme.TextPrimary
import com.example.smartcompanionapp.ui.theme.TextSecondary

@Composable
fun TaskScreen() {
    // ✅ Updated tasks list to include `day` (matches Task data class)
    val tasks = listOf(
        Task("Finish Android Assignment", "Jan 20", "Mon"),
        Task("Prepare for Exam", "Jan 22", "Wed"),
        Task("Submit Project Report", "Jan 25", "Fri")
    )

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
    // ✅ Changed Card background and added day indicator
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .background(AppSurface, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Added day indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF6200EE), shape = RoundedCornerShape(8.dp)), // purple indicator
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = task.day,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
