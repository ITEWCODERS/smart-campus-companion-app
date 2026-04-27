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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.ui.theme.*
import com.google.firebase.messaging.FirebaseMessaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {

    var announcements  by remember { mutableStateOf(true) }
    var deadlines      by remember { mutableStateOf(true) }
    var classReminders by remember { mutableStateOf(true) }
    var events         by remember { mutableStateOf(false) }

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
                    text     = "Notification Preferences",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                NotificationToggle(
                    label    = "Campus Announcements",
                    // WorkManager is gone — the toggle now controls FCM topic subscription.
                    // Unsubscribing means the device won't receive push notifications
                    // for new announcements (the in-app real-time listener still works
                    // when the app is open, but OS tray notifications are suppressed).
                    subtitle = "Push notifications for new announcements",
                    checked  = announcements,
                    onChange = { enabled ->
                        announcements = enabled
                        if (enabled) {
                            FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                        } else {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements")
                        }
                    }
                )
            }
            item {
                NotificationToggle(
                    label    = "Deadlines & Assignments",
                    subtitle = "Reminders before due dates",
                    checked  = deadlines,
                    onChange = { deadlines = it }
                )
            }
            item {
                NotificationToggle(
                    label    = "Class Reminders",
                    subtitle = "15 minutes before class starts",
                    checked  = classReminders,
                    onChange = { classReminders = it }
                )
            }
            item {
                NotificationToggle(
                    label    = "Events & Activities",
                    subtitle = "Campus events and club activities",
                    checked  = events,
                    onChange = { events = it }
                )
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    label    : String,
    subtitle : String,
    checked  : Boolean,
    onChange : (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = AppSurface
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = null,
                tint     = UniAccent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = label,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text  = subtitle,
                    color = TextSecondary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Switch(
                checked         = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = UniPrimary,
                    checkedTrackColor = UniPrimary.copy(alpha = 0.38f)
                )
            )
        }
    }
}