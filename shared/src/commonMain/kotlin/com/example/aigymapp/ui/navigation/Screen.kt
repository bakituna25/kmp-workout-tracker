package com.example.aigymapp.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workout : Screen("workout")
    data object Camera : Screen("camera")
    data object Progress : Screen("progress")
    data object Profile : Screen("profile")
}
