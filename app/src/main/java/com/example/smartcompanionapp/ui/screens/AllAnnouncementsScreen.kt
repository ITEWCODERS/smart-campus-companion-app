package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcompanionapp.model.Announcement
import com.example.smartcompanionapp.ui.theme.AppBackground
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
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

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("All Campus News") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black // Or your theme color
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF0F0F0) // Or your theme color
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.campusNews) { news ->
                FullWidthAnnouncementCard(news = news)
            }
        }
    }
}

@Composable
fun FullWidthAnnouncementCard(news: Announcement) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(news.datePosted))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = news.content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
