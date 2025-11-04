package com.cpe126L.mmcmspotfinder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpe126L.mmcmspotfinder.viewmodel.OccClass
import com.cpe126L.mmcmspotfinder.viewmodel.OpenCloseMode

private val CardShape = RoundedCornerShape(16.dp)
private val CardHeight = 96.dp

@Composable
fun NextHourCard(nextClass: OccClass, modifier: Modifier = Modifier) {
    val (bg, on) = when (nextClass) {
        OccClass.Low -> Color(0xFF2ECC71) to Color.White
        OccClass.Moderate -> Color(0xFFFFC107) to Color.White
        OccClass.High -> Color(0xFFE53935) to Color.White
        OccClass.Closed -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.height(CardHeight),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Next Hour:",
                color = on.copy(alpha = 0.95f),
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Icon(
                imageVector = Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = on,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            CenterContent {
                val label = when (nextClass) {
                    OccClass.Low -> "Low"
                    OccClass.Moderate -> "Moderate"
                    OccClass.High -> "High"
                    OccClass.Closed -> "Closed"
                }
                Text(label, color = on, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("(Whole Campus)", color = on.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun PeakHourCard(peakLabel: String, modifier: Modifier = Modifier) {
    val bg = Color(0xFFE53935)
    val on = Color.White

    Card(
        modifier = modifier.height(CardHeight),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Peak Hour:",
                color = on.copy(alpha = 0.95f),
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = on,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            CenterContent {
                Text(peakLabel, color = on, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun OpenCloseCard(mode: OpenCloseMode, modifier: Modifier = Modifier) {
    val isOpen = mode == OpenCloseMode.DuringOpen
    val bg = if (isOpen) Color(0xFF233A5A) else Color(0xFFFFF4CC)
    val on = if (isOpen) Color.White else Color(0xFF15202B)
    val title = if (isOpen) "Closing Time" else "Opening Time"
    val value = if (isOpen) "7:00 PM" else "6:00 AM"
    val iconTint = if (isOpen) Color(0xFFFFF176) else Color(0xFFFFC107)

    Card(
        modifier = modifier.height(CardHeight),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(title, color = on, fontSize = 15.sp, modifier = Modifier.align(Alignment.TopStart))
            Icon(
                imageVector = Icons.Outlined.Brightness6,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            CenterContent {
                Text(value, color = on, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun BoxScope.CenterContent(content: @Composable () -> Unit) {
    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
        content()
    }
}