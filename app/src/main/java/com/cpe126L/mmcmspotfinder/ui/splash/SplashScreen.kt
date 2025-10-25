package com.cpe126L.mmcmspotfinder.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cpe126L.mmcmspotfinder.R
import kotlinx.coroutines.delay

// ==================== Tuning knobs (edit these) ====================
private val TOP_LOGO_SIZE: Dp = 70.dp        // top logos (box size height/width)
private val TOP_LOGO_GAP: Dp = 16.dp         // gap around vertical divider
private val DIVIDER_HEIGHT: Dp = 50.dp       // divider height between the logos

private val CENTER_LOGO_WIDTH: Dp = 500.dp   // main SpotFinder logo width
private val CENTER_LOGO_HEIGHT: Dp = 500.dp  // main SpotFinder logo height
private val CENTER_LOGO_Y_OFFSET: Dp = 0.dp  // positive moves DOWN, negative moves UP

private val BUILDING_HEIGHT: Dp = 400.dp     // bottom building image height (bigger number -> taller)
// ==================================================================

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500) // change to your real init time
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EDF7)) // match your light background
    ) {
        // Bottom layer: building (edge-to-edge)
        Image(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BUILDING_HEIGHT),
            painter = painterResource(id = R.drawable.mmcm_build),
            contentDescription = "MMCM building",
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter
        )

        // Foreground layout with paddings for safe areas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: two logos centered with divider
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MMCM logo
                Image(
                    modifier = Modifier
                        .width(TOP_LOGO_SIZE)
                        .height(TOP_LOGO_SIZE),
                    painter = painterResource(id = R.drawable.mmcm_logo),
                    contentDescription = "MMCM logo",
                    // Use FillHeight so the artwork fills the box even if the PNG has transparent padding
                    contentScale = ContentScale.FillHeight
                )
                Spacer(Modifier.width(TOP_LOGO_GAP))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(DIVIDER_HEIGHT)
                        .background(Color(0x33000000))
                )
                Spacer(Modifier.width(TOP_LOGO_GAP))
                // ICpEP logo
                Image(
                    modifier = Modifier
                        .width(TOP_LOGO_SIZE)
                        .height(TOP_LOGO_SIZE),
                    painter = painterResource(id = R.drawable.icpep_logo),
                    contentDescription = "ICpEP logo",
                    contentScale = ContentScale.FillHeight
                )
            }

            // Middle spacer (the center logo is layered below using Box scope)
            Spacer(Modifier.height(8.dp))

            // Bottom spacer keeps Column's SpaceBetween structure clean;
            // the building image is drawn in the Box background layer.
            Spacer(Modifier.height(8.dp))
        }

        // Center: app logo/wordmark, independently positioned so size changes are obvious
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = CENTER_LOGO_Y_OFFSET)
                .width(CENTER_LOGO_WIDTH)
                .height(CENTER_LOGO_HEIGHT),
            painter = painterResource(id = R.drawable.spotfinder_logowname),
            contentDescription = "MMCM SpotFinder",
            contentScale = ContentScale.Fit
        )
    }
}