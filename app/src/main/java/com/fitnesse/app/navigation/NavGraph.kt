package com.fitnesse.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitnesse.app.ui.auth.AuthScreen
import com.fitnesse.app.ui.calendar.CalendarScreen
import com.fitnesse.app.ui.ootd.OotdScreen
import com.fitnesse.app.ui.settings.SettingsScreen
import com.fitnesse.app.ui.settings.SettingsViewModel
import com.fitnesse.app.ui.wardrobe.WardrobeScreen
import com.fitnesse.app.ui.wardrobe.AddClothingScreen

object Routes {
    const val AUTH = "auth"
    const val WARDROBE = "wardrobe"
    const val OOTD = "ootd"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val ADD_CLOTHING = "add_clothing"
}

@Composable
fun FITnesseNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    settingsViewModel.load()
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
                onAddClothing = { navController.navigate(Routes.ADD_CLOTHING) },
            )
        }
        composable(Routes.ADD_CLOTHING) {
            AddClothingScreen(onBack = { navController.popBackStack() })
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
                viewModel = settingsViewModel
            )
        }
    }
}
