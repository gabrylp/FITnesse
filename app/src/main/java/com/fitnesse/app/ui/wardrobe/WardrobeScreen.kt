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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnesse.app.ui.theme.WoodBrown
import com.fitnesse.app.ui.theme.WoodDark
import com.fitnesse.app.ui.theme.WoodLight

@Composable
fun WardrobeScreen(
    onOpenOotd: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
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
            text = "Tap a door to begin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))

        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .aspectRatio(0.6f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            val wardrobeWidth = maxWidth
            val wardrobeHeight = maxHeight
            val doorWidth = maxWidth * 0.45f
            val doorHeight = maxHeight * 0.75f
            val topY = maxHeight * 0.1f
            val leftDoorX = maxWidth * 0.04f
            val rightDoorX = maxWidth * 0.51f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val doorWidthPx = w * 0.45f
                val doorHeightPx = h * 0.75f
                val topYPx = h * 0.1f
                val leftDoorXPx = w * 0.04f
                val rightDoorXPx = w * 0.51f

                drawWardrobeBody(w, h, isDark)

                drawDoor(
                    left = leftDoorXPx, top = topYPx,
                    width = doorWidthPx, height = doorHeightPx,
                    isDark = isDark,
                )
                drawDoor(
                    left = rightDoorXPx, top = topYPx,
                    width = doorWidthPx, height = doorHeightPx,
                    isDark = isDark,
                )

                drawDoorDivider(
                    leftDoorXPx + doorWidthPx, topYPx, doorHeightPx, isDark,
                )

                drawHandle(
                    cx = w / 2f, cy = topYPx + doorHeightPx / 2f,
                    isDark = isDark,
                )

                drawDoorPanel(
                    left = leftDoorXPx + doorWidthPx * 0.1f, top = topYPx + doorHeightPx * 0.1f,
                    width = doorWidthPx * 0.8f, height = doorHeightPx * 0.5f,
                )
                drawDoorPanel(
                    left = rightDoorXPx + doorWidthPx * 0.1f, top = topYPx + doorHeightPx * 0.1f,
                    width = doorWidthPx * 0.8f, height = doorHeightPx * 0.5f,
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = leftDoorX, y = topY)
                    .size(width = doorWidth, height = doorHeight)
                    .clickable { onOpenCalendar() },
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = "\uD83D\uDCC5",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = rightDoorX, y = topY)
                    .size(width = doorWidth, height = doorHeight)
                    .clickable { onOpenSettings() },
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = "\uD83D\uDC64",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = maxWidth / 2f - 20.dp, y = topY + doorHeight / 2f - 12.dp)
                    .size(40.dp)
                    .clickable { onOpenOotd() },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\uD83D\uDEE0\uFE0F", fontSize = 24.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Open the wardrobe",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

private fun DrawScope.drawWardrobeBody(width: Float, height: Float, isDark: Boolean) {
    val baseColor = if (isDark) Color(0xFF2D2D2D) else WoodLight
    val darkEdge = if (isDark) Color(0xFF1A1A1A) else WoodDark

    drawRoundRect(
        color = baseColor,
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f),
    )
    drawRoundRect(
        color = darkEdge,
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 4f),
    )

    val trimColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFC9A000)
    drawRoundRect(
        color = trimColor,
        topLeft = Offset(6f, 6f),
        size = Size(width - 12f, height - 12f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawDoor(
    left: Float, top: Float,
    width: Float, height: Float,
    isDark: Boolean,
) {
    val doorColor = if (isDark) Color(0xFF3D3D3D) else Color(0xFFF5EDE0)
    drawRoundRect(
        color = doorColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
    )
    val border = if (isDark) Color(0xFF555555) else WoodBrown
    drawRoundRect(
        color = border,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawDoorDivider(x: Float, top: Float, height: Float, isDark: Boolean) {
    val color = if (isDark) Color(0xFF555555) else WoodDark
    drawLine(
        color = color,
        start = Offset(x, top),
        end = Offset(x, top + height),
        strokeWidth = 3f,
    )
}

private fun DrawScope.drawHandle(cx: Float, cy: Float, isDark: Boolean) {
    val handleColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFC9A000)

    drawCircle(color = handleColor, radius = 18f, center = Offset(cx, cy))
    drawCircle(
        color = Color(0xFF8B6914),
        radius = 18f,
        center = Offset(cx, cy),
        style = Stroke(width = 2f),
    )
    drawCircle(color = Color(0xFFFFF3C4), radius = 10f, center = Offset(cx, cy))
}

private fun DrawScope.drawDoorPanel(
    left: Float, top: Float,
    width: Float, height: Float,
) {
    drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f),
    )
}
