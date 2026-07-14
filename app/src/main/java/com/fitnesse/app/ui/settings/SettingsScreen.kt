package com.fitnesse.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190 Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Text(
                text = "Laundry Cooldown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Enable cooldown")
                Switch(
                    checked = state.settings.laundryCooldownEnabled,
                    onCheckedChange = { viewModel.toggleCooldown(it) },
                )
            }

            if (state.settings.laundryCooldownEnabled) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Days between wears: ${state.settings.cooldownDays}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = state.settings.cooldownDays.toFloat(),
                    onValueChange = { viewModel.setCooldownDays(it.toInt()) },
                    valueRange = 1f..14f,
                    steps = 12,
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            val themes = listOf("system" to "Follow System", "light" to "Light (White & Gold)", "dark" to "Dark (Black & Gold)")
            themes.forEach { (value, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label)
                    RadioButton(
                        selected = state.settings.theme == value,
                        onClick = { viewModel.setTheme(value) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            if (state.isSignedIn) {
                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Sign Out")
                }
            } else {
                Text("Not signed in", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
