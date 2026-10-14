package com.example.aigymapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * AI Gym App Theme
 * Forces Dark Mode with Neon Lime accent color scheme
 * 
 * @param content The composable content to wrap with the theme
 */
@Composable
fun AiGymAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AiGymAppDarkColorScheme,
        typography = AiGymAppTypography,
        content = content
    )
}
