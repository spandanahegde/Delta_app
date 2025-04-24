package com.example.deltasitemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.deltasitemanager.ui.*
import com.example.deltasitemanager.ui.screens.*
import com.example.deltasitemanager.ui.theme.DeltaSiteManagerTheme
import com.example.deltasitemanager.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            DeltaSiteManagerTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

                    val onLogout = {
                        authViewModel.clearSession()
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerMenu(
                                navController = navController,
                                onNavigate = { route ->
                                    navController.navigate(route)
                                    scope.launch { drawerState.close() }
                                },
                                onCloseDrawer = { scope.launch { drawerState.close() } },
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onLoginSuccess = {
                                        isLoggedIn = true
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    },
                                    authViewModel = authViewModel
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    navController = navController,
                                    authViewModel = authViewModel,
                                    onLogout = onLogout,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                                )
                            }

                            composable(
                                route = Screen.SiteDetail.route,
                                arguments = listOf(navArgument("macId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val macId = backStackEntry.arguments?.getString("macId") ?: ""
                                SiteDetailScreen(
                                    macId = macId,
                                    navController = navController,
                                    authViewModel = authViewModel
                                )
                            }

                            composable(Screen.Analytics.route) {
                                AnalyticsScreen(navController = navController)
                            }

                            // ✅ Correct route for PowerGraph
                            composable(
                                route = "powerGraphScreen/{macId}",
                                arguments = listOf(navArgument("macId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val macId = backStackEntry.arguments?.getString("macId") ?: ""
                                PowerGraphScreen(
                                    navController = navController, // Pass the navController here
                                    authViewModel = authViewModel, // Pass the authViewModel here
                                    macId = macId // Pass the macId here
                                )
                            }


                        }
                    }
                }
            }
        }
    }
}
