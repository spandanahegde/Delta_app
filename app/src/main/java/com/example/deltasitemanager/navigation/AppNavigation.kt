package com.example.deltasitemanager.navigation

// Jetpack Compose core
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// Navigation
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Screens & UI
import com.example.deltasitemanager.ui.DashboardScreen
import com.example.deltasitemanager.ui.screens.GraphScreen
import com.example.deltasitemanager.ui.LoginScreen
import com.example.deltasitemanager.ui.SiteDetailScreen
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.example.deltasitemanager.viewmodel.GraphViewModel

// Utilities
import com.example.deltasitemanager.utils.getTodayDate

// Navigation routes (assumes you have a sealed class or enum named Screen)
import com.example.deltasitemanager.ui.Screen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    // Handle logout globally
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
        addLoginScreen(navController, authViewModel) { isLoggedIn = true }
        addDashboardScreen(navController, authViewModel, onLogout)
        addSiteDetailScreen(navController, authViewModel)
        addGraphScreen(navController)
        // Future: addAnalyticsScreen(navController)
    }
}

/* --------- Composable Route Builders --------- */

private fun NavGraphBuilder.addLoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    composable(Screen.Login.route) {
        LoginScreen(
            onLoginSuccess = {
                onLoginSuccess()
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            authViewModel = authViewModel
        )
    }
}

private fun NavGraphBuilder.addDashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    composable(Screen.Dashboard.route) {
        DashboardScreen(
            navController = navController,
            authViewModel = authViewModel,
            onLogout = onLogout
        )
    }
}

private fun NavGraphBuilder.addSiteDetailScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
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
}

private fun NavGraphBuilder.addGraphScreen(navController: NavHostController) {
    composable(
        route = "graph_screen/{macId}",
        arguments = listOf(navArgument("macId") { type = NavType.StringType })
    ) { backStackEntry ->
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

/*
// Uncomment when Analytics screen is implemented
private fun NavGraphBuilder.addAnalyticsScreen(navController: NavHostController) {
    composable(Screen.Analytics.route) {
        AnalyticsScreen(navController = navController)
    }
}
*/
