package com.cpe126L.mmcmspotfinder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cpe126L.mmcmspotfinder.ui.screens.ForecastScreen
import com.cpe126L.mmcmspotfinder.ui.screens.HomeScreen
import com.cpe126L.mmcmspotfinder.ui.screens.MapScreen
import com.cpe126L.mmcmspotfinder.ui.screens.MenuScreen
import com.cpe126L.mmcmspotfinder.ui.splash.SplashScreen

sealed class Screen(val route: String, val label: String) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Home")
    object Map : Screen("map", "Map")
    object Forecast : Screen("forecast", "Forecast")
    object Menu : Screen("menu", "Menu")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val mainScreens = listOf(Screen.Home, Screen.Map, Screen.Forecast, Screen.Menu)

    // Show bottom bar on main screens only (not on splash)
    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            if (currentRoute in mainScreens.map { it.route }) {
                NavigationBar {
                    mainScreens.forEach { screen ->
                        val destination = backStackEntry?.destination
                        NavigationBarItem(
                            selected = destination.isInHierarchy(screen.route),
                            onClick = {
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Screen.Home.route) { saveState = true }
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Home -> Icon(Icons.Filled.Home, contentDescription = null)
                                    Screen.Map -> Icon(Icons.Filled.Room, contentDescription = null)
                                    Screen.Forecast -> Icon(Icons.Filled.QueryStats, contentDescription = null)
                                    Screen.Menu -> Icon(Icons.Filled.Menu, contentDescription = null)
                                    else -> {}
                                }
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Branded loading page (after system splash)
            composable(Screen.Splash.route) {
                SplashScreen {
                    // Navigate and clear splash from back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Forecast.route) { ForecastScreen() }
            composable(Screen.Menu.route) { MenuScreen() }
        }
    }
}

private fun NavDestination?.isInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}