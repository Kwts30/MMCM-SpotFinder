package com.cpe126L.mmcmspotfinder.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cpe126L.mmcmspotfinder.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Simulate quick init work (fetch config, warm cache, etc.)
    LaunchedEffect(Unit) {
        delay(1500) // adjust or replace with real init
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(5.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Center logo (use your drawable name)
            Image(
                modifier = Modifier.size(450.dp), // Adjust the size as needed
                painter = painterResource(id = R.drawable.spotfinder_logowname),
                contentDescription = "MMCM SpotFinder",
            )
        }

        // Footer badges (optional: show your school/org logos if present)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // If you have these files, they’ll show; otherwise remove or rename.
            Image(
                modifier = Modifier.size(50.dp),
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = "MMCM logo"
            )
            Image(
                modifier = Modifier.size(50.dp),
                painter = painterResource(id = R.drawable.icpep_logo),
                contentDescription = "ICpEP logo"
            )
        }
    }
}