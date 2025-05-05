package com.example.deltasitemanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//private val LightColorScheme = lightColorScheme(
////    primary = Color(0xFF6200EE),
//    primary = Color(0xFF2196F3),
//    onPrimary = Color.White,
//    background = Color(0xFFF2F2F2),
//    onBackground = Color.Black,
//    surface = Color.White,
//    onSurface = Color.Black
//)
//
//private val DarkColorScheme = darkColorScheme(
////    primary = Color(0xFFBB86FC),
//    primary = Color(0xFF1976D2),
//    onPrimary = Color.Black,
//    background = Color(0xFF121212),
//    onBackground = Color.White,
//    surface = Color(0xFF1F1F1F),
//    onSurface = Color.White
//)

@Composable
fun DeltaSiteManagerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF435385),        // Light Blue (for buttons, highlights)
        onPrimary = Color.Black,            // Text/icons on primary
        secondary = Color(0xFF80CBC4),      // Teal accent
        onSecondary = Color.White,          // Text/icons on secondary
        background = Color(0xFF121212),     // True dark background
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
