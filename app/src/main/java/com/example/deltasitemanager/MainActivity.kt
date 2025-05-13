package com.example.deltasitemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.deltasitemanager.ui.theme.DeltaSiteManagerTheme
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.example.deltasitemanager.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

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
