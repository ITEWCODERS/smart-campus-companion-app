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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartcompanionapp.model.Announcement
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.intent.DashboardState
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val state by viewModel.state.collectAsState()

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
            // --- NEW: TOP FLASHING ANNOUNCEMENT BANNER ---
            item {
                AnimatedVisibility(
                    visible = state.topAnnouncement != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    state.topAnnouncement?.let { announcement ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            AnnouncementBanner(
                                announcement = announcement,
                                onDismiss = {
                                    viewModel.processIntent(DashboardIntent.DismissAnnouncement(announcement.id))
                                }
                            )
                        }
                    }
                }
            }

            // 1. Header
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    StudentHeader()
                }
            }

            // 2. Next Class (With Teacher info)
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    NextClassCard()
                }
            }

            // 3. Announcements (Now dynamic from Room DB)
            item {
                SectionTitle(title = "Campus News", action = "View All")
                Spacer(modifier = Modifier.height(12.dp))
                AnnouncementList(newsList = state.campusNews)
            }

            // 4. Deadlines
            item {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Column {
                        Text(
                            text = "Upcoming Deadlines",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DeadlineItem("Physics Lab Report", "Today, 11:59 PM", UniAccent)
                        Spacer(modifier = Modifier.height(12.dp))
                        DeadlineItem("History Essay", "Tomorrow, 10:00 AM", UniPrimary)
                    }
                }
            }
        }
    }
}

// --- NEW COMPONENT: The Top Banner UI ---
@Composable
fun AnnouncementBanner(announcement: Announcement, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AlertBg), // Uses your custom AlertBg
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Alert",
                tint = AlertText
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = announcement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AlertText
                )
                Text(
                    text = announcement.content,
                    fontSize = 12.sp,
                    color = AlertText.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    tint = AlertText
                )
            }
        }
    }
}

// --- MODIFIED COMPONENT: Dynamic News List ---
@Composable
fun AnnouncementList(newsList: List<Announcement>) {
    // A quick formatter to convert the Long timestamp into a readable date (e.g., "Jan 15")
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // If there are no news items, you could show an empty state here,
        // but for now we loop through the actual Room Database items!
        items(newsList) { news ->
            val dateString = dateFormat.format(Date(news.datePosted))

            AnnouncementCard(
                category = "Campus News", // You can update your DB Model to hold categories later!
                title = news.title,
                date = dateString,
                color = EventInfoBg, // Defaulting to your EventInfo styles
                textColor = EventInfoText
            )
        }
    }
}

// --- ORIGINAL COMPONENTS (Left Untouched) ---

@Composable
fun StudentHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Jan 15, Wednesday",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hi, Alex",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Computer Science • Year 3",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = Color.White
            )
        }
    }
}

@Composable
fun NextClassCard() {
    val gradient = Brush.linearGradient(
        colors = listOf(UniPrimary, UniSecondary)
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Badge(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ) {
                        Text("Next Class", modifier = Modifier.padding(4.dp))
                    }
                    Text(
                        text = "10:00 AM",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Data Structures & Algo",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lecture Hall B, Room 304",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Dr. Sarah Jenkins",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String) {
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
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun AnnouncementCard(category: String, title: String, date: String, color: Color, textColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        modifier = Modifier.width(260.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContainerBadge(text = category, bgColor = color, textColor = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = date, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun DeadlineItem(task: String, due: String, indicatorColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = task, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = "Due: $due", fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
fun ContainerBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun CampusBottomNav(navController: NavController) {
    NavigationBar(containerColor = AppSurface, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("dashboard") },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("schedule") },
            icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("task") },
            icon = { Icon(Icons.Rounded.Checklist, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("campusInfo") },
            icon = { Icon(Icons.Rounded.Info, contentDescription = "Campus Info") },
            label = { Text("Info") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("settings") },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // This will now cause a crash if you don't have a way to preview the ViewModel.
    // A common approach is to create a fake/mock ViewModel for previews.
    // DashboardScreen(navController = rememberNavController(), viewModel = ...)
}
