package com.app.curotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.curotel.navigation.CurotelNavHost
import com.app.curotel.navigation.Screen
import com.app.curotel.ui.components.CurotelBottomNavBar
import com.app.curotel.ui.theme.CurotelTheme
import com.app.curotel.viewmodel.DeviceViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point for the app
 * Annotated with @AndroidEntryPoint for Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurotelTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route
                
                // Get ViewModel using Hilt
                val viewModel: DeviceViewModel = hiltViewModel()
                val consultationViewModel: com.app.curotel.viewmodel.ConsultationViewModel = hiltViewModel()
                
                // Only show bottom nav on main screens (not splash or onboarding)
                val showBottomNav = currentRoute !in listOf(
                    Screen.Splash.route,
                    Screen.Onboarding.route
                )
                
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomNav) {
                            CurotelBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        // Pop up to start destination to avoid building up back stack
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of same destination
                                        launchSingleTop = true
                                        // Restore state when reselecting
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CurotelNavHost(
                            navController = navController,
                            viewModel = viewModel,
                            consultationViewModel = consultationViewModel
                        )
                    }
                }
            }
        }
    }
}