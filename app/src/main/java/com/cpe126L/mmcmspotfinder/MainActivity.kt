package com.cpe126L.mmcmspotfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cpe126L.mmcmspotfinder.ui.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Shows Android 12+ system splash (uses your XML theme); no extra keep condition needed
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppNav()
        }
    }
}