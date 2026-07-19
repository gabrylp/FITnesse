package com.fitnesse.app.ui.wardrobe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnesse.app.ui.theme.GoldPrimary
import com.fitnesse.app.ui.theme.GoldSecondary
import com.fitnesse.app.ui.theme.WoodBrown
import com.fitnesse.app.ui.theme.WoodDark
import com.fitnesse.app.ui.theme.WoodLight

@Composable
fun WardrobeScreen(
    onOpenOotd: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddClothing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background == com.fitnesse.app.ui.theme.DarkBackground

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "FITnesse",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your AI Wardrobe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(32.dp))

        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .aspectRatio(0.65f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            val wardrobeWidth = maxWidth
            val wardrobeHeight = maxHeight
            
            // Door dimensions
            val doorWidth = maxWidth * 0.44f
            val doorHeight = maxHeight * 0.7f
            val topY = maxHeight * 0.12f
            val leftDoorX = maxWidth * 0.05f
            val rightDoorX = maxWidth * 0.51f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawVintageWardrobe(w, h, isDark)
                
                // Draw ornate details on doors
                drawDoorDetail(leftDoorX.toPx(), topY.toPx(), doorWidth.toPx(), doorHeight.toPx(), isDark)
                drawDoorDetail(rightDoorX.toPx(), topY.toPx(), doorWidth.toPx(), doorHeight.toPx(), isDark)
                
                drawOrnateHandle(w / 2f, topY.toPx() + doorHeight.toPx() / 2f, isDark)
            }

            // Clickable regions for doors and handle
            // Left Door -> Calendar
            Box(
                modifier = Modifier
                    .offset(x = leftDoorX, y = topY)
                    .size(width = doorWidth, height = doorHeight)
                    .clickable { onOpenCalendar() },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "\uD83D\uDCC5", fontSize = 32.sp) // Calendar sticker
                    Text("History", style = MaterialTheme.typography.labelSmall, color = if(isDark) Color.White else WoodDark)
                }
            }

            // Right Door -> Settings
            Box(
                modifier = Modifier
                    .offset(x = rightDoorX, y = topY)
                    .size(width = doorWidth, height = doorHeight)
                    .clickable { onOpenSettings() },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "\uD83D\uDC64", fontSize = 32.sp) // Profile sticker
                    Text("Profile", style = MaterialTheme.typography.labelSmall, color = if(isDark) Color.White else WoodDark)
                }
            }

            // Center Handle -> OOTD
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (topY + doorHeight / 2f - maxHeight / 2f))
                    .size(60.dp)
                    .clickable { onOpenOotd() },
            )

            // Bottom Drawer -> Add Clothing
            val drawerHeight = maxHeight * 0.15f
            val drawerY = topY + doorHeight + 8.dp
            Box(
                modifier = Modifier
                    .offset(x = leftDoorX, y = drawerY)
                    .size(width = maxWidth * 0.9f, height = drawerHeight)
                    .clickable { onAddClothing() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ Add Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) GoldSecondary else WoodBrown
                )
            }
        }

        Spacer(Modifier.height(48.dp))
        Text(
            text = "Tap handle for today's look",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

private fun DrawScope.drawVintageWardrobe(w: Float, h: Float, isDark: Boolean) {
    val bodyBaseColor = if (isDark) Color(0xFF121212) else Color(0xFFFCFAF2)
    val woodAccent = if (isDark) WoodDark else WoodLight
    val goldTrim = GoldPrimary

    // Main Body
    drawRoundRect(
        color = bodyBaseColor,
        size = Size(w, h),
        cornerRadius = CornerRadius(20f, 20f)
    )

    // Crown Molding (Top)
    val crownPath = Path().apply {
        moveTo(0f, 40f)
        quadraticTo(w / 2f, -20f, w, 40f)
        lineTo(w, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(path = crownPath, color = woodAccent)
    drawPath(path = crownPath, color = goldTrim, style = Stroke(width = 4f))

    // Base/Feet
    drawRect(
        color = woodAccent,
        topLeft = Offset(0f, h - 20f),
        size = Size(w, 20f)
    )

    // Gold Trim around the whole thing
    drawRoundRect(
        color = goldTrim,
        size = Size(w, h),
        cornerRadius = CornerRadius(20f, 20f),
        style = Stroke(width = 6f)
    )

    // Internal Dividers
    val topY = h * 0.12f
    val doorH = h * 0.7f
    val leftX = w * 0.05f
    val rightX = w * 0.51f
    val doorW = w * 0.44f

    // Draw Doors
    drawRect(color = bodyBaseColor, topLeft = Offset(leftX, topY), size = Size(doorW, doorH))
    drawRect(color = bodyBaseColor, topLeft = Offset(rightX, topY), size = Size(doorW, doorH))

    // Door Frames
    drawRect(color = woodAccent, topLeft = Offset(leftX, topY), size = Size(doorW, doorH), style = Stroke(width = 8f))
    drawRect(color = woodAccent, topLeft = Offset(rightX, topY), size = Size(doorW, doorH), style = Stroke(width = 8f))
    
    // Bottom Drawer
    val drawerY = topY + doorH + 20f
    val drawerH = h * 0.15f
    drawRoundRect(
        color = woodAccent.copy(alpha = 0.2f),
        topLeft = Offset(leftX, drawerY),
        size = Size(w * 0.9f, drawerH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = goldTrim,
        topLeft = Offset(leftX, drawerY),
        size = Size(w * 0.9f, drawerH),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawDoorDetail(x: Float, y: Float, w: Float, h: Float, isDark: Boolean) {
    val gold = GoldSecondary.copy(alpha = 0.6f)
    val padding = w * 0.15f
    
    // Inner ornate rectangle
    drawRoundRect(
        color = gold,
        topLeft = Offset(x + padding, y + padding),
        size = Size(w - padding * 2, h - padding * 2),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2f)
    )
    
    // Corner flourishes
    val flSize = 15f
    // Top Left
    drawLine(gold, Offset(x + padding, y + padding), Offset(x + padding + flSize, y + padding + flSize), 2f)
    // Top Right
    drawLine(gold, Offset(x + w - padding, y + padding), Offset(x + w - padding - flSize, y + padding + flSize), 2f)
}

private fun DrawScope.drawOrnateHandle(cx: Float, cy: Float, isDark: Boolean) {
    val gold = GoldPrimary
    
    // Backplate
    drawCircle(
        color = gold,
        radius = 24f,
        center = Offset(cx, cy),
        style = Stroke(width = 2f)
    )
    
    // Main Ring
    drawCircle(
        color = gold,
        radius = 18f,
        center = Offset(cx, cy)
    )
    
    // Center Pin
    drawCircle(
        color = if (isDark) Color.Black else Color.White,
        radius = 6f,
        center = Offset(cx, cy)
    )
}
