package com.example.smartcompanionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.User
import com.example.smartcompanionapp.data.UserRepository
import com.example.smartcompanionapp.data.UserRole
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.*

@Composable
fun GetStartedScreen(onLogin: () -> Unit, onSignUp: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(180.dp),
                        tint = BrandBlue
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Smart Campus Companion",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextBlack,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A modern minimal academic experience designed for your daily campus life.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextGray,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandBlue))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandBlueLight))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrandBlueLight))
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Gradient Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryGradientHorizontal)
                        .clickable { onLogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Login",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Create an account",
                    color = BrandBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onSignUp() }
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            color = TextBlack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Selection
        RoleSelector(selectedRole) { selectedRole = it }

        Spacer(modifier = Modifier.height(24.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = Color.LightGray
            )
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryGradientHorizontal)
                .clickable {
                    val user = UserRepository.login(username, password)
                    if (user != null) {
                        if (user.role == selectedRole) {
                            // --- NAVIGATION CHANGE ---
                            // We navigate to Dashboard defined in your Screen class
                            navController.navigate("dashboard") {
                                // Pop up to login so back button doesn't return to login
                                popUpTo(Screen.login.route) { inclusive = true }
                            }
                        } else {
                            errorMessage = "Invalid role for this user"
                        }
                    } else {
                        errorMessage = "Invalid username or password"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Login",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Don't have an account? ", color = TextGray)
            Text(
                text = "Create an account",
                color = BrandBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // Note: Your Screen class currently doesn't have a SignUp object.
                    // You would need to add `object SignUp : Screen("signup")`
                    // and a composable entry in AppNavigation to use this:
                    navController.navigate("signup")
                }
            )
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Signup", style = MaterialTheme.typography.headlineLarge, color = TextBlack)

        Spacer(modifier = Modifier.height(32.dp))

        RoleSelector(selectedRole) { selectedRole = it }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("Username") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // ... (Include Email and Phone fields here similarly) ...

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            }
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryGradientHorizontal)
                .clickable {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill all fields"
                    } else {
                        val success = UserRepository.signUp(
                            User(username, password, selectedRole, email, phoneNumber)
                        )
                        if (success) {
                            // --- NAVIGATION CHANGE ---
                            navController.navigate(Screen.login.route) {
                                popUpTo(Screen.login.route) { inclusive = true }
                            }
                        } else {
                            errorMessage = "Username already exists"
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Signup", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Already have an account? ", color = TextGray)
            Text(
                text = "Login",
                color = BrandBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // Go back to Login
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun RoleSelector(selectedRole: UserRole, onRoleSelected: (UserRole) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChip(
            selected = selectedRole == UserRole.USER,
            onClick = { onRoleSelected(UserRole.USER) },
            label = { Text("User") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrandBlueLight,
                selectedLabelColor = BrandBlue
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        FilterChip(
            selected = selectedRole == UserRole.ADMIN,
            onClick = { onRoleSelected(UserRole.ADMIN) },
            label = { Text("Admin") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrandBlueLight,
                selectedLabelColor = BrandBlue
            )
        )
    }
}

@Composable
fun HomeScreen(username: String, role: String, onLogout: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundSoft),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(PrimaryGradientHorizontal),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    Text(
                        "Welcome \uD83D\uDC4B",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            }

            // Profile Section pulling up over the gradient
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-60).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar with White Border and Shadow effect
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = BrandBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Hi, $username",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Badge with subtle gradient or solid
                Surface(
                    color = BrandBlueLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Role: ",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextGray
                            )
                        )
                        Text(
                            text = role,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gradient Back Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryGradientHorizontal)
                        .clickable { onLogout() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Back to Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLogout() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout, 
                        contentDescription = null, 
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
