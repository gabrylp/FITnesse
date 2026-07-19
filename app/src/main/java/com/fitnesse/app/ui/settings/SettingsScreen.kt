package com.fitnesse.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontFamily = FontFamily.Serif,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190 Close") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Spacer(Modifier.height(8.dp))

            ProfileCard(email = if (state.isSignedIn) state.email else "Not signed in")

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column {
                    LaundryCooldownToggle(state, viewModel)
                    if (state.settings.laundryCooldownEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        CooldownSlider(state, viewModel)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        CooldownCategoriesSelector(state, viewModel)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeSelector(state, viewModel)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun ProfileCard(email: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "\uD83D\uDC64",
                        fontSize = 24.sp,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface,
                )

            }
        }
    }
}

@Composable
private fun LaundryCooldownToggle(state: SettingsUiState, viewModel: SettingsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Laundry Cooldown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Exclude recently worn items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = state.settings.laundryCooldownEnabled,
            onCheckedChange = { viewModel.toggleCooldown(it) },
        )
    }
}

@Composable
private fun CooldownSlider(state: SettingsUiState, viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Cooldown Duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${state.settings.cooldownDays} days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = state.settings.cooldownDays.toFloat(),
            onValueChange = { viewModel.setCooldownDays(it.toInt()) },
            valueRange = 3f..7f,
            steps = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "3 days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "7 days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CooldownCategoriesSelector(state: SettingsUiState, viewModel: SettingsViewModel) {
    val categories = listOf("top", "bottom", "footwear", "outerwear", "accessory", "dress")
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Applies to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.take(3).forEach { c ->
                FilterChip(
                    selected = c in state.settings.cooldownCategories.map { it.lowercase() },
                    onClick = { viewModel.toggleCooldownCategory(c) },
                    label = { Text(c.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.drop(3).forEach { c ->
                FilterChip(
                    selected = c in state.settings.cooldownCategories.map { it.lowercase() },
                    onClick = { viewModel.toggleCooldownCategory(c) },
                    label = { Text(c.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
    }
}

@Composable
private fun ThemeSelector(state: SettingsUiState, viewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        val themes = listOf("light" to "Light", "dark" to "Dark", "system" to "System")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                themes.forEach { (value, label) ->
                    val selected = state.settings.theme == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (selected) Modifier.background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(10.dp),
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(
                            onClick = { viewModel.setTheme(value) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}


