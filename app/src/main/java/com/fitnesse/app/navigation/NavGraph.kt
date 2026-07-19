package com.fitnesse.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitnesse.app.ui.auth.AuthScreen
import com.fitnesse.app.ui.calendar.CalendarScreen
import com.fitnesse.app.ui.clothes.AddClothesScreen
import com.fitnesse.app.ui.clothes.ClothingDetailScreen
import com.fitnesse.app.ui.settings.SettingsScreen
import com.fitnesse.app.ui.wardrobe.WardrobeScreen

object Routes {
    const val AUTH = "auth"
    const val WARDROBE = "wardrobe"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val ADD_CLOTHES = "add_clothes"
    const val CLOTHING_DETAIL = "clothing_detail/{itemId}"

    fun clothingDetail(itemId: String) = "clothing_detail/$itemId"
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
                onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onAddClothes = { navController.navigate(Routes.ADD_CLOTHES) },
                onOpenItemDetail = { itemId -> navController.navigate(Routes.clothingDetail(itemId)) },
            )
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
        composable(Routes.ADD_CLOTHES) {
            AddClothesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CLOTHING_DETAIL) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            ClothingDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
