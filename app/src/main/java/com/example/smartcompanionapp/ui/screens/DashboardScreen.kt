package com.example.smartcompanionapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.domain.TaskUiState
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import com.example.smartcompanionapp.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    taskViewModel: TaskViewModel, // Added TaskViewModel
    onViewAllClick: () -> Unit // New parameter for navigation
) {
    val state by viewModel.state.collectAsState()
    val taskState by taskViewModel.uiState.collectAsState() // Observe TaskViewModel state
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val username = sessionManager.getUsername() ?: "User"

    Scaffold(
        containerColor = AppBackground,
        bottomBar = { CampusBottomNav(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Banner
            item {
                AnimatedVisibility(
                    visible = state.topAnnouncement != null,
                    enter = expandVertically(),
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

            // Header
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    StudentHeader(username = username)
                }
            }

            // Next Class
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    NextClassCard()
                }
            }

            // Announcements
            item {
                SectionTitle(title = "Campus News", action = "View All", onActionClick = onViewAllClick) // Pass lambda here
                Spacer(modifier = Modifier.height(12.dp))
                AnnouncementList(newsList = state.campusNews)
            }

            // Deadlines
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Column {
                        Text("Upcoming Deadlines", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // ── DYNAMIC DEADLINES FROM VIEWMODEL ─────────────────────────
                        when (val tasks = taskState) {
                            is TaskUiState.Success -> {
                                if (tasks.tasks.isEmpty()) {
                                    Text("No upcoming deadlines", color = TextSecondary, fontSize = 14.sp)
                                } else {
                                    // Show first 3 tasks as dynamic deadlines
                                    tasks.tasks.take(3).forEach { task ->
                                        DeadlineItem(task.title, task.dueDate, UniPrimary)
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                            is TaskUiState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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


@Composable
fun SectionTitle(title: String, action: String, onActionClick: () -> Unit) { // Modified parameter
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
            color = UniPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onActionClick) // Make it clickable
        )
    }
}

@Composable
fun AnnouncementBanner(announcement: Announcement, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlertBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Notifications, "Alert", tint = AlertText)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AlertText)
                Text(announcement.content, fontSize = 12.sp, color = AlertText.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, "Dismiss", tint = AlertText)
            }
        }
    }
}

@Composable
fun AnnouncementList(newsList: List<Announcement>) {
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
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
                color = EventInfoBg,
                textColor = EventInfoText
            )
        }
    }
}

@Composable
fun StudentHeader(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val dateFormat = SimpleDateFormat("MMM dd, EEEE", Locale.getDefault())
            Text(dateFormat.format(Date()), fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Hi, $username", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Computer Science • Year 3", fontSize = 14.sp, color = TextSecondary)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Person, "Profile", tint = Color.White)
        }
    }
}

@Composable
fun NextClassCard() {
    val gradient = Brush.linearGradient(colors = listOf(UniPrimary, UniSecondary))
    Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Badge(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White) {
                        Text("Next Class", modifier = Modifier.padding(4.dp))
                    }
                    Text("10:00 AM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Data Structures & Algo", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Lecture Hall B, Room 304", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Dr. Sarah Jenkins", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(category: String, title: String, date: String, color: Color, textColor: Color) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppSurface), modifier = Modifier.width(260.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContainerBadge(text = category, bgColor = color, textColor = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            Text(date, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun DeadlineItem(task: String, due: String, indicatorColor: Color) {
    Row(Modifier.fillMaxWidth().background(AppSurface, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(indicatorColor))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(task, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Due: $due", fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun ContainerBadge(text: String, bgColor: Color, textColor: Color) {
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun CampusBottomNav(navController: NavController) {
    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        NavigationBarItem(selected = true, onClick = { navController.navigate("dashboard") }, icon = { Icon(Icons.Rounded.Home, "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = { navController.navigate("schedule") }, icon = { Icon(Icons.Rounded.CalendarMonth, "Schedule") }, label = { Text("Schedule") })
        NavigationBarItem(selected = false, onClick = { navController.navigate("task") }, icon = { Icon(Icons.Rounded.Checklist, "Tasks") }, label = { Text("Tasks") })
        NavigationBarItem(selected = false, onClick = { navController.navigate("campusInfo") }, icon = { Icon(Icons.Rounded.Info, "Info") }, label = { Text("Info") })
        NavigationBarItem(selected = false, onClick = { navController.navigate("settings") }, icon = { Icon(Icons.Rounded.Settings, "Settings") }, label = { Text("Settings") })
    }
}
