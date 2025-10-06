package com.cpe126L.mmcmspotfinder.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PillTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Bottom pill bar with 3 tabs (icon above label) and a separate circular Menu button.
 * - No ripple/press halo anywhere.
 * - Pill height == Menu circle size (barHeight).
 * - Selection capsule slides between tabs (set animationDurationMs=0 to snap).
 * - When menuActive = true, selection capsule is hidden.
 */
@Composable
fun PillBottomBar(
    currentRoute: String?,
    tabs: List<PillTab>,                // expected: 3 tabs (Home, Map, Forecast)
    onSelectTab: (route: String) -> Unit,
    onCircleClick: () -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp = 60.dp,              // pill height
    circleSize: Dp = barHeight,         // circle matches pill height
    pillColor: Color = Color(0xFFE7E7E7),
    selectedTint: Color = Color(0xFF1769E0),
    selectedBg: Color = Color(0xFFDDE7FF),
    unselectedTint: Color = Color.Black,
    menuActive: Boolean = false,        // if true: hide selection indicator
    indicatorHorizontalInset: Dp = 0.dp,
    indicatorVerticalInset: Dp = 0.dp,
    animationDurationMs: Int = 520
) {
    val selectedIndexRaw = tabs.indexOfFirst { it.route == currentRoute }
    val selectedIndex = if (menuActive || selectedIndexRaw < 0) -1 else selectedIndexRaw

    val rightGap = circleSize + 16.dp // spacing for the Menu circle at right

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 30.dp)
    ) {
        // Pill container (capsule)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(end = rightGap)
                .fillMaxWidth()
                .height(barHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(pillColor)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val segmentCount = tabs.size.coerceAtLeast(1)
                    val pillWidth = this.maxWidth
                    val segmentWidth = pillWidth / segmentCount

                    // Indicator fills almost the whole segment, centered
                    val indicatorWidth = segmentWidth - (indicatorHorizontalInset * 2)
                    val indicatorHeight = barHeight - (indicatorVerticalInset * 2)

                    val targetOffset =
                        if (selectedIndex >= 0)
                            (segmentWidth * selectedIndex) + ((segmentWidth - indicatorWidth) / 2)
                        else 0.dp

                    val animatedOffsetX by animateDpAsState(
                        targetValue = targetOffset,
                        animationSpec = if (animationDurationMs > 0)
                            tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing)
                        else tween(0),
                        label = "pill-indicator-offset"
                    )

                    if (selectedIndex >= 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = animatedOffsetX)
                                .width(indicatorWidth)
                                .height(indicatorHeight)
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(selectedBg)
                        )
                    }

                    // Tabs content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = indicatorHorizontalInset),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = !menuActive && index == selectedIndex
                            PillSegment(
                                tab = tab,
                                selected = isSelected,
                                selectedTint = selectedTint,
                                unselectedTint = unselectedTint,
                                onClick = { onSelectTab(tab.route) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(vertical = indicatorVerticalInset)
                            )
                        }
                    }
                }
            }
        }

        // Custom circle "Menu" button without ripple/halo
        CircleButton(
            onClick = onCircleClick,
            size = circleSize,
            bg = pillColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = unselectedTint)
        }
    }
}

@Composable
private fun PillSegment(
    tab: PillTab,
    selected: Boolean,
    selectedTint: Color,
    unselectedTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) selectedTint else unselectedTint
    val noRipple = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = noRipple,
                indication = null, // disable ripple/halo
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(
            tab.label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * Simple circular button with no ripple/halo.
 */
@Composable
private fun CircleButton(
    onClick: () -> Unit,
    size: Dp,
    bg: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val noRipple = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = noRipple,
                indication = null, // disable ripple/halo
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}