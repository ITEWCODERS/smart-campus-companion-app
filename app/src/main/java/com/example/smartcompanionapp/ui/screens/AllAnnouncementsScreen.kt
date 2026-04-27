package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAnnouncementsScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val state by viewModel.state.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userRole = sessionManager.getRole()
    val isAdmin = userRole == "admin"

    // ── MAIN SCAFFOLD ─────────────────────────────────────────────────────────
    Scaffold(
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Campus Announcements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        // ONLY SHOW FAB IF ADMIN
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = AuroraVividPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add News")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.campusNews.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No announcements yet", color = TextSecondary)
                    }
                }
            } else {
                items(state.campusNews) { news ->
                    FullWidthAnnouncementCard(
                        news = news,
                        isAdmin = isAdmin,
                        onDelete = {
                            viewModel.processIntent(DashboardIntent.DeleteAnnouncement(news))
                            scope.launch { snackbarHostState.showSnackbar("Announcement removed") }
                        },
                        onMarkAsRead = {
                            viewModel.processIntent(DashboardIntent.MarkAsRead(news.id))
                        }
                    )
                }
            }
        }

        if (showSheet && isAdmin) {
            AddAnnouncementBottomSheet(
                onDismiss = { showSheet = false },
                onPost = { title, content ->
                    val newAnnouncement = Announcement(
                        title = title,
                        content = content,
                        datePosted = System.currentTimeMillis(),
                        isRead = false
                    )
                    viewModel.processIntent(DashboardIntent.AddAnnouncement(newAnnouncement))
                    showSheet = false
                    scope.launch { snackbarHostState.showSnackbar("Announcement posted for all students") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementBottomSheet(
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Post New Announcement", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Announcement Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AuroraVividPurple,
                    unfocusedLabelColor = TextSecondary
                )
            )

            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("Details") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AuroraVividPurple,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onPost(title, content) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuroraVividPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Broadcast to Students", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FullWidthAnnouncementCard(
    news: Announcement,
    isAdmin: Boolean,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(news.datePosted))

    val isRead = news.isRead
    val containerAlpha = if (isRead) 0.75f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = containerAlpha },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRead) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isRead) TextSecondary else AuroraVividPurple
                        )
                        if (!news.isRead) {
                            Spacer(Modifier.width(8.dp))
                            Surface(color = AuroraSoftTeal, shape = RoundedCornerShape(4.dp)) {
                                Text("NEW", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AuroraDeepIndigo)
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dateString, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        
                        if (isRead) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Read",
                                tint = AuroraSoftTeal.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Already Read",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraSoftTeal.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Unread",
                                tint = AuroraVividPurple,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                " Mark as Read",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AuroraVividPurple,
                                modifier = Modifier.clickable { onMarkAsRead() }
                            )
                        }
                    }
                }
                
                Row {
                    // Only for unread items and for students
                    if (!isAdmin && !isRead) {
                        IconButton(onClick = onMarkAsRead) {
                            Icon(Icons.Default.Check, "Mark as Read", tint = AuroraSoftTeal)
                        }
                    }
                    
                    // ONLY ADMINS see delete button
                    if (isAdmin) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = news.content,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isRead) TextSecondary else TextPrimary,
                lineHeight = 24.sp
            )
        }
    }
}
