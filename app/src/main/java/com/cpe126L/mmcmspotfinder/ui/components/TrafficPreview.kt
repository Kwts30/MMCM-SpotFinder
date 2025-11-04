package com.cpe126L.mmcmspotfinder.ui.components

import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cpe126L.mmcmspotfinder.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun TrafficPreviewCard(
    lat: Double,
    lon: Double,
    zoom: Double,
    onViewMap: () -> Unit
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(lat, lon))
            // Disable gestures to keep preview static
            setOnTouchListener { _: View, _: MotionEvent -> true }
        }
    }

    // Always-on TomTom traffic overlay in the preview
    DisposableEffect(Unit) {
        val key = runCatching { context.getString(R.string.tomtom_key) }.getOrNull().orEmpty()
        if (key.isNotBlank() && key != "YOUR_TOMTOM_KEY") {
            val overlay = createTomTomTrafficOverlay(context, key)
            mapView.overlays.add(overlay)
            mapView.invalidate()
        }
        onDispose { /* MapView manages overlays */ }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onViewMap,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF223A5E),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.72f)   // wider and centered
        ) {
            Text("View Map")
        }
    }
}

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
    val provider = MapTileProviderBasic(context, src).apply { setUseDataConnection(true) }
    return TilesOverlay(provider, context)
}