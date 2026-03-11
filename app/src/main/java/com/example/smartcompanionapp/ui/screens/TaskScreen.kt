package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Task
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
import com.example.smartcompanionapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Tasks", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun TaskScreen(navController: NavController) {
    //Static list of tasks
    val tasks = remember {
        mutableStateListOf(
            Task("Finish Assignment 1", "Today, 11:59 PM"),
            Task("Prepare for Exam", "Feb 4, 10:00 AM"),
            Task("Submit Project for APPDEV", "Jan 25, 5:00 PM")
        )
    }

    Scaffold(
        topBar = { TaskTopBar { navController.popBackStack() } },
        bottomBar = { CampusBottomNav(navController) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        //List of tasks
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    onDelete = {
                        //Deletes a selected task
                        tasks.remove(task)
                    },
                    onEdit = {
                        // No function yet
                    }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    //Card for tasks
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            //Buttons for Edit & Delete
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
