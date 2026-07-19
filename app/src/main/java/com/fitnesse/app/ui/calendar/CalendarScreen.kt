package com.fitnesse.app.ui.calendar

import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.ui.theme.GoldPrimary
import com.fitnesse.app.ui.theme.WarmCream
import com.fitnesse.app.ui.theme.WoodBrown
import com.fitnesse.app.ui.theme.WoodDark
import com.fitnesse.app.ui.theme.WoodLight

private fun isLightColor(name: String): Boolean {
    val light = listOf("white", "cream", "beige", "light yellow", "light gray", "light grey", "light pink", "light red", "light orange", "light green", "light blue", "light purple", "light brown", "yellow", "coral", "olive")
    return light.contains(name.lowercase())
}

private fun parseDialogColor(name: String): Color {
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
        "navy" to 0xFF1A237E, "teal" to 0xFF00897B, "olive" to 0xFF827717,
        "coral" to 0xFFFF7043, "beige" to 0xFFF5F5DC, "cream" to 0xFFFFFDD0, "maroon" to 0xFF800000,
    )
    return Color(map[name.lowercase()] ?: 0xFFBDBDBD)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Calendar",
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
                .padding(horizontal = 12.dp),
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Spacer(Modifier.height(8.dp))

                WeeklyView(
                    onDayClick = { dateStr ->
                        state.outfits.find { it.date == dateStr }?.let { viewModel.showOutfitDetail(it) }
                    },
                    outfits = state.outfits,
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Past Outfits",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = WoodDark,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldPrimary.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.height(12.dp))

                if (state.outfits.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No outfits recorded yet.\nYour daily recommendations will appear here.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.outfits.reversed()) { outfit ->
                            OutfitHistoryCard(
                                outfit = outfit,
                                onClick = { viewModel.showOutfitDetail(outfit) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showOutfitDetail && state.selectedOutfit != null) {
        OutfitDetailDialog(
            outfit = state.selectedOutfit!!,
            items = state.selectedOutfitItems,
            onDismiss = { viewModel.hideOutfitDetail() },
            onDelete = { viewModel.deleteOutfit(state.selectedOutfit!!.id) },
        )
    }
}

@Composable
private fun WeeklyView(
    onDayClick: (String) -> Unit = {},
    outfits: List<OutfitRecommendation> = emptyList(),
) {
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)
    val currentYear = now.get(Calendar.YEAR)
    val today = now.get(Calendar.DAY_OF_MONTH)

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek())
    val weekStart = cal.clone() as Calendar
    val weekEnd = cal.clone() as Calendar
    weekEnd.add(Calendar.DAY_OF_WEEK, 6)

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")

    val monthCal = Calendar.getInstance()
    monthCal.set(currentYear, currentMonth, 1)
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1

    val outfitsByDate = outfits.associateBy { it.date }

    Column {
        Text(
            text = "${monthNames[currentMonth]} $currentYear",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            color = WoodDark,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = GoldPrimary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            dayHeaders.forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = WoodBrown,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val totalCells = startDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7

                        for (row in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                for (col in 0..6) {
                                    val dayNum = row * 7 + col - startDayOfWeek + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val isToday = dayNum == today
                                        val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayNum)
                                        val dayCal = Calendar.getInstance()
                                        dayCal.set(currentYear, currentMonth, dayNum)
                                        val inCurrentWeek = dayCal.after(weekStart) || dayCal.compareTo(weekStart) == 0
                                        val dayBeforeWeekEnd = dayCal.before(weekEnd) || dayCal.compareTo(weekEnd) == 0
                                        val isInWeek = inCurrentWeek && dayBeforeWeekEnd
                                        val hasOutfit = outfitsByDate.containsKey(dateStr)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isToday -> GoldPrimary.copy(alpha = 0.2f)
                                                        isInWeek && hasOutfit -> WoodLight.copy(alpha = 0.15f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .then(
                                                    if (isToday) Modifier.border(
                                                        1.5.dp, GoldPrimary, RoundedCornerShape(8.dp)
                                                    ) else Modifier
                                                )
                                                .then(
                                                    if (isInWeek) Modifier.clickable { onDayClick(dateStr) }
                                                    else Modifier
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isToday -> WoodDark
                                                        isInWeek -> WoodDark
                                                        else -> WoodBrown.copy(alpha = 0.35f)
                                                    },
                                                )
                                                if (hasOutfit) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 2.dp)
                                                            .size(5.dp)
                                                            .clip(RoundedCornerShape(2.5.dp))
                                                            .background(
                                                                if (isToday) GoldPrimary
                                                                else WoodBrown
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutfitHistoryCard(
    outfit: OutfitRecommendation,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmCream,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WoodLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = outfit.date.takeLast(2),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    color = WoodDark,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = outfit.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Serif,
                    color = WoodDark,
                )
                Text(
                    text = "${outfit.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Serif,
                    color = WoodBrown,
                )
                if (outfit.reasoning.isNotEmpty()) {
                    Text(
                        text = outfit.reasoning,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Serif,
                        color = WoodDark.copy(alpha = 0.6f),
                        maxLines = 1,
                    )
                }
            }
            if (outfit.confirmedWorn) {
                Text(
                    text = "Worn \u2713",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Serif,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun OutfitDetailDialog(
    outfit: OutfitRecommendation,
    items: List<ClothingItem>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Outfit - ${outfit.date}", fontFamily = FontFamily.Serif) },
        text = {
            Column {
                Text(
                    text = if (outfit.confirmedWorn) "Worn \u2713" else "Not yet worn",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Serif,
                    color = if (outfit.confirmedWorn) GoldPrimary else WoodBrown,
                )
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(parseDialogColor(item.dominantColor)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    item.category.take(1),
                                    color = if (isLightColor(item.dominantColor)) Color(0xFF333333) else Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (item.photoURL.isNotEmpty()) {
                                AsyncImage(
                                    model = item.photoURL,
                                    contentDescription = item.subcategory,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = item.subcategory.ifEmpty { item.category },
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                color = WoodDark,
                            )
                            Text(
                                text = "${item.dominantColor} \u2022 ${item.pattern}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Serif,
                                color = WoodBrown,
                            )
                        }
                    }
                }
                if (outfit.reasoning.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = outfit.reasoning,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = WoodDark.copy(alpha = 0.7f),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
