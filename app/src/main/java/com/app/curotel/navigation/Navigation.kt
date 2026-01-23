package com.app.curotel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.curotel.ui.dashboard.DashboardScreen
import com.app.curotel.ui.history.HistoryScreen
import com.app.curotel.ui.consultation.ConsultationScreen
import com.app.curotel.ui.onboarding.OnboardingScreen
import com.app.curotel.ui.settings.SettingsScreen
import com.app.curotel.ui.splash.SplashScreen
import com.app.curotel.viewmodel.DeviceViewModel

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object History : Screen("history")
    object Consult : Screen("consult")
    object Settings : Screen("settings")
}

/**
 * Main navigation host that defines all navigable destinations
 */
@Composable
fun CurotelNavHost(
    navController: NavHostController,
    viewModel: DeviceViewModel,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home/Dashboard Screen
        composable(Screen.Home.route) {
            DashboardScreen(viewModel = viewModel)
        }
        
        // History Screen
        composable(Screen.History.route) {
            HistoryScreen()
        }
        
        // Consult Screen - Now uses Agora Video SDK
        composable(Screen.Consult.route) {
            ConsultationScreen(
                onBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Consult.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
