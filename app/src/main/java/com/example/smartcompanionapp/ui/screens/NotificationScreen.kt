package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Part 1: Initialize states from persistent storage
    var announcements by remember { mutableStateOf(sessionManager.isAnnouncementsEnabled()) }
    var deadlines by remember { mutableStateOf(sessionManager.isDeadlinesEnabled()) }
    var classReminders by remember { mutableStateOf(sessionManager.isClassRemindersEnabled()) }
    var events by remember { mutableStateOf(sessionManager.isEventsEnabled()) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Notification preferences",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item { NotificationToggle("Campus Announcements", announcements) { announcements = it } }
            item { NotificationToggle("Deadlines & Assignments", deadlines) { deadlines = it } }
            item { NotificationToggle("Class Reminders", classReminders) { classReminders = it } }
            item { NotificationToggle("Events & Activities", events) { events = it } }
        }
    }
}

@Composable
private fun NotificationToggle(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = AppSurface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = null,
                tint = AuroraSoftTeal,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = TextPrimary
            )
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = UniPrimary,
                    checkedTrackColor = UniPrimary.copy(alpha = 0.38f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationsScreen(navController = androidx.navigation.compose.rememberNavController())
}
