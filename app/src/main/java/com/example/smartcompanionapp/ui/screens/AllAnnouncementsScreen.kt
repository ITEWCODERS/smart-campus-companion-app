package com.example.smartcompanionapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAnnouncementsScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val announcements by viewModel.allAnnouncements.collectAsState()

    // ── ADD ANNOUNCEMENT DIALOG STATE ─────────────────────────────────────────
    var showAddDialog       by remember { mutableStateOf(false) }
    var newTitle            by remember { mutableStateOf("") }
    var newContent          by remember { mutableStateOf("") }
    var titleError          by remember { mutableStateOf(false) }
    var contentError        by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Announcement added successfully!")
            showSuccessSnackbar = false
        }
    }

    // ── ADD ANNOUNCEMENT DIALOG ───────────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newTitle      = ""
                newContent    = ""
                titleError    = false
                contentError  = false
            },
            containerColor = AppSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AddAlert,
                        contentDescription = null,
                        tint     = UniAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New Announcement",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = newTitle,
                        onValueChange = { newTitle = it; titleError = false },
                        label         = { Text("Title") },
                        placeholder   = { Text("e.g. Enrollment open for AY 2025–26") },
                        isError       = titleError,
                        supportingText = if (titleError) {
                            { Text("Title cannot be empty", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = UniAccent,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                            focusedLabelColor    = UniAccent
                        )
                    )
                    OutlinedTextField(
                        value         = newContent,
                        onValueChange = { newContent = it; contentError = false },
                        label         = { Text("Content") },
                        placeholder   = { Text("Write the announcement details here…") },
                        isError       = contentError,
                        supportingText = if (contentError) {
                            { Text("Content cannot be empty", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        minLines      = 4,
                        maxLines      = 6,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = UniAccent,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                            focusedLabelColor    = UniAccent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        titleError   = newTitle.isBlank()
                        contentError = newContent.isBlank()
                        if (!titleError && !contentError) {
                            viewModel.processIntent(
                                DashboardIntent.AddAnnouncement(
                                    Announcement(
                                        title      = newTitle.trim(),
                                        content    = newContent.trim(),
                                        datePosted = System.currentTimeMillis(),
                                        isRead     = false
                                    )
                                )
                            )
                            newTitle          = ""
                            newContent        = ""
                            showAddDialog     = false
                            showSuccessSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UniPrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        newTitle      = ""
                        newContent    = ""
                        titleError    = false
                        contentError  = false
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ── MAIN SCAFFOLD ─────────────────────────────────────────────────────────
    Scaffold(
        containerColor = AppBackground,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Campus Announcements", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processIntent(DashboardIntent.TriggerSync) }
                    ) {
                        Icon(Icons.Rounded.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppSurface)
            )
        },
        // ONLY SHOW FAB IF ADMIN
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = UniPrimary,
                contentColor   = AppBackground
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Announcement")
            }
        }
    ) { innerPadding ->

        if (announcements.isEmpty()) {
            Box(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment  = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Campaign, null,
                        modifier = Modifier.size(72.dp),
                        tint     = UniAccent.copy(alpha = 0.35f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No announcements yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap + to add one or refresh to sync", style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding  = PaddingValues(vertical = 16.dp)
            ) {
                val unreadCount = announcements.count { !it.isRead }
                if (unreadCount > 0) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(UniPrimary))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$unreadCount unread",
                                style      = MaterialTheme.typography.labelMedium,
                                color      = UniPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

        if (showSheet && state.isAdmin) {
            AddAnnouncementBottomSheet(
                onDismiss = { showSheet = false },
                onPost = { title, content ->
                    val newAnnouncement = Announcement(
                        title = title,
                        content = content,
                        datePosted = System.currentTimeMillis(),
                        isRead = false
                items(announcements, key = { it.id }) { announcement ->
                    AnnouncementListCard(
                        announcement = announcement,
                        onMarkAsRead = {
                            viewModel.processIntent(DashboardIntent.MarkAsRead(announcement.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FullWidthAnnouncementCard(
    news: Announcement,
    isAdmin: Boolean, // New parameter
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(news.datePosted))

            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UniPrimary,
                    modifier = Modifier.weight(1f)
                )

                // ONLY SHOW DELETE BUTTON IF ADMIN
                if (isAdmin) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Announcement",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementBottomSheet(
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Post New Announcement",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onPost(title, content) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UniPrimary),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Post Announcement", color = Color.White)
            }
        }
    }
}