package com.fitnesse.app.ui.clothes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothesScreen(
    onBack: () -> Unit,
    viewModel: AddClothesViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes() ?: return@let
            inputStream.close()
            val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
            viewModel.setImage(it, bytes, mimeType)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Clothes",
                        fontFamily = FontFamily.Serif,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { onBack() }) { Text("\u2190 Close") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.savedSuccessfully) {
                Spacer(Modifier.height(60.dp))
                Text(
                    text = "Clothing Added!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your item has been saved to the wardrobe.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(48.dp).fillMaxWidth(0.6f),
                ) {
                    Text("Done")
                }
                return@Column
            }

            if (state.imageUri == null) {
                Spacer(Modifier.height(40.dp))
                Text(
                    text = "Pick a photo and Gemini will auto-detect the details",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("\uD83D\uDDBC\uFE0F  Choose from Gallery")
                }
            } else if (state.isAnalyzing) {
                Spacer(Modifier.height(60.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Analyzing with Gemini AI...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (state.error != null) {
                Spacer(Modifier.height(40.dp))
                Text(
                    text = state.error ?: "Analysis failed",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { viewModel.retryAnalysis() }) {
                    Text("Retry")
                }
            } else if (state.isEditing) {
                EditAttributesCard(state, viewModel)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.toggleEdit() }) {
                    Text("\u2190 Back to summary")
                }
                Spacer(Modifier.height(8.dp))
                SaveButton(state, viewModel)
            } else {
                SummaryCard(state, viewModel)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.toggleEdit() }) {
                    Text("Edit details")
                }
                Spacer(Modifier.height(8.dp))
                SaveButton(state, viewModel)
            }
        }
    }
}

@Composable
private fun SummaryCard(state: AddClothesUiState, viewModel: AddClothesViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (state.imageUri != null) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = "Detected Attributes",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Gemini AI analyzed your photo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            SummaryRow("Category", state.category)
            SummaryRow("Subcategory", state.subcategory)
            SummaryRow("Color", state.dominantColor)
            if (state.secondaryColor.isNotEmpty()) SummaryRow("Color Variation", state.secondaryColor)
            SummaryRow("Pattern", state.pattern)
            SummaryRow("Length", state.length)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value.ifEmpty { "\u2014" },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAttributesCard(state: AddClothesUiState, viewModel: AddClothesViewModel) {
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

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Edit Attributes",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
            )
            Spacer(Modifier.height(12.dp))

            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(
                    value = state.category,
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
                value = state.subcategory,
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
                    if (state.dominantColor.isNotEmpty()) state.dominantColor else "Select",
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
                                val isActive = state.dominantColor == shadeName
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
            if (state.dominantColor.isNotEmpty()) {
                OutlinedTextField(
                    value = state.dominantColor,
                    onValueChange = { viewModel.updateDominantColor(it) },
                    label = { Text("Color (custom)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.secondaryColor,
                onValueChange = { viewModel.updateSecondaryColor(it) },
                label = { Text("Color Variation / Secondary (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            var patExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = patExpanded, onExpandedChange = { patExpanded = it }) {
                OutlinedTextField(
                    value = state.pattern,
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
                    value = state.length,
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
        }
    }
}

@Composable
private fun SaveButton(state: AddClothesUiState, viewModel: AddClothesViewModel) {
    Button(
        onClick = { viewModel.save() },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(50),
        enabled = !state.isSaving && state.category.isNotBlank(),
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Save to Wardrobe")
        }
    }
}
