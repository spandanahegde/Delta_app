package com.example.deltasitemanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



@Composable
fun DeltaSiteManagerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF4964CB),        // Light Blue (for buttons, highlights)
        onPrimary = Color.White,            // Text/icons on primary
        secondary = Color(0xFFE4EEEE),      // Teal accent
        onSecondary = Color.White,          // Text/icons on secondary
        background = Color(0xFF020101),     // True dark background
        onBackground = Color(0xFFE0E0E0),   // Light gray text/icons on background
        surface = Color(0xFF232121),        // Surface for cards, sheets
        onSurface = Color(0xFFF5F5F5),      // Text/icons on surface
        error = Color(0xFFCF6679),          // Desaturated red
        onError = Color.Black
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
