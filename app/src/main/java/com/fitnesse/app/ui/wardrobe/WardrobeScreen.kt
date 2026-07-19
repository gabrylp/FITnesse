package com.fitnesse.app.ui.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fitnesse.app.data.model.ClothingItem
import com.fitnesse.app.data.model.OutfitRecommendation
import com.fitnesse.app.data.repository.WardrobeRepository
import com.fitnesse.app.ui.ootd.MannequinLayout
import com.fitnesse.app.ui.ootd.ReasoningBox
import com.fitnesse.app.ui.theme.DarkBorder
import com.fitnesse.app.ui.theme.DarkSurface
import com.fitnesse.app.ui.theme.GoldOnDark
import com.fitnesse.app.ui.theme.GoldPrimary
import com.fitnesse.app.ui.theme.WarmCream
import com.fitnesse.app.ui.theme.WoodBrown
import com.fitnesse.app.ui.theme.WoodDark
import com.fitnesse.app.ui.theme.WoodLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddClothes: () -> Unit,
    onOpenItemDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isOpen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }

    AnimatedContent(
        targetState = isOpen,
        transitionSpec = {
            if (targetState) {
                (fadeIn(animationSpec = tween(200)) + slideInHorizontally { it })
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            } else {
                (fadeIn(animationSpec = tween(200)) + slideInHorizontally { -it })
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            }
        },
        label = "closet_transition",
    ) { opened ->
        if (opened) {
            ClosetInterior(
                onAddClothes = onAddClothes,
                showMenu = showMenu,
                onShowMenuChange = { showMenu = it },
                showManualDialog = showManualDialog,
                onShowManualDialogChange = { showManualDialog = it },
                onClose = { isOpen = false },
                onOpenItemDetail = onOpenItemDetail,
            )
        } else {
            ClosetExterior(
                onOpenCalendar = onOpenCalendar,
                onOpenSettings = onOpenSettings,
                onOpenCloset = {
                    scope.launch {
                        scale.animateTo(
                            3f,
                            animationSpec = androidx.compose.animation.core.tween(350),
                        )
                        isOpen = true
                        scale.snapTo(1f)
                    }
                },
                scale = scale.value,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClosetExterior(
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCloset: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background == com.fitnesse.app.ui.theme.DarkBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                transformOrigin = TransformOrigin.Center,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "My Wardrobe",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap the doors or handle to explore.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(24.dp))

        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .aspectRatio(0.739f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            val svgWidth = 340f
            val svgHeight = 460f
            val doorW = maxWidth * (153f / svgWidth)
            val doorH = maxHeight * (395f / svgHeight)
            val leftDoorX = maxWidth * (15f / svgWidth)
            val rightDoorX = maxWidth * (172f / svgWidth)
            val topY = maxHeight * (40f / svgHeight)

            val gold = if (isDark) GoldOnDark else GoldPrimary
            val wood = if (isDark) DarkBorder else WoodBrown
            val surfaceClr = if (isDark) DarkSurface else WarmCream
            val shadowClr = Color(0x33000000)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val sx = size.width / svgWidth
                val sy = size.height / svgHeight

                drawWardrobeBody(size, sx, sy, wood, gold, shadowClr)
                drawTopMolding(size, sx, sy, wood, gold)
                drawBaseMolding(size, sx, sy, wood, gold)
                drawLegs(size, sx, sy, gold)

                drawDoor(size, sx, sy, 15f, surfaceClr, gold, shadowClr)
                drawDoorPanels(size, sx, sy, 30f, 55f, 123f, 160f, gold)
                drawDoorPanels(size, sx, sy, 30f, 230f, 123f, 190f, gold)
                drawCalendarSticker(size, sx, sy, 80f, 100f)

                drawDoor(size, sx, sy, 172f, surfaceClr, gold, shadowClr)
                drawDoorPanels(size, sx, sy, 187f, 55f, 123f, 160f, gold)
                drawDoorPanels(size, sx, sy, 187f, 230f, 123f, 190f, gold)
                drawProfileSticker(size, sx, sy, 230f, 280f)

                drawHandle(size, sx, sy, 155f, 237f, gold)
                drawHandle(size, sx, sy, 185f, 237f, gold)
                drawHandleBars(size, sx, sy, 153f, 217f, gold)
                drawHandleBars(size, sx, sy, 183f, 217f, gold)
            }

            Box(
                modifier = Modifier
                    .offset(x = leftDoorX, y = topY)
                    .size(width = doorW, height = doorH)
                    .clickable { onOpenCalendar() },
            )

            Box(
                modifier = Modifier
                    .offset(x = rightDoorX, y = topY)
                    .size(width = doorW, height = doorH)
                    .clickable { onOpenSettings() },
            )

            Box(
                modifier = Modifier
                    .offset(x = maxWidth / 2f - 20.dp, y = maxHeight * (237f / svgHeight) - 20.dp)
                    .size(40.dp)
                    .clickable { onOpenCloset() },
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Open the wardrobe",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClosetInterior(
    onAddClothes: () -> Unit,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    showManualDialog: Boolean,
    onShowManualDialogChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onOpenItemDetail: (String) -> Unit = {},
) {
    val repository = remember { WardrobeRepository() }
    var items by remember { mutableStateOf<List<ClothingItem>>(emptyList()) }
    var outfit by remember { mutableStateOf<OutfitRecommendation?>(null) }
    var outfitItems by remember { mutableStateOf<List<ClothingItem>>(emptyList()) }
    var cooldownDays by remember { mutableStateOf(3) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            outfit = repository.getTodaysOutfit()
            items = repository.getClothingItems()
            outfitItems = if (outfit != null) repository.resolveOutfitItems(outfit!!.items) else emptyList()
            cooldownDays = repository.getUserSettings().cooldownDays
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Closet",
                        fontFamily = FontFamily.Serif,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onClose) { Text("\u2190 Close") }
                },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { onShowMenuChange(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add clothes")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) },
                ) {
                    DropdownMenuItem(
                        text = { Text("Add from Gallery") },
                        onClick = {
                            onShowMenuChange(false)
                            onAddClothes()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Add Manually") },
                        onClick = {
                            onShowMenuChange(false)
                            onShowManualDialogChange(true)
                        },
                    )
                }
            }
        },
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (outfit != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Today's Outfit",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(16.dp))
                            MannequinLayout(items = outfitItems)
                            Spacer(Modifier.height(16.dp))
                            ReasoningBox(reasoning = outfit!!.reasoning)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.confirmWorn(outfit!!.id, outfit!!.items)
                                            loadData()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(50),
                                    enabled = !outfit!!.confirmedWorn,
                                ) {
                                    Text(if (outfit!!.confirmedWorn) "Worn \u2713" else "Mark as Worn")
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            repository.regenerateOutfit()
                                            loadData()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Text("New Outfit")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No outfit today",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Add clothes to get a recommendation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                Text(
                    text = "My Collection",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Tap + to add your first clothing item",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items.chunked(2).forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                rowItems.forEach { item ->
                                    ClothingCard(
                                        item = item,
                                        onClick = { onOpenItemDetail(item.id) },
                                        modifier = Modifier.weight(1f),
                                        cooldownDays = cooldownDays,
                                    )
                                }
                                if (rowItems.size < 2) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showManualDialog) {
        ManualAddDialog(
            onDismiss = { onShowManualDialogChange(false) },
            onSaved = { onShowManualDialogChange(false) },
        )
    }
}

@Composable
private fun ClothingCard(item: ClothingItem, onClick: () -> Unit = {}, modifier: Modifier = Modifier, cooldownDays: Int = 3) {
    val bgColor = parseColor(item.dominantColor)

    val cooldownRemaining = if (item.lastWorn > 0L) {
        val cooldownEnd = item.lastWorn + cooldownDays * 86400000L
        cooldownEnd - System.currentTimeMillis()
    } else 0L

    val wornText = when {
        cooldownRemaining > 0 -> {
            val hours = cooldownRemaining / 3600000L
            if (hours < 24) "${hours}h left" else "${hours / 24}d left"
        }
        item.lastWorn > 0 -> {
            val daysSince = (System.currentTimeMillis() - item.lastWorn) / 86400000L
            when {
                daysSince == 0L -> "Worn today"
                daysSince == 1L -> "Worn yesterday"
                else -> "Worn ${daysSince}d ago"
            }
        }
        else -> null
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = item.subcategory.take(2).uppercase().ifEmpty { item.category.take(2).uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isLightColor(item.dominantColor)) Color(0xFF333333) else Color.White,
                    )
                }
                if (item.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = item.photoURL,
                        contentDescription = item.subcategory,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                if (cooldownRemaining > 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x88000000), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "On cooldown",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.subcategory.ifEmpty { item.category },
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            if (wornText != null) {
                Text(
                    text = wornText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cooldownRemaining > 0) Color(0xFFFFB74D) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            } else if (item.dominantColor.isNotEmpty()) {
                Text(
                    text = item.dominantColor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }}
    }
}

