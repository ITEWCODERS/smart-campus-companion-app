package com.example.smartcompanionapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AuroraVividPurple,
    secondary = AuroraSoftTeal,
    tertiary = AuroraDeepIndigo,
    background = Color(0xFF0F051D), 
    surface = Color(0xFF1B0D33),
    onPrimary = Color.White,
    onSecondary = AuroraDeepIndigo,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0)
)

private val LightColorScheme = lightColorScheme(
    primary = AuroraVividPurple,
    secondary = AuroraSoftTeal,
    tertiary = AuroraDeepIndigo,
    background = Color(0xFFFBF9FF),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = AuroraDeepIndigo,
    onTertiary = Color.White,
    onBackground = TextBlackStatic,
    onSurface = TextBlackStatic,
    onSurfaceVariant = TextGrayStatic
)

@Composable
fun SmartCompanionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
