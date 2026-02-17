package com.example.smartcompanionapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Primary Theme Colors - Modern Blue & White Palette
val BrandBlue = Color(0xFF1A73E8) // Google Blue style
val BrandBlueDark = Color(0xFF1557B0)
val BrandBlueLight = Color(0xFFE8F0FE)
val BrandGradientStart = Color(0xFF1A73E8)
val BrandGradientEnd = Color(0xFF679AF0)

val BackgroundWhite = Color(0xFFFFFFFF)
val BackgroundSoft = Color(0xFFF8F9FA)
val TextBlack = Color(0xFF202124)
val TextGray = Color(0xFF5F6368)


// Semantic Mappings
val AcademicBlue = BrandBlue
val AcademicBlueLight = BrandBlueLight
val AcademicWhite = BackgroundWhite
val AcademicBlack = TextBlack
val AcademicGray = TextGray

// Gradients
val PrimaryGradient = Brush.verticalGradient(
    colors = listOf(BrandGradientStart, BrandGradientEnd)
)

val PrimaryGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(BrandGradientStart, BrandGradientEnd)
)

// Legacy compatibility
val GreenBackground = BackgroundSoft
val YellowBackground = BackgroundWhite
val DarkGreen = BrandBlue
val GoldText = BackgroundWhite
val DashboardBg = BackgroundSoft
val DashboardTextPrimary = TextBlack
val DashboardTextSecondary = TextGray
val Orange = Color(0xFFFBBC04) // Warning yellow/orange
val DeadlineOrange = Color(0xFFE67C73) // Soft Red/Orange for deadlines
val DeadlineBlue = BrandBlue
val EventLightBlue = BrandBlueLight

val UniPrimary = Color(0xFF2563EB)   // Academic Blue
val UniSecondary = Color(0xFF60A5FA) // Lighter Blue
val UniAccent = Color(0xFFF59E0B)    // Alert/Accent Orange


val AppBackground = Color(0xFFF3F4F6)
val AppSurface = Color.White
val TextPrimary = Color(0xFF1F2937)
val TextSecondary = Color(0xFF6B7280)


val ActionMapBg = Color(0xFFE0F2FE)
val ActionMapIcon = Color(0xFF0284C7)

val ActionLibraryBg = Color(0xFFFEF3C7)
val ActionLibraryIcon = Color(0xFFD97706)

val ActionShuttleBg = Color(0xFFDCFCE7)
val ActionShuttleIcon = Color(0xFF16A34A)

val ActionEventsBg = Color(0xFFF3E8FF)
val ActionEventsIcon = Color(0xFF9333EA)

// --- Feature: Announcements ---
val AlertBg = Color(0xFFFEE2E2)
val AlertText = Color(0xFFDC2626)

val EventInfoBg = Color(0xFFDBEAFE)
val EventInfoText = Color(0xFF1D4ED8)
