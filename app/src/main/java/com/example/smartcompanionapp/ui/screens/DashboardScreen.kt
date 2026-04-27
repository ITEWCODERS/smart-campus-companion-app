package com.example.smartcompanionapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.draw.shadow
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.absoluteValue

@RequiresApi(Build.VERSION_CODES.O)
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

    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(50) // Tiny delay ensures animations don't skip the first frame
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
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // --- HEADER ---
            item {
                AnimatedEntrance(visible = startAnimation, delay = 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(AuroraDeepIndigo, AuroraVividPurple)
                                )
                            )
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

            // --- IMPORTANT ALERT BANNER ---
            item {
                AnimatedVisibility(
                    visible = state.topAnnouncement != null && startAnimation,
                    modifier = Modifier.fillMaxWidth(),
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
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

            // --- UPCOMING TASK ---
            item {
                AnimatedEntrance(visible = startAnimation, delay = 150) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        val tasks = (taskState as? TaskUiState.Success)?.tasks ?: emptyList()
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val today = LocalDate.now()

                        val nextTask = tasks
                            .filter {
                                try {
                                    val dueDate = LocalDate.parse(it.dueDate, dateFormatter)
                                    !dueDate.isBefore(today)
                                } catch (e: Exception) { false }
                            }
                            .minByOrNull {
                                try { LocalDate.parse(it.dueDate, dateFormatter).toEpochDay() }
                                catch (e: Exception) { Long.MAX_VALUE }
                            }

                        UpcomingTaskCard(nextTask)
                    }
                }
            }

            // --- CAMPUS NEWS ---
            item {
                AnimatedEntrance(visible = startAnimation, delay = 300) {
                    Column {
                        SectionTitle(title = "Campus News", action = "View All", onActionClick = onViewAllClick)
                        Spacer(modifier = Modifier.height(16.dp))
                        AnnouncementList(newsList = state.campusNews)
                    }
                }
            }

            // --- UPCOMING DEADLINES ---
            item {
                AnimatedEntrance(visible = startAnimation, delay = 450) {
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Column {
                            SectionTitle(title = "Upcoming Deadlines", action = "", onActionClick = {})
                            Spacer(modifier = Modifier.height(16.dp))

                            when (val tasks = taskState) {
                                is TaskUiState.Success -> {
                                    if (tasks.tasks.isEmpty()) {
                                        EmptyStateText("No upcoming deadlines. You're all caught up!")
                                    } else {
                                        tasks.tasks.take(4).forEach { task ->
                                            DeadlineItem(task.title, task.dueDate, AuroraSoftTeal)
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                                is TaskUiState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = AuroraVividPurple)
                                    }
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

// ============================================================================
// UI COMPONENTS
// ============================================================================

@Composable
fun AnimatedEntrance(visible: Boolean, delay: Int = 0, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.fillMaxWidth(),
        enter = fadeIn(tween(600, delayMillis = delay)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) { content() }
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
        role == "admin" -> "Admin"
        rawCourse == null -> "Student"
        else -> rawCourse
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            Text(
                text = dateFormat.format(Date()).uppercase(),
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hello, $username",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Sleek Role Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.School, contentDescription = null, tint = AuroraSoftTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = headerSubtext,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Profile Picture with Online Status Indicator
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
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
                    Icon(Icons.Rounded.Person, "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            // Online dot
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .border(2.dp, AuroraVividPurple, CircleShape)
            )
        }
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
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        if (action.isNotEmpty()) {
            Text(
                text = action,
                fontSize = 14.sp,
                color = AuroraVividPurple,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AnnouncementBanner(announcement: Announcement, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Soft warning orange tint
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            // Left Accent Color Line
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(Color(0xFFFF9800)))

            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFF9800).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Campaign, "Alert", tint = Color(0xFFE65100))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = announcement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFE65100),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = announcement.content,
                        fontSize = 13.sp,
                        color = Color(0xFFE65100).copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, "Dismiss", tint = Color(0xFFE65100).copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun UpcomingTaskCard(task: Task?) {
    // Beautiful layered card effect
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp), spotColor = AuroraVividPurple)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(colors = listOf(Color(0xFF2B215A), Color(0xFF4C3B9B))))
                .padding(2.dp) // Subtle inner border space
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AuroraVividPurple.copy(alpha = 0.3f), Color.Transparent),
                            radius = 600f
                        )
                    )
                    .padding(24.dp)
            ) {
                if (task == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.CheckCircleOutline, null, tint = AuroraSoftTeal, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("No upcoming deadlines", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("You're totally free! Go relax.", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                } else {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(AuroraSoftTeal.copy(alpha = 0.2f))
                                    .border(1.dp, AuroraSoftTeal.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Timer, null, tint = AuroraSoftTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("NEXT UP", color = AuroraSoftTeal, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                            }
                            Text(task.dueDate, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(task.subject.uppercase(), color = AuroraSoftTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(task.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Description box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Subject, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.description.ifEmpty { "No description provided." },
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementList(newsList: List<Announcement>) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val configuration = LocalConfiguration.current
    val responsiveCardWidth = (configuration.screenWidthDp * 0.65).dp // Slightly narrower for better carousel peeking

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
                // Passing title hash to generate a consistent but pseudo-random gradient cover
                idSeed = news.title.hashCode(),
                modifier = Modifier.width(responsiveCardWidth)
            )
        }
    }
}

@Composable
fun AnnouncementCard(category: String, title: String, date: String, idSeed: Int, modifier: Modifier = Modifier) {
    // Generate a cool dynamic gradient based on the item's title string hash
    val color1 = Color(0xFF000000 + (idSeed * 0xFFFFFF).absoluteValue % 0xFFFFFF).copy(alpha = 0.7f)
    val color2 = AuroraVividPurple

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(200.dp) // Fixed height for uniformity
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top half: Generated "Cover Art"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(Brush.linearGradient(listOf(color1, color2)))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                }
            }

            // Bottom half: Content
            Column(
                modifier = Modifier.fillMaxWidth().weight(0.55f).padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Event, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(date, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun DeadlineItem(task: String, due: String, indicatorColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline indicator bar
            Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(indicatorColor))

            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(indicatorColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Assignment, null, tint = indicatorColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(task, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(due, fontSize = 13.sp, color = TextSecondary)
                    }
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun EmptyStateText(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.IncompleteCircle, null, tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}