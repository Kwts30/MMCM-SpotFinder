package com.cpe126L.mmcmspotfinder.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cpe126L.mmcmspotfinder.ml.TimeOnlyPredictor
import com.cpe126L.mmcmspotfinder.ui.components.GaugeArc
import com.cpe126L.mmcmspotfinder.ui.components.NextHourCard
import com.cpe126L.mmcmspotfinder.ui.components.OpenCloseCard
import com.cpe126L.mmcmspotfinder.ui.components.PeakHourCard
import com.cpe126L.mmcmspotfinder.ui.components.TrafficPreviewCard
import com.cpe126L.mmcmspotfinder.viewmodel.HomeUiState
import com.cpe126L.mmcmspotfinder.viewmodel.HomeViewModel
import com.cpe126L.mmcmspotfinder.viewmodel.OccClass
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    predictor: TimeOnlyPredictor,
    onViewMap: () -> Unit,
    zone: ZoneId
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory(predictor, zone))
    val state by vm.state.collectAsState()

    val timeFmt = DateTimeFormatter.ofPattern("h:mm a, MMM d")
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
        ) {
            item {
                Text(
                    "Home",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp)) // margin above gauge
            }
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GaugeArc(
                        percent = state.currentPercent,
                        subtitle = if (state.isOpen && state.currentPercent != null)
                            "as of ${state.slotTime.format(timeFmt)}"
                        else null,
                        isOpen = state.isOpen
                    )
                }
                Spacer(Modifier.height(5.dp)) // margin below gauge
            }

            // Predicted For You
            item {
                Text(
                    "Predicted For You",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
            }
            item {
                val pages = listOf("NextHour", "PeakHour", "OpenClose")
                val pagerState = rememberPagerState(pageCount = { pages.size })

                Box(Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        pageSpacing = 12.dp
                    ) { page ->
                        when (pages[page]) {
                            "NextHour" -> NextHourCard(state.nextHourClass, modifier = Modifier.fillMaxWidth())
                            "PeakHour" -> PeakHourCard(state.peakHourLabel, modifier = Modifier.fillMaxWidth())
                            "OpenClose" -> OpenCloseCard(state.openCloseMode, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    IconButton(
                        onClick = {
                            val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                            scope.launch { pagerState.animateScrollToPage(target) }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous") }

                    IconButton(
                        onClick = {
                            val target = (pagerState.currentPage + 1).coerceAtMost(pages.size - 1)
                            scope.launch { pagerState.animateScrollToPage(target) }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) { Icon(Icons.Outlined.ChevronRight, contentDescription = "Next") }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Traffic Overview
            item {
                Text(
                    "Davao City Traffic Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                TrafficPreviewCard(
                    lat = 7.063972,
                    lon = 125.595690,
                    zoom = 17.0,
                    onViewMap = onViewMap
                )
                Spacer(Modifier.height(16.dp))
            }

            // Recommendation
            item {
                Text(
                    "Recommend For You",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                RecommendationCard(state)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RecommendationCard(state: HomeUiState) {
    // Fixed green design with leaf icon
    val msg = when (state.currentClass) {
        OccClass.Low -> "Low Occupancy predicted at this Hour. It’s a good time to go."
        OccClass.Moderate -> "Moderate Occupancy. Expect some congestion; consider arriving a bit earlier."
        OccClass.High -> "High Occupancy predicted. Consider adjusting your schedule or transport."
        else -> "Campus closed. Predictions resume at 6:00 AM."
    }

    val bg = Color(0xFFD1FADF)
    val textColor = Color(0xFF027A48)
    val leafTint = Color(0xFF12B76A)

    Surface(
        color = bg,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = leafTint,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(msg, color = textColor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}