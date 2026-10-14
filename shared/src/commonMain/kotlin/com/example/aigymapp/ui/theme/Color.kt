package com.example.aigymapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Dark Mode + Neon Lime Palette
val Background = Color(0xFF000000)        // Pure Black - OLED optimized
val Surface = Color(0xFF0D0D0D)           // Very Dark Grey - Cards
val Primary = Color(0xFFCCFF00)           // Neon Lime Green - Buttons/Active
val Secondary = Color(0xFFFFFFFF)         // White - Primary Text
val Tertiary = Color(0xFF1A1A1A)          // Darker Grey - Inactive Elements
val OnPrimary = Color(0xFF000000)         // Black Text on Neon Buttons
val Error = Color(0xFFCF6679)             // Error Color

// Additional Supporting Colors
val OnBackground = Color(0xFFE1E1E1)      // Text on Background
val OnSurface = Color(0xFFE1E1E1)         // Text on Surface
val OnSecondary = Color(0xFF000000)       // Text on Secondary
val OnTertiary = Color(0xFFB0B0B0)        // Text on Tertiary
val OnError = Color(0xFF000000)           // Text on Error

val OutlineVariant = Color(0xFF2A2A2A)    // Subtle borders/dividers

/**
 * Dark Color Scheme with Neon Lime Accent
 */
val AiGymAppDarkColorScheme = darkColorScheme(
    // Core Colors
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,
    
    // Secondary Colors
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Tertiary,
    onSecondaryContainer = OnTertiary,
    
    // Tertiary Colors
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = Tertiary.copy(alpha = 0.5f),
    onTertiaryContainer = OnTertiary,
    
    // Background & Surface
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Tertiary,
    onSurfaceVariant = OnTertiary,
    
    // Utility Colors
    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,
    
    // Outline
    outline = OutlineVariant,
    outlineVariant = OutlineVariant.copy(alpha = 0.5f),
    
    // Surface Tints
    surfaceTint = Primary,
    inverseSurface = Secondary,
    inverseOnSurface = Background,
    inversePrimary = Background,
    
    // Scrim
    scrim = Color.Black.copy(alpha = 0.5f)
)
