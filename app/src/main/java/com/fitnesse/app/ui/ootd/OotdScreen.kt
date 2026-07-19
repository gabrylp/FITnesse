package com.fitnesse.app.ui.ootd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fitnesse.app.data.model.ClothingItem

@Composable
fun MannequinLayout(
    items: List<ClothingItem> = emptyList(),
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(32.dp),
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                val top = items.find { it.category.equals("top", true) }
                val bottom = items.find { it.category.equals("bottom", true) }
                val shoes = items.find { it.category.equals("shoes", true) || it.category.equals("footwear", true) }
                WardrobeSlot("Top", 60.dp, top)
                WardrobeSlot("Bottom", 80.dp, bottom)
                WardrobeSlot("Shoes", 40.dp, shoes)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val outerwear = items.find { it.category.equals("outerwear", true) }
                val accessory = items.find { it.category.equals("accessory", true) }
                WardrobeSlot("Outerwear", 160.dp, outerwear)
                WardrobeSlot("Accessory", 40.dp, accessory)
            }
        }
    }
}

@Composable
fun WardrobeSlot(label: String, height: Dp, item: ClothingItem? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (item != null) item.subcategory.ifEmpty { label }.uppercase() else label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(4.dp))
        val outlineColor = MaterialTheme.colorScheme.outline
        val surfaceColor = MaterialTheme.colorScheme.surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (item != null) {
                        Modifier.background(
                            parseSlotColor(item.dominantColor),
                        )
                    } else {
                        Modifier.drawBehind {
                            drawRoundRect(
                                color = outlineColor.copy(alpha = 0.4f),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                                ),
                            )
                            drawRoundRect(
                                color = surfaceColor.copy(alpha = 0.3f),
                                cornerRadius = CornerRadius(12f, 12f),
                            )
                        }
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.subcategory.take(2).uppercase().ifEmpty { item.category.take(2).uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            color = if (isLightColor(item.dominantColor)) Color(0xFF333333) else Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (item.photoURL.isNotEmpty()) {
                        AsyncImage(
                            model = item.photoURL,
                            contentDescription = item.subcategory,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

private fun isLightColor(name: String): Boolean {
    val light = listOf("white", "cream", "beige", "light yellow", "light gray", "light grey", "light pink", "light red", "light orange", "light green", "light blue", "light purple", "light brown", "yellow", "coral", "olive")
    return light.contains(name.lowercase())
}

private fun parseSlotColor(name: String): Color {
    val map = mapOf(
        "black" to 0xFF000000, "white" to 0xFFFFFFFF,
        "red" to 0xFFE53935, "dark red" to 0xFFB71C1C, "light red" to 0xFFFFCDD2,
        "pink" to 0xFFEC407A, "dark pink" to 0xFF880E4F, "light pink" to 0xFFF8BBD0,
        "orange" to 0xFFFF9800, "dark orange" to 0xFFE65100, "light orange" to 0xFFFFE0B2,
        "yellow" to 0xFFFFEB3B, "dark yellow" to 0xFFF9A825, "light yellow" to 0xFFFFF9C4,
        "green" to 0xFF43A047, "dark green" to 0xFF1B5E20, "light green" to 0xFFC8E6C9,
        "blue" to 0xFF1E88E5, "dark blue" to 0xFF0D47A1, "light blue" to 0xFFBBDEFB,
        "purple" to 0xFF8E24AA, "dark purple" to 0xFF4A148C, "light purple" to 0xFFE1BEE7,
        "brown" to 0xFF6D4C41, "dark brown" to 0xFF3E2723, "light brown" to 0xFFD7CCC8,
        "gray" to 0xFF757575, "grey" to 0xFF757575,
        "dark gray" to 0xFF424242, "dark grey" to 0xFF424242,
        "light gray" to 0xFFBDBDBD, "light grey" to 0xFFBDBDBD,
    )
    return Color(map[name.lowercase()] ?: 0xFFBDBDBD)
}

@Composable
fun ReasoningBox(reasoning: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = reasoning.ifEmpty { "No reasoning available." },
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "\u2727 Curated by Gemini",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
