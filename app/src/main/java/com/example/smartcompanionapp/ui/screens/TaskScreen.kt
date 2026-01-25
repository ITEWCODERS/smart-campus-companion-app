package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Task
import com.example.smartcompanionapp.ui.theme.AppBackground


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.smartcompanionapp.ui.theme.AppSurface
import com.example.smartcompanionapp.ui.theme.TextPrimary
import com.example.smartcompanionapp.ui.theme.TextSecondary
import com.example.smartcompanionapp.ui.theme.UniAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppBackground
        )
    )
}

@Composable
fun TaskScreen(navController: NavController) {

    val tasks = listOf(
        Task("Finish Android Assignment", "Today, 11:59 PM"),
        Task("Prepare for Exam", "Jan 22, 10:00 AM"),
        Task("Submit Project Report", "Jan 25, 5:00 PM")
    )

    Scaffold(
        topBar = {
            TaskTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Do Nothing for now */ },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddTask,
                    contentDescription = "Add Task"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            items(tasks) { task ->
                TaskCard(task)
            }
        }
    }
}


@Composable
fun TaskCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(UniAccent)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}


//
//@Composable
//fun TaskItem(task: Task) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
//            Text(text = "Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}
