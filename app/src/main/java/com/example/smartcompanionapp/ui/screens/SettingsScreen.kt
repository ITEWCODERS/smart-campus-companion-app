package com.example.smartcompanionapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.ui.navigation.CampusBottomNav
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isDarkModePref by sessionManager.isDarkModeFlow.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = isDarkModePref ?: systemDark
    
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var name by remember { mutableStateOf(currentUser?.displayName ?: sessionManager.getUsername() ?: "Student") }
    var email by remember { mutableStateOf(currentUser?.email ?: sessionManager.getEmail() ?: "No email") }
    var course by remember { mutableStateOf(sessionManager.getCourse()) }
    val role = sessionManager.getRole()?.replaceFirstChar { it.uppercase() } ?: "Student"
    
    val googlePhotoUrl = currentUser?.photoUrl?.toString()
    val localPhotoUri = sessionManager.getProfileImage()

    var isProfileExpanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    
    var editName by remember { mutableStateOf(name) }
    var editEmail by remember { mutableStateOf(email) }
    var editCourse by remember { mutableStateOf(course) }

    val rotation by animateFloatAsState(
        targetValue = if (isProfileExpanded) 180f else 0f,
        animationSpec = tween(300), label = "rotation"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sessionManager.updateProfileImage(it.toString()) }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppSurface)
            )
        },
        bottomBar = { CampusBottomNav(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Interactive & Editable Profile Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { if (!isEditing) isProfileExpanded = !isProfileExpanded },
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AuroraVividPurple.copy(alpha = 0.1f))
                                    .border(2.dp, AuroraSoftTeal, CircleShape)
                                    .clickable { if (googlePhotoUrl == null) galleryLauncher.launch("image/*") },
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
                                    Icon(Icons.Rounded.Person, null, tint = AuroraVividPurple, modifier = Modifier.size(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                Text(role, color = AuroraVividPurple, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }

                            if (!isEditing) {
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                                    tint = TextSecondary
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isProfileExpanded || isEditing,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 24.dp)) {
                                HorizontalDivider(color = TextSecondary.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Full Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = editEmail,
                                        onValueChange = { editEmail = it },
                                        label = { Text("Email Address") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = editCourse,
                                        onValueChange = { editCourse = it },
                                        label = { Text("Course / Department") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { isEditing = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = {
                                                name = editName
                                                email = editEmail
                                                course = editCourse
                                                sessionManager.updateProfile(editName, editEmail, editCourse)
                                                isEditing = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AuroraVividPurple)
                                        ) {
                                            Text("Save", color = Color.White)
                                        }
                                    }
                                } else {
                                    ProfileInfoRow(Icons.Rounded.Email, "Email Address", email)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ProfileInfoRow(Icons.Rounded.School, "Academic Course", course)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ProfileInfoRow(Icons.Rounded.Badge, "Role ID", "CAMPUS-${role.uppercase()}")
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = { isEditing = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AuroraVividPurple),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp)
                                    ) {
                                        Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SettingsCard(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "Alerts & updates",
                    onClick = { navController.navigate("notifications") }
                )
            }

            item {
                SettingsCard(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { sessionManager.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuroraSoftTeal,
                                checkedTrackColor = AuroraVividPurple.copy(alpha = 0.4f)
                            )
                        )
                    }
                )
            }

            item {
                SettingsCard(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = "Log Out",
                    onClick = {
                        sessionManager.logout()
                        navController.navigate(Screen.GetStarted.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AuroraVividPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        color = AppSurface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AuroraVividPurple, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (subtitle != null) Text(subtitle, fontSize = 13.sp, color = TextSecondary)
            }
            if (trailing != null) trailing() else Icon(Icons.Rounded.ChevronRight, null, tint = TextSecondary)
        }
    }
}
