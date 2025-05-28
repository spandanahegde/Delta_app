package com.example.deltasitemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.deltasitemanager.ui.theme.DeltaSiteManagerTheme
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.example.deltasitemanager.navigation.AppNavigation

/**
 * Main entry point for the EMS Android app.
 * Sets up the app theme, state management, and root navigation.
 */
class MainActivity : ComponentActivity() {

    // Shared ViewModel used across screens for authentication and site info
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Track light/dark theme preference with saveable state
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            // Apply app theme
            DeltaSiteManagerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        isDarkTheme = isDarkTheme,
                        toggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}
