package com.example.deltasitemanager.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.deltasitemanager.ui.*
import com.example.deltasitemanager.ui.screens.*
import com.example.deltasitemanager.utils.getTodayDate
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.example.deltasitemanager.viewmodel.GraphViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    val onLogout = {
        authViewModel.clearSession()
        isLoggedIn = false
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Dashboard.route) { inclusive = true }
        }
    }

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
                onLogout = onLogout
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

//        composable(Screen.Analytics.route) {
//            AnalyticsScreen(navController = navController)
//        }

        composable("graph_screen/{macId}") { backStackEntry ->
            val macId = backStackEntry.arguments?.getString("macId") ?: return@composable
            val graphViewModel: GraphViewModel = viewModel()

            LaunchedEffect(Unit) {
                graphViewModel.fetchGraphData(macId, getTodayDate())
            }

            GraphScreen(
                viewModel = graphViewModel,
                navController = navController,
                macId = macId
            )
        }
    }
}
