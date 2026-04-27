package com.example.smartcompanionapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aurora Pulse Palette (Modern Tech & Social)
val AuroraDeepIndigo = Color(0xFF2B1055)
val AuroraVividPurple = Color(0xFF7C3AED)
val AuroraSoftTeal = Color(0xFF2DD4BF)

// Legacy / Helper Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Semantic Theme Mappings - Properly Reactive to Dark Mode
val AppBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val AppSurface: Color @Composable get() = MaterialTheme.colorScheme.surface

// TEXT VISIBILITY FIX: Always use 'onSurface' variants so text flips to white in dark mode
val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val UniPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary
val UniSecondary: Color @Composable get() = MaterialTheme.colorScheme.secondary

// Background and Hardcoded Mappings - Made Dynamic to fix invisible text
val BackgroundWhite @Composable get() = MaterialTheme.colorScheme.surface
val TextBlack @Composable get() = MaterialTheme.colorScheme.onSurface 
val TextGray @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

// Aurora Gradients
val AuroraMeshGradient = Brush.verticalGradient(
    colors = listOf(AuroraDeepIndigo, AuroraVividPurple, AuroraSoftTeal)
)

val PrimaryGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(AuroraVividPurple, AuroraSoftTeal)
)

// Specific Mappings for Aurora Pulse
val CTAButtonColor = AuroraSoftTeal
val BrandBlue = AuroraVividPurple 
val BrandBlueLight = Color(0xFFF3E8FF)

// Dashboard & UI Helpers
val AlertBg = Color(0xFFFEE2E2)
val AlertText = Color(0xFFDC2626)
val EventInfoBg = AuroraSoftTeal.copy(alpha = 0.1f)
val EventInfoText = AuroraDeepIndigo

val ActionMapBg = Color(0xFFE0F2FE)
val ActionMapIcon = Color(0xFF0284C7)
val ActionLibraryBg = Color(0xFFFEF3C7)
val ActionLibraryIcon = Color(0xFFD97706)
val ActionShuttleBg = Color(0xFFDCFCE7)
val ActionShuttleIcon = Color(0xFF16A34A)
val ActionEventsBg = AuroraVividPurple.copy(alpha = 0.1f)
val ActionEventsIcon = AuroraVividPurple
