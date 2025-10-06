package com.cpe126L.mmcmspotfinder.ui.screens

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cpe126L.mmcmspotfinder.R
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

/**
 * Full-screen OpenStreetMap (osmdroid), centered on MMCM (Davao),
 * with a logo loading overlay that stays for a minimum time (aesthetic latency),
 * hides on first tile load, and fades out smoothly.
 */
@Composable
fun MapScreen() {
    val context = LocalContext.current

    // MMCM verified coordinates
    val MMCM_LAT = 7.063972
    val MMCM_LON = 125.595690
    val START_ZOOM = 19.5

    // Aesthetic loading timings
    val MIN_SHOW_MS = 500L      // keep logo at least this long
    val MAX_TIMEOUT_MS = 5000L  // hard fallback

    // Required by OSM tile policy
    Configuration.getInstance().userAgentValue = context.packageName

    // Loading coordination flags
    val firstTileLoaded = remember { mutableStateOf(false) }
    val minTimerDone = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }

    // Single MapView instance
    val mapView = remember {
        MapView(context).apply {
            id = View.generateViewId()
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            val point = GeoPoint(MMCM_LAT, MMCM_LON)
            controller.setZoom(START_ZOOM)
            controller.setCenter(point)
        }
    }

    // Hide loader on first tile completion
    DisposableEffect(mapView) {
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (!firstTileLoaded.value) {
                    firstTileLoaded.value = true
                }
                // remove after first signal
                mapView.tileProvider.tileRequestCompleteHandlers.remove(this)
            }
        }
        mapView.tileProvider.tileRequestCompleteHandlers.add(handler)

        onDispose {
            mapView.tileProvider.tileRequestCompleteHandlers.remove(handler)
        }
    }

    // Lifecycle management for MapView
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    // Minimum display timer for the logo (aesthetic latency)
    LaunchedEffect(Unit) {
        delay(MIN_SHOW_MS)
        minTimerDone.value = true
    }

    // Close loader as soon as both: min time elapsed AND first tile loaded
    LaunchedEffect(firstTileLoaded.value, minTimerDone.value) {
        if (firstTileLoaded.value && minTimerDone.value) {
            isLoading.value = false
        }
    }

    // Hard fallback timeout
    LaunchedEffect(Unit) {
        delay(MAX_TIMEOUT_MS)
        isLoading.value = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map view
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Loading overlay with fade-out when it finishes
        AnimatedVisibility(
            visible = isLoading.value,
            enter = EnterTransition.None,                 // no fade-in; only fade-out when done
            exit = fadeOut(animationSpec = tween(500))    // fade-out on finish
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F4F8)), // match your splash bg
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.spotfinder_logowname),
                    contentDescription = "Loading map…",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}