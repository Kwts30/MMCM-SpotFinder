package com.cpe126L.mmcmspotfinder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GaugeArc(
    percent: Int?,
    // pass formatted subtitle like: "as of 5:30 PM, Oct 31"
    subtitle: String?,
    isOpen: Boolean,
    trackColor: Color = Color(0xFFE9EBEF),
    strokeWidth: Dp = 22.dp,
    size: Dp = 260.dp
) {
    val p = (percent ?: 0).coerceIn(0, 100)
    val sweep = 360f * (p / 100f)
    val valueColor = arcColorForPercent(p)

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = style
            )
            // Value
            drawArc(
                color = valueColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = style
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isOpen && percent != null) "${p}%" else "--%",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = if (!isOpen) "Campus closed" else (subtitle ?: ""),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun arcColorForPercent(p: Int): Color {
    val green = Color(0xFF2ECC71)
    val yellow = Color(0xFFFFC107)
    val red = Color(0xFFE53935)
    val t = (p / 100f).coerceIn(0f, 1f)
    return if (t <= 0.5f) {
        lerp(green, yellow, t / 0.5f)
    } else {
        lerp(yellow, red, (t - 0.5f) / 0.5f)
    }
}

private fun lerp(a: Color, b: Color, t: Float): Color {
    val clamped = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * clamped,
        green = a.green + (b.green - a.green) * clamped,
        blue = a.blue + (b.blue - a.blue) * clamped,
        alpha = a.alpha + (b.alpha - a.alpha) * clamped
    )
}