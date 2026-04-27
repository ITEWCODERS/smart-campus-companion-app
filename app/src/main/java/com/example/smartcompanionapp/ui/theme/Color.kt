package com.example.smartcompanionapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aurora Pulse Palette (Modern Tech & Social)
val AuroraDeepIndigo = Color(0xFF2B1055)
val AuroraVividPurple = Color(0xFF7C3AED)
val AuroraSoftTeal = Color(0xFF2DD4BF)

// Legacy / Helper Colors (kept for compatibility if needed)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Semantic Theme Mappings
val AppBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val AppSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val UniPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary
val UniSecondary: Color @Composable get() = MaterialTheme.colorScheme.secondary

// Background White and Text
val BackgroundWhite = Color(0xFFFFFFFF)
val TextBlack = Color(0xFF202124)
val TextGray = Color(0xFF5F6368)

// Aurora Gradients
val AuroraMeshGradient = Brush.verticalGradient(
    colors = listOf(AuroraDeepIndigo, AuroraVividPurple, AuroraSoftTeal)
)

val PrimaryGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(AuroraVividPurple, AuroraSoftTeal)
)

// Specific Mappings for Aurora Pulse
val CTAButtonColor = AuroraSoftTeal
val BrandBlue = AuroraVividPurple // Replacing primary BrandBlue with Vivid Purple
val BrandBlueLight = Color(0xFFF3E8FF) // A very soft purple for backgrounds

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
