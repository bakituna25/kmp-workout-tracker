package com.example.aigymapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Modernist Dark" design tokens — used only by the Home screen and the Goal
 * (Yeni Hedef) flow. Scoped locally rather than merged into the global
 * MaterialTheme color scheme so the rest of the app (Antrenman, İlerleme,
 * Profil) is unaffected.
 */
object ModernistDark {
    // Same black the bottom navigation bar uses (MaterialTheme.colorScheme.background),
    // kept as one shared constant so every redesigned screen matches it exactly.
    val Bg = Background
    val BorderStrong = Color(0xFF2F2C2B) // 2px section dividers
    val BorderWeak = Color(0xFF232120)   // 1px inner dividers
    val TextPrimary = Color(0xFFF3F2F2)
    val TextMuted = Color(0xFF9B9797)
    val TextDim = Color(0xFF605D5D)
    val TextFaint = Color(0xFF3F3C3A)
    val Lime = Color(0xFFC5FF00)
    val LimeWeakBg = Color(0xFF1A1F00)
    val OnLime = Color(0xFF131211)
}
