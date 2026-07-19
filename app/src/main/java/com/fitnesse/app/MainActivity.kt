package com.fitnesse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fitnesse.app.data.repository.WardrobeRepository
import com.fitnesse.app.navigation.FITnesseNavGraph
import com.fitnesse.app.ui.theme.FITnesseTheme
import com.fitnesse.app.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                val settings = WardrobeRepository().getUserSettings()
                ThemeManager.theme = settings.theme
            }

            val darkTheme = when (ThemeManager.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            FITnesseTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    FITnesseNavGraph(navController = navController)
                }
            }
        }
    }
}
