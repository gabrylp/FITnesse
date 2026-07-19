package com.fitnesse.app.ui.ootd

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.ui.theme.GoldPrimary
import com.fitnesse.app.ui.theme.WoodBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OotdScreen(
    onBack: () -> Unit,
    viewModel: OotdViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    
    // Animation for "popping out"
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    LaunchedEffect(state.outfit) {
        if (state.outfit != null) {
            visible = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Outfit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("\u2190", fontSize = 24.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = GoldPrimary
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(color = GoldPrimary)
                }

                state.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadTodaysOutfit() }) { Text("Retry") }
                    }
                }

                state.outfit == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\uD83D\uDEAB", fontSize = 48.sp)
                        Text("No outfit yet", style = MaterialTheme.typography.titleLarge)
                        Text("Add items to your wardrobe first!", textAlign = TextAlign.Center)
                    }
                }

                else -> {
                    val outfit = state.outfit!!

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FITnesse Selects",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary
                            )
                            
                            Spacer(Modifier.height(16.dp))

                            // Display recommended items in a row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(state.items) { item ->
                                    OutfitItemCard(item)
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Text(
                                text = "Stylist's Note",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = outfit.reasoning.ifEmpty { "This combination follows classic style rules for a balanced look." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(32.dp))

                            Button(
                                onClick = { viewModel.confirmWorn() },
                                enabled = !outfit.confirmedWorn,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    disabledContainerColor = Color.Gray
                                )
                            ) {
                                Text(
                                    if (outfit.confirmedWorn) "Worn Today \u2713" else "Mark as Worn",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitItemCard(item: ClothingItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, WoodBrown.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            if (item.photoURL.isNotEmpty()) {
                AsyncImage(
                    model = item.photoURL,
                    contentDescription = item.subcategory,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(item.category.take(1).uppercase(), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.subcategory.ifEmpty { item.category },
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
