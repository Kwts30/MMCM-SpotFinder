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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

/**
 * OSM (osmdroid) base map + optional TomTom Traffic Flow overlay (transparent raster).
 * - Keeps your loading logo and lifecycle handling.
 * - Traffic is togglable from the UI.
 */
@Composable
fun MapScreen() {
    val context = LocalContext.current

    // MMCM verified coordinates
    val MMCM_LAT = 7.063534
    val MMCM_LON = 125.595635
    val START_ZOOM = 20.0

    // Aesthetic loading timings
    val MIN_SHOW_MS = 500L      // keep logo at least this long
    val MAX_TIMEOUT_MS = 5000L  // hard fallback

    // Required by OSM tile policy
    Configuration.getInstance().userAgentValue = context.packageName

    // Loading coordination flags
    val firstTileLoaded = remember { mutableStateOf(false) }
    val minTimerDone = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }

    // Read TomTom key; if placeholder/blank, overlay is disabled
    val tomtomKey = remember { runCatching { context.getString(R.string.tomtom_key) }.getOrNull().orEmpty() }
    var trafficOn by remember { mutableStateOf(true) } // default on

    // Single MapView instance
    val mapView = remember {
        MapView(context).apply {
            id = View.generateViewId()
            setTileSource(TileSourceFactory.MAPNIK) // OSM base
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

            val point = GeoPoint(MMCM_LAT, MMCM_LON)
            controller.setZoom(START_ZOOM)
            controller.setCenter(point)
        }
    }

    // Create TomTom traffic overlay (transparent tiles)
    val trafficOverlay = remember(tomtomKey) {
        if (tomtomKey.isNotBlank() && tomtomKey != "YOUR_TOMTOM_KEY") {
            createTomTomTrafficOverlay(context, tomtomKey, palette = "relative0")
        } else {
            null
        }
    }

    // Attach/detach overlay when toggled or when overlay available
    DisposableEffect(trafficOn, trafficOverlay, mapView) {
        if (trafficOn && trafficOverlay != null) {
            if (!mapView.overlays.contains(trafficOverlay)) {
                mapView.overlays.add(trafficOverlay) // on top of base
            }
            mapView.invalidate()
        } else {
            if (trafficOverlay != null && mapView.overlays.contains(trafficOverlay)) {
                mapView.overlays.remove(trafficOverlay)
                mapView.invalidate()
            }
        }
        onDispose { /* no-op; lifecycle block handles pause/resume */ }
    }

    // Hide loader on first base tile load (OSM)
    DisposableEffect(mapView) {
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (!firstTileLoaded.value) {
                    firstTileLoaded.value = true
                }
                mapView.tileProvider.tileRequestCompleteHandlers.remove(this)
            }
        }
        mapView.tileProvider.tileRequestCompleteHandlers.add(handler)
        onDispose { mapView.tileProvider.tileRequestCompleteHandlers.remove(handler) }
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

    // Close loader as soon as both: min time elapsed AND first base tile loaded
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
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.spotfinder_logowname),
                    contentDescription = "Loading map…",
                    modifier = Modifier.size(350.dp)
                )
            }
        }
    }
}

/**
 * Builds a TilesOverlay using TomTom Traffic Flow raster tiles.
 * palette: "relative0".."relative3" change the color scheme.
 */
private fun createTomTomTrafficOverlay(
    context: android.content.Context,
    apiKey: String,
    palette: String = "relative0"
): TilesOverlay {
    val src = object : OnlineTileSourceBase(
        "TomTomTraffic",
        0, 20, 256, ".png",
        arrayOf("https://api.tomtom.com/")
    ) {
        override fun getTileURLString(index: Long): String {
            val z = MapTileIndex.getZoom(index)
            val x = MapTileIndex.getX(index)
            val y = MapTileIndex.getY(index)
            return "https://api.tomtom.com/traffic/map/4/tile/flow/$palette/$z/$x/$y.png?key=$apiKey"
        }
    }
    val provider = MapTileProviderBasic(context, src).apply {
        setUseDataConnection(true)
    }
    return TilesOverlay(provider, context).apply {
        // Make loading placeholders invisible; traffic tiles themselves are transparent overlays.
        setLoadingBackgroundColor(android.graphics.Color.TRANSPARENT)
        setLoadingLineColor(android.graphics.Color.TRANSPARENT)
    }
}