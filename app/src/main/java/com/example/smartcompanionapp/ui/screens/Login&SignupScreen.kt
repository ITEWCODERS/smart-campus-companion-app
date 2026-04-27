package com.example.smartcompanionapp.ui.screens

import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcompanionapp.R
import com.example.smartcompanionapp.data.session.SessionManager
import com.example.smartcompanionapp.data.model.UserRole
import com.example.smartcompanionapp.ui.navigation.Screen
import com.example.smartcompanionapp.ui.theme.*
import com.example.smartcompanionapp.viewmodel.AuthState
import com.example.smartcompanionapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.smartcompanionapp.viewmodel.DashboardViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GetStartedScreen(onLogin: () -> Unit, onSignUp: () -> Unit) {
    // Moving Aurora Background Logic
    val infiniteTransition = rememberInfiniteTransition(label = "get_started_aurora")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val movingAurora = Brush.linearGradient(
        colors = listOf(AuroraDeepIndigo, AuroraVividPurple, AuroraSoftTeal, AuroraDeepIndigo),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 1000f, 2000f)
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(movingAurora)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // High-Tech SmartCampus 'S' Logo Section
                Box(
                    modifier = Modifier.weight(1.5f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SmartCampusLogo()
                }

                // Clean Minimalist Text Section
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SmartCampus",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Experience a cutting-edge campus companion designed for modern student life.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Primary CTA
                    Button(
                        onClick = onLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = AuroraSoftTeal),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuroraSoftTeal)
                    ) {
                        Text(
                            "ENTER CAMPUS",
                            color = AuroraDeepIndigo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onSignUp) {
                        Text("CREATE AN ACCOUNT", color = Color.White, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SmartCampusLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = floatAnim.dp)
            .graphicsLayer {
                rotationX = 15f
                rotationY = -10f
                cameraDistance = 12f * density
            }
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .blur(40.dp)
                .background(AuroraSoftTeal.copy(alpha = 0.15f), CircleShape)
        )
        
        Surface(
            modifier = Modifier
                .size(160.dp)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(35.dp)) {
                val w = size.width
                val h = size.height
                val sPath = Path().apply {
                    moveTo(w * 0.75f, h * 0.2f)
                    cubicTo(w * 0.75f, h * 0.05f, w * 0.2f, h * 0.05f, w * 0.25f, h * 0.35f)
                    cubicTo(w * 0.3f, h * 0.5f, w * 0.7f, h * 0.5f, w * 0.75f, h * 0.65f)
                    cubicTo(w * 0.8f, h * 0.95f, w * 0.25f, h * 0.95f, w * 0.25f, h * 0.8f)
                }
                val auroraBrush = Brush.linearGradient(listOf(AuroraSoftTeal, AuroraVividPurple))
                drawPath(path = sPath, brush = auroraBrush, style = Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                
                val sPath2 = Path().apply {
                    moveTo(w * 0.7f, h * 0.25f)
                    cubicTo(w * 0.7f, h * 0.15f, w * 0.3f, h * 0.15f, w * 0.35f, h * 0.35f)
                    cubicTo(w * 0.4f, h * 0.5f, w * 0.6f, h * 0.5f, w * 0.65f, h * 0.65f)
                    cubicTo(w * 0.7f, h * 0.85f, w * 0.3f, h * 0.85f, w * 0.3f, h * 0.75f)
                }
                drawPath(path = sPath2, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 8f, cap = StrokeCap.Round))
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                authViewModel.signInWithGoogle(idToken, selectedRole.name.lowercase())
            }
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Google sign in failed", e)
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val user = (authState as AuthState.Success).user
            sessionManager.saveSession(user.username, user.role, user.email, user.email)
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
                .background(AppBackground)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome Back", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))
            RoleSelector(selectedRole) { selectedRole = it }
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = AuroraSoftTeal) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = AuroraSoftTeal) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                    }
                }
            )

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isFormValid = email.isNotBlank() && password.isNotBlank()

            Button(
                onClick = { authViewModel.login(email, password, selectedRole.name.lowercase()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuroraVividPurple,
                    disabledContainerColor = AuroraVividPurple.copy(alpha = 0.3f)
                ),
                enabled = authState !is AuthState.Loading && isFormValid
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(com.example.smartcompanionapp.R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuroraSoftTeal)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_google_logo), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                Text("New here? Create an account", color = AuroraVividPurple)
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
    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    val authState by authViewModel.authState.collectAsState()

    // Validation State
    val isUsernameValid = username.length >= 3
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid = phoneNumber.length == 11 && phoneNumber.all { it.isDigit() }
    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()

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
            .background(AppBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))
        RoleSelector(selectedRole) { selectedRole = it }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = AuroraSoftTeal) },
            isError = !isUsernameValid && username.isNotEmpty(),
            supportingText = {
                if (!isUsernameValid && username.isNotEmpty()) {
                    Text("Username must be at least 3 characters", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = AuroraSoftTeal) },
            isError = !isEmailValid && email.isNotEmpty(),
            supportingText = {
                if (!isEmailValid && email.isNotEmpty()) {
                    Text("Please enter a valid email address", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 11) phoneNumber = it },
            label = { Text("Phone Number (11 digits)") },
            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = AuroraSoftTeal) },
            isError = !isPhoneValid && phoneNumber.isNotEmpty(),
            supportingText = {
                if (!isPhoneValid && phoneNumber.isNotEmpty()) {
                    Text("Phone number must be exactly 11 digits", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Min. 6 keys)") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = AuroraSoftTeal) },
            isError = !isPasswordValid && password.isNotEmpty(),
            supportingText = {
                if (!isPasswordValid && password.isNotEmpty()) {
                    Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AuroraSoftTeal) },
            isError = !isConfirmPasswordValid && confirmPassword.isNotEmpty(),
            supportingText = {
                if (!isConfirmPasswordValid && confirmPassword.isNotEmpty()) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isFormValid = isUsernameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid

        Button(
            onClick = { authViewModel.signUp(username, email, password, selectedRole.name.lowercase()) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuroraVividPurple,
                disabledContainerColor = AuroraVividPurple.copy(alpha = 0.3f)
            ),
            enabled = authState !is AuthState.Loading && isFormValid
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Login", color = AuroraVividPurple)
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
            label = { Text("Student") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AuroraVividPurple.copy(alpha = 0.2f),
                selectedLabelColor = AuroraVividPurple
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        FilterChip(
            selected = selectedRole == UserRole.ADMIN,
            onClick = { onRoleSelected(UserRole.ADMIN) },
            label = { Text("Admin") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AuroraVividPurple.copy(alpha = 0.2f),
                selectedLabelColor = AuroraVividPurple
            )
        )
    }
}

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    
    // Aurora Mesh Background Animation
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_loading")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val loadingAurora = Brush.linearGradient(
        colors = listOf(AuroraDeepIndigo, AuroraVividPurple, AuroraSoftTeal, AuroraDeepIndigo),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 1000f, 2000f)
    )

    var startAnimation by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500) 
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(loadingAurora),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SmartCampusLogo(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    }
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            FluidMergingLoader(color = AuroraSoftTeal, alpha = logoScale)
        }
    }
}

