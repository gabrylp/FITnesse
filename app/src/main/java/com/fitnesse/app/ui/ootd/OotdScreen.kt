package com.fitnesse.app.ui.ootd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OotdScreen(
    onBack: () -> Unit,
    viewModel: OotdViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Outfit") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.error != null -> {
                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadTodaysOutfit() }) {
                        Text("Retry")
                    }
                }

                state.outfit == null -> {
                    Text(
                        text = "No outfit yet",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Add some clothing items to your wardrobe first!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    val outfit = state.outfit!!

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Recommended for Today",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(16.dp))

                            if (outfit.items.isNotEmpty()) {
                                Text(
                                    text = "Items: ${outfit.items.size} selected",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Why this outfit?",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = outfit.reasoning.ifEmpty { "No reasoning available." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                            )

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.confirmWorn() },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                enabled = !outfit.confirmedWorn,
                            ) {
                                Text(if (outfit.confirmedWorn) "Already Worn \u2713" else "Mark as Worn")
                            }
                        }
                    }
                }
            }
        }
    }
}
