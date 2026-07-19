package com.fitnesse.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnesse.app.ui.theme.GoldPrimary

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
                title = { Text("Wardrobe Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("\u2190", fontSize = 24.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = GoldPrimary
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
                return@Scaffold
            }

            SettingsGroup(title = "Usage Rules") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Laundry Cooldown", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Wait before suggesting same item",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = state.settings.laundryCooldownEnabled,
                        onCheckedChange = { viewModel.toggleCooldown(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
                    )
                }

                if (state.settings.laundryCooldownEnabled) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Cooldown: ${state.settings.cooldownDays} days",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = state.settings.cooldownDays.toFloat(),
                        onValueChange = { viewModel.setCooldownDays(it.toInt()) },
                        valueRange = 1f..14f,
                        steps = 12,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            SettingsGroup(title = "Appearance") {
                val themes = listOf(
                    "system" to "Follow System",
                    "light" to "Vintage Light (White & Gold)",
                    "dark" to "Vintage Dark (Black & Gold)"
                )
                themes.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label)
                        RadioButton(
                            selected = state.settings.theme == value,
                            onClick = { viewModel.setTheme(value) },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SettingsGroup(title = "Account") {
                if (state.isSignedIn) {
                    Button(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign Out", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Not signed in", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