@Composable
fun FluidMergingLoader(color: Color, alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid")
    
    // Orbiting animations for the blobs
    val orbitRadius = 30f
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        // High-Fidelity Gooey Blobs using Blur and Offset
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            
            // Draw 3 blobs with varying orbits
            val blob1Offset = androidx.compose.ui.geometry.Offset(
                x = center.x + orbitRadius * cos(angle),
                y = center.y + orbitRadius * sin(angle)
            )
            val blob2Offset = androidx.compose.ui.geometry.Offset(
                x = center.x + orbitRadius * cos(angle + (2 * PI / 3).toFloat()),
                y = center.y + orbitRadius * sin(angle + (2 * PI / 3).toFloat())
            )
            val blob3Offset = androidx.compose.ui.geometry.Offset(
                x = center.x + orbitRadius * cos(angle + (4 * PI / 3).toFloat()),
                y = center.y + orbitRadius * sin(angle + (4 * PI / 3).toFloat())
            )

            // Blur logic for gooey effect
            val blobs = listOf(blob1Offset, blob2Offset, blob3Offset)
            
            blobs.forEachIndexed { index, offset ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0f)),
                        center = offset,
                        radius = 45f
                    ),
                    center = offset,
                    radius = 45f
                )
                
                // Add a "core" to each blob for more depth
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    center = offset,
                    radius = 12f
                )
            }
        }
    }
}
