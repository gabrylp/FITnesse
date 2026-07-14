package com.fitnesse.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitnesse.app.ui.auth.AuthScreen
import com.fitnesse.app.ui.calendar.CalendarScreen
import com.fitnesse.app.ui.ootd.OotdScreen
import com.fitnesse.app.ui.settings.SettingsScreen
import com.fitnesse.app.ui.wardrobe.WardrobeScreen

object Routes {
    const val AUTH = "auth"
    const val WARDROBE = "wardrobe"
    const val OOTD = "ootd"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
}

@Composable
fun FITnesseNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.WARDROBE) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.WARDROBE) {
            WardrobeScreen(
                onOpenOotd = { navController.navigate(Routes.OOTD) },
                onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.OOTD) {
            OotdScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
