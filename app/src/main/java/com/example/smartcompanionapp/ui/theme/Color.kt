package com.example.smartcompanionapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aurora Pulse Palette
val AuroraDeepIndigo = Color(0xFF2B1055)
val AuroraVividPurple = Color(0xFF7C3AED)
val AuroraSoftTeal = Color(0xFF2DD4BF)

// Static Text Colors (for use in non-composable ColorScheme definitions)
val TextBlackStatic = Color(0xFF202124)
val TextGrayStatic = Color(0xFF5F6368)
val BackgroundWhiteStatic = Color(0xFFFFFFFF)

// Semantic Theme Mappings
val AppBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val AppSurface: Color @Composable get() = MaterialTheme.colorScheme.surface

// Master Visibility Logic for Text
val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

// LEGACY VISIBILITY FIX
val TextBlack: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextGray: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val BackgroundWhite: Color @Composable get() = MaterialTheme.colorScheme.surface

val UniPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary
val UniSecondary: Color @Composable get() = MaterialTheme.colorScheme.secondary

// Gradients and specific mappings
val AuroraMeshGradient = Brush.verticalGradient(listOf(AuroraDeepIndigo, AuroraVividPurple, AuroraSoftTeal))
val PrimaryGradientHorizontal = Brush.horizontalGradient(listOf(AuroraVividPurple, AuroraSoftTeal))
val CTAButtonColor = AuroraSoftTeal
val BrandBlue = AuroraVividPurple 
val BrandBlueLight = Color(0xFFF3E8FF)

// UI Helpers
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
