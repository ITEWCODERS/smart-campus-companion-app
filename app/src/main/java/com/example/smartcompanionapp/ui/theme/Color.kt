package com.example.smartcompanionapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
