package com.fitnesse.app.ui.clothes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    viewModel: ClothingDetailViewModel = viewModel(
        key = itemId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ClothingDetailViewModel(itemId) as T
            }
        },
    ),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    val categories = listOf("Top", "Bottom", "Footwear", "Outerwear", "Accessory", "Dress")
    val patternsList = listOf("Plain", "Striped", "Plaid", "Floral", "Polka Dot", "Geometric", "Tie-Dye", "Solid")
    val lengthsList = listOf("Cropped", "Regular", "Long", "Oversized", "Fitted")

    val colorPalettes = listOf(
        listOf("#FFCDD2", "#E53935", "#B71C1C"),
        listOf("#FFE0B2", "#FF9800", "#E65100"),
        listOf("#FFF9C4", "#FFEB3B", "#F9A825"),
        listOf("#C8E6C9", "#43A047", "#1B5E20"),
        listOf("#BBDEFB", "#1E88E5", "#0D47A1"),
        listOf("#E1BEE7", "#8E24AA", "#4A148C"),
        listOf("#F8BBD0", "#EC407A", "#880E4F"),
        listOf("#D7CCC8", "#6D4C41", "#3E2723"),
        listOf("#BDBDBD", "#757575", "#424242"),
        listOf("#FFFFFF", "#FFFFFF", "#000000"),
    )
    val colorNames = listOf(
        listOf("Light Red", "Red", "Dark Red"),
        listOf("Light Orange", "Orange", "Dark Orange"),
        listOf("Light Yellow", "Yellow", "Dark Yellow"),
        listOf("Light Green", "Green", "Dark Green"),
        listOf("Light Blue", "Blue", "Dark Blue"),
        listOf("Light Purple", "Purple", "Dark Purple"),
        listOf("Light Pink", "Pink", "Dark Pink"),
        listOf("Light Brown", "Brown", "Dark Brown"),
        listOf("Light Gray", "Gray", "Dark Gray"),
        listOf("White", "White", "Black"),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Edit Item" else "Item Details",
                        fontFamily = FontFamily.Serif,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("\u2190 Close") }
                },
                actions = {
                    if (state.item != null && !state.isEditing) {
                        TextButton(onClick = { viewModel.toggleEdit() }) { Text("Edit") }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Item not found")
            }
        } else {
            val item = state.item!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (item.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = item.photoURL,
                        contentDescription = item.subcategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                if (state.isEditing) {
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(
                            value = state.editedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                            singleLine = true,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = { viewModel.updateCategory(c); catExpanded = false },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.editedSubcategory,
                        onValueChange = { viewModel.updateSubcategory(it) },
                        label = { Text("Subcategory") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))

                    var colorExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { colorExpanded = !colorExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(
                            if (state.editedDominantColor.isNotEmpty()) state.editedDominantColor else "Select",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(if (colorExpanded) "\u25BC" else "\u25B6", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                    if (colorExpanded) {
                        Column {
                            colorPalettes.forEachIndexed { paletteIdx, shades ->
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp)) {
                                    shades.forEachIndexed { shadeIdx, hex ->
                                        val colorValue = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFFBDBDBD) }
                                        val shadeName = colorNames[paletteIdx][shadeIdx]
                                        val isActive = state.editedDominantColor == shadeName
                                        Box(
                                            modifier = Modifier.weight(1f).height(24.dp).background(colorValue)
                                                .clickable { viewModel.updateDominantColor(shadeName) }
                                                .then(if (isActive) Modifier.border(2.dp, if (hex == "#FFFFFF") Color(0xFF757575) else Color.White) else Modifier),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (isActive) Text("\u2713", color = if (hex in listOf("#FFF9C4", "#FFFFFF", "#FFE0B2", "#C8E6C9", "#BBDEFB", "#E1BEE7", "#F8BBD0", "#D7CCC8", "#FFCDD2", "#BDBDBD")) Color(0xFF333333) else Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (state.editedDominantColor.isNotEmpty()) {
                        OutlinedTextField(
                            value = state.editedDominantColor,
                            onValueChange = { viewModel.updateDominantColor(it) },
                            label = { Text("Color (custom)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.editedSecondaryColor,
                        onValueChange = { viewModel.updateSecondaryColor(it) },
                        label = { Text("Secondary Color (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))

                    var patExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = patExpanded, onExpandedChange = { patExpanded = it }) {
                        OutlinedTextField(
                            value = state.editedPattern,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pattern") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(patExpanded) },
                            singleLine = true,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = patExpanded, onDismissRequest = { patExpanded = false }) {
                            patternsList.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = { viewModel.updatePattern(p); patExpanded = false },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    var lenExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = lenExpanded, onExpandedChange = { lenExpanded = it }) {
                        OutlinedTextField(
                            value = state.editedLength,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Length") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lenExpanded) },
                            singleLine = true,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = lenExpanded, onDismissRequest = { lenExpanded = false }) {
                            lengthsList.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text(l) },
                                    onClick = { viewModel.updateLength(l); lenExpanded = false },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    if (state.error != null) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.toggleEdit() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = { viewModel.save() }, modifier = Modifier.weight(1f), enabled = !state.isSaving) {
                            if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp))
                            else Text("Save")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.showDeleteConfirm() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text("Delete Item") }
                } else {
                    DetailRow("Category", item.category)
                    DetailRow("Subcategory", item.subcategory)
                    DetailRow("Color", item.dominantColor)
                    if (item.secondaryColor.isNotEmpty()) DetailRow("Secondary Color", item.secondaryColor)
                    DetailRow("Pattern", item.pattern)
                    DetailRow("Length", item.length)
                    if (item.lastWorn > 0) {
                        val lastWornDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(item.lastWorn))
                        val daysSince = (System.currentTimeMillis() - item.lastWorn) / 86400000L
                        val label = when {
                            daysSince == 0L -> "Today"
                            daysSince == 1L -> "Yesterday"
                            else -> "$daysSince days ago"
                        }
                        DetailRow("Last Worn", "$label ($lastWornDate)")
                    }
                    if (item.dateAdded > 0) {
                        val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(item.dateAdded))
                        DetailRow("Added", date)
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete() },
                    colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value.ifEmpty { "\u2014" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