private fun isLightColor(name: String): Boolean {
    val light = listOf("white", "cream", "beige", "light yellow", "light gray", "light grey", "light pink", "light red", "light orange", "light green", "light blue", "light purple", "light brown", "yellow", "coral", "olive")
    return light.contains(name.lowercase())
}

private fun parseColor(name: String): Color {
    return when (name.lowercase()) {
        "black" -> Color(0xFF000000)
        "white" -> Color(0xFFFFFFFF)
        "red" -> Color(0xFFE53935)
        "dark red" -> Color(0xFFB71C1C)
        "light red" -> Color(0xFFFFCDD2)
        "pink" -> Color(0xFFEC407A)
        "dark pink" -> Color(0xFF880E4F)
        "light pink" -> Color(0xFFF8BBD0)
        "orange" -> Color(0xFFFF9800)
        "dark orange" -> Color(0xFFE65100)
        "light orange" -> Color(0xFFFFE0B2)
        "yellow" -> Color(0xFFFFEB3B)
        "dark yellow" -> Color(0xFFF9A825)
        "light yellow" -> Color(0xFFFFF9C4)
        "green" -> Color(0xFF43A047)
        "dark green" -> Color(0xFF1B5E20)
        "light green" -> Color(0xFFC8E6C9)
        "blue" -> Color(0xFF1E88E5)
        "dark blue" -> Color(0xFF0D47A1)
        "light blue" -> Color(0xFFBBDEFB)
        "purple" -> Color(0xFF8E24AA)
        "dark purple" -> Color(0xFF4A148C)
        "light purple" -> Color(0xFFE1BEE7)
        "brown" -> Color(0xFF6D4C41)
        "dark brown" -> Color(0xFF3E2723)
        "light brown" -> Color(0xFFD7CCC8)
        "gray", "grey" -> Color(0xFF757575)
        "dark gray", "dark grey" -> Color(0xFF424242)
        "light gray", "light grey" -> Color(0xFFBDBDBD)
        "navy" -> Color(0xFF1A237E)
        "teal" -> Color(0xFF00897B)
        "olive" -> Color(0xFF827717)
        "coral" -> Color(0xFFFF7043)
        "beige" -> Color(0xFFF5F5DC)
        "cream" -> Color(0xFFFFFDD0)
        "maroon" -> Color(0xFF800000)
        else -> Color(0xFFBDBDBD)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualAddDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Top") }
    var dominantColor by remember { mutableStateOf("") }
    var secondaryColor by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { WardrobeRepository() }
    val categories = listOf("Top", "Bottom", "Footwear", "Outerwear", "Accessory", "Dress")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        photoUri = uri
        photoBytes = uri?.let {
            context.contentResolver.openInputStream(it)?.readBytes()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            val uri = cameraPhotoUri
            if (uri != null) {
                photoUri = uri
                photoBytes = context.contentResolver.openInputStream(uri)?.readBytes()
            }
        }
    }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Item Manually",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                var colorExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { colorExpanded = !colorExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (dominantColor.isNotEmpty()) dominantColor else "Select",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (colorExpanded) "\u25BC" else "\u25B6",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                if (colorExpanded) {
                    Column {
                        colorPalettes.forEachIndexed { paletteIdx, shades ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp),
                            ) {
                                shades.forEachIndexed { shadeIdx, hex ->
                                    val colorValue = try {
                                        Color(android.graphics.Color.parseColor(hex))
                                    } catch (_: Exception) { Color(0xFFBDBDBD) }
                                    val shadeName = colorNames[paletteIdx][shadeIdx]
                                    val isActive = dominantColor == shadeName
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                            .background(colorValue)
                                            .clickable { dominantColor = shadeName }
                                            .then(
                                                if (isActive) Modifier.border(
                                                    2.dp,
                                                    if (hex == "#FFFFFF") Color(0xFF757575) else Color.White,
                                                ) else Modifier
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isActive) {
                                            Text(
                                                text = "\u2713",
                                                color = if (hex in listOf("#FFF9C4", "#FFFFFF", "#FFE0B2", "#C8E6C9", "#BBDEFB", "#E1BEE7", "#F8BBD0", "#D7CCC8", "#FFCDD2", "#BDBDBD"))
                                                    Color(0xFF333333) else Color.White,
                                                fontSize = 12.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = secondaryColor,
                    onValueChange = { secondaryColor = it },
                    label = { Text("Color Variation / Secondary (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                val lengthsList = listOf("Cropped", "Regular", "Long", "Oversized", "Fitted")
                var lengthExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = lengthExpanded,
                    onExpandedChange = { lengthExpanded = it },
                ) {
                    OutlinedTextField(
                        value = length,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Length") },
                        placeholder = { Text("Select length") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(lengthExpanded) },
                        singleLine = true,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = lengthExpanded,
                        onDismissRequest = { lengthExpanded = false },
                    ) {
                        lengthsList.forEach { l ->
                            DropdownMenuItem(
                                text = { Text(l) },
                                onClick = {
                                    length = l
                                    lengthExpanded = false
                                },
                            )
                        }
                    }
                }

                val patternsList = listOf("Plain", "Striped", "Plaid", "Floral", "Polka Dot", "Geometric", "Tie-Dye", "Solid")
                var patternExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = patternExpanded,
                    onExpandedChange = { patternExpanded = it },
                ) {
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pattern") },
                        placeholder = { Text("Select pattern") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(patternExpanded) },
                        singleLine = true,
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = patternExpanded,
                        onDismissRequest = { patternExpanded = false },
                    ) {
                        patternsList.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    pattern = p
                                    patternExpanded = false
                                },
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Gallery")
                    }
                    Button(
                        onClick = {
                            val tempFile = java.io.File.createTempFile("camera_", ".jpg", context.cacheDir)
                            cameraPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", tempFile
                            )
                            cameraLauncher.launch(cameraPhotoUri!!)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Camera")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        try {
                            val photoUrl = if (photoBytes != null) {
                                repository.uploadPhoto(photoBytes!!)
                            } else ""
                            repository.addClothingItem(
                                ClothingItem(
                                    category = category,
                                    subcategory = name,
                                    dominantColor = dominantColor,
                                    secondaryColor = secondaryColor,
                                    pattern = pattern,
                                    length = length,
                                    photoURL = photoUrl,
                                )
                            )
                            onSaved()
                        } catch (_: Exception) {
                            isSaving = false
                        }
                    }
                },
                enabled = name.isNotBlank() && !isSaving,
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun DrawScope.drawWardrobeBody(size: Size, sx: Float, sy: Float, wood: Color, gold: Color, shadow: Color) {
    drawRoundRect(
        color = shadow,
        topLeft = Offset(12f * sx, 22f * sy),
        size = Size(320f * sx, 420f * sy),
        cornerRadius = CornerRadius(8f * (sx + sy) / 2f, 8f * (sx + sy) / 2f),
        alpha = 0.3f,
    )
    drawRoundRect(
        color = wood,
        topLeft = Offset(10f * sx, 20f * sy),
        size = Size(320f * sx, 420f * sy),
        cornerRadius = CornerRadius(8f * (sx + sy) / 2f, 8f * (sx + sy) / 2f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(10f * sx, 20f * sy),
        size = Size(320f * sx, 420f * sy),
        cornerRadius = CornerRadius(8f * (sx + sy) / 2f, 8f * (sx + sy) / 2f),
        style = Stroke(width = 4f * (sx + sy) / 2f),
    )
}

private fun DrawScope.drawTopMolding(size: Size, sx: Float, sy: Float, wood: Color, gold: Color) {
    val path = Path().apply {
        moveTo(0f, 20f * sy)
        quadraticTo(170f * sx, 0f, size.width, 20f * sy)
        lineTo(330f * sx, 35f * sy)
        lineTo(10f * sx, 35f * sy)
        close()
    }
    val shadow = Path().apply {
        moveTo(2f, 22f * sy)
        quadraticTo(170f * sx, 2f, size.width - 2f, 22f * sy)
        lineTo(328f * sx, 37f * sy)
        lineTo(12f * sx, 37f * sy)
        close()
    }
    drawPath(shadow, color = Color(0x22000000))
    drawPath(path, color = wood)
    drawPath(path, color = gold, style = Stroke(width = 2f * (sx + sy) / 2f))
}

private fun DrawScope.drawBaseMolding(size: Size, sx: Float, sy: Float, wood: Color, gold: Color) {
    drawRoundRect(
        color = Color(0x22000000),
        topLeft = Offset(7f * sx, 442f * sy),
        size = Size(330f * sx, 20f * sy),
        cornerRadius = CornerRadius(4f * (sx + sy) / 2f, 4f * (sx + sy) / 2f),
    )
    drawRoundRect(
        color = wood,
        topLeft = Offset(5f * sx, 440f * sy),
        size = Size(330f * sx, 20f * sy),
        cornerRadius = CornerRadius(4f * (sx + sy) / 2f, 4f * (sx + sy) / 2f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(5f * sx, 440f * sy),
        size = Size(330f * sx, 20f * sy),
        cornerRadius = CornerRadius(4f * (sx + sy) / 2f, 4f * (sx + sy) / 2f),
        style = Stroke(width = 2f * (sx + sy) / 2f),
    )
}

private fun DrawScope.drawLegs(size: Size, sx: Float, sy: Float, gold: Color) {
    drawRoundRect(
        color = gold,
        topLeft = Offset(20f * sx, 460f * sy),
        size = Size(20f * sx, 15f * sy),
        cornerRadius = CornerRadius(2f, 2f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(300f * sx, 460f * sy),
        size = Size(20f * sx, 15f * sy),
        cornerRadius = CornerRadius(2f, 2f),
    )
}

private fun DrawScope.drawDoor(
    size: Size, sx: Float, sy: Float, x: Float,
    surface: Color, gold: Color, shadow: Color,
) {
    drawRoundRect(
        color = shadow,
        topLeft = Offset((x + 2f) * sx, 42f * sy),
        size = Size(153f * sx, 395f * sy),
        cornerRadius = CornerRadius(4f, 4f),
        alpha = 0.3f,
    )
    drawRoundRect(
        color = surface,
        topLeft = Offset(x * sx, 40f * sy),
        size = Size(153f * sx, 395f * sy),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(x * sx, 40f * sy),
        size = Size(153f * sx, 395f * sy),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 2f * (sx + sy) / 2f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = Offset((x + 3f) * sx, 43f * sy),
        size = Size(147f * sx, 389f * sy),
        cornerRadius = CornerRadius(3f, 3f),
    )
}

private fun DrawScope.drawDoorPanels(
    size: Size, sx: Float, sy: Float,
    x: Float, y: Float, w: Float, h: Float,
    gold: Color,
) {
    drawRoundRect(
        color = Color(0x11000000),
        topLeft = Offset((x + 2f) * sx, (y + 2f) * sy),
        size = Size(w * sx, h * sy),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(x * sx, y * sy),
        size = Size(w * sx, h * sy),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f),
        alpha = 0.4f,
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.05f),
        topLeft = Offset((x + 2f) * sx, (y + 2f) * sy),
        size = Size((w - 4f) * sx, (h - 4f) * sy),
        cornerRadius = CornerRadius(3f, 3f),
    )
}

private fun DrawScope.drawCalendarSticker(
    size: Size, sx: Float, sy: Float,
    x: Float, y: Float,
) {
    val cx = x * sx
    val cy = y * sy
    val sw = 52f * sx
    val sh = 48f * sy

    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(cx + 2f, cy + 2f),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color(0xFFFFF5E0),
        topLeft = Offset(cx, cy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color(0xFFC62828),
        topLeft = Offset(cx, cy),
        size = Size(sw, 14f * sy),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRect(
        color = Color(0xFFC62828),
        topLeft = Offset(cx, cy + 6f * sy),
        size = Size(sw, 8f * sy),
    )
    drawRoundRect(
        color = Color(0xFF8D6E63),
        topLeft = Offset(cx, cy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 1.5f),
    )
    drawCircle(color = Color(0xFF8D6E63), radius = 1.5f, center = Offset(cx + 9f * sx, cy + 5f * sy))
    drawCircle(color = Color(0xFF8D6E63), radius = 1.5f, center = Offset(cx + sw - 9f * sx, cy + 5f * sy))
    drawCircle(color = Color(0x22000000), radius = 3f, center = Offset(cx + sw / 2f, cy - 2f * sy))
    drawCircle(color = Color(0xFFBB8F5A), radius = 2.5f, center = Offset(cx + sw / 2f, cy - 3f * sy))

    listOf(22f, 29f, 36f).forEach { yOff ->
        drawLine(
            color = Color(0xFFBCAAA4),
            start = Offset(cx + 6f * sx, cy + yOff * sy),
            end = Offset(cx + sw - 6f * sx, cy + yOff * sy),
            strokeWidth = 0.5f,
            alpha = 0.6f,
        )
    }
}

private fun DrawScope.drawProfileSticker(
    size: Size, sx: Float, sy: Float,
    x: Float, y: Float,
) {
    val cx = (x + 16f) * sx
    val cy = (y + 16f) * sy
    val pw = 42f * sx
    val ph = 52f * sy
    val photoLeft = cx - pw / 2f
    val photoTop = cy - ph / 2f
    val photoW = 34f * sx
    val photoH = 34f * sy
    val photoX = cx - photoW / 2f
    val photoY = cy - ph / 2f + 2f * sy

    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(photoLeft + 2f, photoTop + 2f),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = Color(0xFFFFF5E0),
        topLeft = Offset(photoLeft, photoTop),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = Color(0xFF8D6E63),
        topLeft = Offset(photoLeft, photoTop),
        size = Size(pw, ph),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f),
    )
    drawRoundRect(
        color = Color(0xFFF5E6CC),
        topLeft = Offset(photoX, photoY),
        size = Size(photoW, photoH),
        cornerRadius = CornerRadius(2f, 2f),
    )

    drawCircle(color = Color(0x22000000), radius = 3f, center = Offset(cx, cy - ph / 2f - 4f * sy))
    drawCircle(color = Color(0xFFBB8F5A), radius = 2.5f, center = Offset(cx, cy - ph / 2f - 5f * sy))

    val headR = 3.5f * (sx + sy) / 2f
    drawCircle(color = Color(0xFF555555), radius = headR, center = Offset(cx, photoY + photoH * 0.3f))
    val bodyPath = Path().apply {
        moveTo(cx - 6f * sx, photoY + photoH * 0.65f)
        cubicTo(cx - 6f * sx, photoY + photoH * 0.45f, cx + 6f * sx, photoY + photoH * 0.45f, cx + 6f * sx, photoY + photoH * 0.65f)
    }
    drawPath(bodyPath, color = Color(0xFF555555), style = Stroke(width = 2.5f))
}

private fun DrawScope.drawHandle(size: Size, sx: Float, sy: Float, cx: Float, cy: Float, gold: Color) {
    drawCircle(color = Color(0x44000000), radius = 7f * (sx + sy) / 2f, center = Offset((cx + 1f) * sx, (cy + 1f) * sy))
    drawCircle(color = gold, radius = 6f * (sx + sy) / 2f, center = Offset(cx * sx, cy * sy))
}

private fun DrawScope.drawHandleBars(size: Size, sx: Float, sy: Float, x: Float, y: Float, gold: Color) {
    drawRoundRect(
        color = Color(0x44000000),
        topLeft = Offset((x + 1f) * sx, (y + 1f) * sy),
        size = Size(4f * sx, 40f * sy),
        cornerRadius = CornerRadius(2f, 2f),
    )
    drawRoundRect(
        color = gold,
        topLeft = Offset(x * sx, y * sy),
        size = Size(4f * sx, 40f * sy),
        cornerRadius = CornerRadius(2f, 2f),
    )
}
