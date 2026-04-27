package com.example.smartcompanionapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.domain.TaskUiState
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    taskViewModel: TaskViewModel,
    onViewAllClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val taskState by taskViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val username = sessionManager.getUsername() ?: "User"

    // Animation state to trigger staggered entrance
    var startAnimation by remember { mutableStateOf(false) }
    
    // Trigger animation after the first composition or when loading finishes
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            startAnimation = true
        }
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = { CampusBottomNav(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Header Section
            item {
                AnimatedEntrance(visible = startAnimation, delay = 100) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(PrimaryGradientHorizontal)
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        StudentHeader(
                            username = username,
                            onProfileClick = { navController.navigate(Screen.Options.route) }
                        )
                    }
                }
            }

            // 2. Banner
            item {
                AnimatedVisibility(
                    visible = state.topAnnouncement != null && startAnimation,
                    enter = fadeIn(tween(500, 200)) + expandVertically(tween(500, 200)),
                    exit = shrinkVertically()
                ) {
                    state.topAnnouncement?.let {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            AnnouncementBanner(
                                announcement = it,
                                onDismiss = { viewModel.processIntent(DashboardIntent.DismissAnnouncement(it.id)) }
                            )
                        }
                    }
                }
            }

            // 3. Upcoming Task Card
            item {
                AnimatedEntrance(visible = startAnimation, delay = 300) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        val tasks = (taskState as? TaskUiState.Success)?.tasks ?: emptyList()
                        val nextTask = tasks.firstOrNull()
                        UpcomingTaskCard(nextTask)
                    }
                }
            }

            // 4. Campus News Section
            item {
                AnimatedEntrance(visible = startAnimation, delay = 400) {
                    Column {
                        SectionTitle(title = "Campus News", action = "View All", onActionClick = onViewAllClick)
                        Spacer(modifier = Modifier.height(12.dp))
                        AnnouncementList(newsList = state.campusNews)
                    }
                }
            }

            // 5. Deadlines Section
            item {
                AnimatedEntrance(visible = startAnimation, delay = 500) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Column {
                            Text("Upcoming Deadlines", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(16.dp))

                            when (val tasks = taskState) {
                                is TaskUiState.Success -> {
                                    if (tasks.tasks.isEmpty()) {
                                        EmptyStateText("No upcoming deadlines")
                                    } else {
                                        tasks.tasks.take(3).forEach { task ->
                                            DeadlineItem(task.title, task.dueDate, AuroraSoftTeal)
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                                is TaskUiState.Loading -> {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AuroraVividPurple)
                                }
                                else -> {
                                    Text("Unable to load deadlines", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable component for staggering the entrance of UI elements.
 */
@Composable
fun AnimatedEntrance(
    visible: Boolean,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = delay)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 600, delayMillis = delay)
                )
    ) {
        content()
    }
}

@Composable
fun StudentHeader(username: String, onProfileClick: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val googlePhotoUrl = currentUser?.photoUrl?.toString()
    val localPhotoUri = sessionManager.getProfileImage()
    
    val role = sessionManager.getRole() ?: "user"
    val rawCourse = sessionManager.getCourse()
    
    val headerSubtext = when {
        role == "admin" -> "Administrator"
        rawCourse == null -> "Student • Access Granted"
        else -> rawCourse
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            val dateFormat = SimpleDateFormat("MMM dd, EEEE", Locale.getDefault())
            Text(dateFormat.format(Date()), fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hi, $username",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = headerSubtext,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .size(60.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(2.dp, AuroraSoftTeal.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (googlePhotoUrl != null || localPhotoUri != null) {
                AsyncImage(
                    model = googlePhotoUrl ?: localPhotoUri,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Rounded.Person, "Profile", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun EmptyStateText(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun SectionTitle(title: String, action: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(
            text = action,
            fontSize = 14.sp,
            color = AuroraVividPurple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable(onClick = onActionClick)
                .padding(8.dp)
        )
    }
}

@Composable
fun AnnouncementBanner(announcement: Announcement, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuroraSoftTeal.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Notifications, "Alert", tint = AuroraVividPurple)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AuroraDeepIndigo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = announcement.content,
                    fontSize = 12.sp,
                    color = AuroraDeepIndigo.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, "Dismiss", tint = AuroraDeepIndigo)
            }
        }
    }
}

@Composable
fun AnnouncementList(newsList: List<Announcement>) {
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    val configuration = LocalConfiguration.current
    val responsiveCardWidth = (configuration.screenWidthDp * 0.75).dp

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(newsList) { news ->
            val dateString = dateFormat.format(Date(news.datePosted))
            AnnouncementCard(
                category = "Campus News",
                title = news.title,
                date = dateString,
                color = AuroraSoftTeal.copy(alpha = 0.1f),
                textColor = AuroraDeepIndigo,
                modifier = Modifier.width(responsiveCardWidth)
            )
        }
    }
}

@Composable
fun UpcomingTaskCard(task: Task?) {
    val gradient = Brush.linearGradient(colors = listOf(AuroraDeepIndigo, AuroraVividPurple))
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            if (task == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.TaskAlt, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No upcoming tasks", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Enjoy your free time!", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            } else {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AuroraSoftTeal,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next Task", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = AuroraDeepIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(task.dueDate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(task.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(task.subject, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Description, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(task.description, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(category: String, title: String, date: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContainerBadge(text = category, bgColor = color, textColor = textColor)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text(date, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun DeadlineItem(task: String, due: String, indicatorColor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(indicatorColor))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(task, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Due: $due", fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ContainerBadge(text: String, bgColor: Color, textColor: Color) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bgColor).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
    }
}
