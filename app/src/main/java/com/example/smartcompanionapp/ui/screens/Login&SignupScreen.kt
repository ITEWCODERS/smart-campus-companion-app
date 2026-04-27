package com.example.smartcompanionapp.ui.screens

import android.util.Patterns
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.data.model.UserRole
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.AuthState
import com.example.smartcompanionapp.viewmodel.AuthViewModel
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GetStartedScreen(onLogin: () -> Unit, onSignUp: () -> Unit) {
    // 1. Animated Gradient Background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // 2. Central Logo Rotation (Slow and steady)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 3. Floating effect for small feature icons
    val floatTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by floatTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    val animatedGradient = Brush.linearGradient(
        colors = listOf(
            BackgroundWhite,
            Color(0xFFE3F2FD),
            Color(0xFFF3E5F5),
            BackgroundWhite
        ),
        start = androidx.compose.ui.geometry.Offset(offset, offset),
        end = androidx.compose.ui.geometry.Offset(offset + 600f, offset + 600f)
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(animatedGradient)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Section 1: Animated Icons Section
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Main Logo: Stylized Book (Center)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(170.dp)
                            .graphicsLayer {
                                rotationZ = rotation
                            },
                        tint = BrandBlue
                    )

                    // Feature Icons surrounding the logo (matching user image layout)
                    // Chat (Top Left)
                    FloatingIcon(Icons.Rounded.ChatBubble, 220, 140.dp, floatOffset)
                    // Mini Book (Right)
                    FloatingIcon(Icons.Rounded.AutoStories, 0, 140.dp, floatOffset)
                    // Calendar (Bottom Left)
                    FloatingIcon(Icons.Rounded.CalendarMonth, 140, 140.dp, floatOffset)
                }

                // Section 2: Text and Navigation Section
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(12.dp))

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

                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(BrandBlue))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(BrandBlueLight.copy(alpha = 0.5f)))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(BrandBlueLight.copy(alpha = 0.5f)))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Primary Button: Get Started
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(PrimaryGradientHorizontal)
                            .clickable { onLogin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Get Started",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Secondary Option: Signup
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
}

@Composable
private fun FloatingIcon(icon: ImageVector, angle: Int, radius: androidx.compose.ui.unit.Dp, floatOffset: Float) {
    val angleRad = angle * PI / 180f
    Box(
        modifier = Modifier.offset(
            x = (radius.value * cos(angleRad)).dp,
            y = (radius.value * sin(angleRad)).dp + floatOffset.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = BrandBlue.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2500, easing = LinearEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(3000)
        onFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = BrandBlue
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Loading campus services...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar Loading
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrandBlue,
                    trackColor = BrandBlueLight,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End),
                    color = TextGray
                )
            }
        }
    }
}


@Composable
fun LoginScreen(
    navController: NavController,
    // Add the '= viewModel()' to make it optional in Navigation.kt
    authViewModel: AuthViewModel = viewModel(),
    // This is passed from Navigation.kt
    dashboardViewModel: DashboardViewModel,
    // Add a default role so Navigation.kt doesn't have to pass it
    selectedRole: UserRole = UserRole.USER
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {            val user = (authState as AuthState.Success).user
            sessionManager.saveSession(user.username, user.role)

            // --- ADD THIS LINE TO GIVE ADMIN PRIVILEGES ---
            dashboardViewModel.setAdminPrivileges(selectedRole == UserRole.ADMIN)
            // ----------------------------------------------

            FirebaseMessaging.getInstance().subscribeToTopic("announcements") // ← add this
            showLoading = true
            authViewModel.resetState()

        }
    }

    if (showLoading) {
        LoadingScreen {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge,
                color = TextBlack
            )

            Spacer(modifier = Modifier.height(32.dp))

            RoleSelector(selectedRole) { selectedRole = it }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .let { modifier ->
                        if (authState is AuthState.Loading) {
                            modifier.background(Color.Gray)
                        } else {
                            modifier.background(PrimaryGradientHorizontal)
                        }
                    }
                    .clickable(enabled = authState !is AuthState.Loading) {
                        authViewModel.login(email, password, selectedRole.name.lowercase())
                    },
                contentAlignment = Alignment.Center
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text("Don\u0027t have an account? ", color = TextGray)
                Text(
                    text = "Create an account",
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    // Real-time validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var lengthError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    // Real-time validation (equivalent to TextWatcher)
    LaunchedEffect(email) {
        emailError = when {
            email.isEmpty() -> null
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format (e.g., user@example.com)"
            else -> null
        }
    }

    LaunchedEffect(phoneNumber) {
        phoneError = when {
            phoneNumber.isEmpty() -> null
            !phoneNumber.all { it.isDigit() } -> "Numbers only (0-9)"
            phoneNumber.length != 11 -> "Phone number must be exactly 11 digits"
            else -> null
        }
    }

    LaunchedEffect(username, password) {
        lengthError = if ((username.isNotEmpty() && username.length < 6) || (password.isNotEmpty() && password.length < 6)) {
            "Username and Password must be at least 6 characters."
        } else {
            null
        }
    }

    LaunchedEffect(confirmPassword, password) {
        confirmPasswordError = if (confirmPassword.isNotEmpty() && confirmPassword != password) {
            "Passwords do not match."
        } else {
            null
        }
    }

    val isFormValid = username.length >= 6 &&
                      email.isNotBlank() && emailError == null &&
                      phoneNumber.length == 11 && phoneError == null &&
                      password.length >= 6 &&
                      confirmPassword == password

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Signup.route) { inclusive = true }
            }
            authViewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Signup", style = MaterialTheme.typography.headlineLarge, color = TextBlack)

        Spacer(modifier = Modifier.height(32.dp))

        RoleSelector(selectedRole) { selectedRole = it }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = lengthError != null && username.length < 6 && username.isNotEmpty(),
            supportingText = {
                if (lengthError != null && username.length < 6 && username.isNotEmpty()) {
                    Text(text = lengthError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 11) phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = phoneError != null,
            supportingText = {
                if (phoneError != null) {
                    Text(text = phoneError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = lengthError != null && password.length < 6 && password.isNotEmpty(),
            supportingText = {
                if (lengthError != null && password.length < 6 && password.isNotEmpty()) {
                    Text(text = lengthError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = {
                if (confirmPasswordError != null) {
                    Text(text = confirmPasswordError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            }
        )

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .let { modifier ->
                    if (isFormValid && authState !is AuthState.Loading) {
                        modifier.background(PrimaryGradientHorizontal)
                    } else {
                        modifier.background(Color.LightGray)
                    }
                }
                .clickable(enabled = isFormValid && authState !is AuthState.Loading) {
                    authViewModel.signUp(username, email, password, selectedRole.name.lowercase())
                },
            contentAlignment = Alignment.Center
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Signup", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Already have an account? ", color = TextGray)
            Text(
                text = "Login",
                color = BrandBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
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
