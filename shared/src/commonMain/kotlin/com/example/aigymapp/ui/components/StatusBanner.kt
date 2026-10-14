package com.example.aigymapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.ui.theme.Primary as InfoAccent
import com.example.aigymapp.ui.theme.Surface as CardSurface

/**
 * Tone for [StatusBanner]. The card surface is always the same dark
 * [CardSurface] — only the accent bars, the label, and the action color
 * change between tones.
 */
enum class SnackTone { Error, Warning, Info }

private val ToneErrorAccent = Color(0xFFEF5350)   // Same red used app-wide for errors/delete
private val ToneWarningAccent = Color(0xFFD1A02A) // Mustard

private fun SnackTone.accent(): Color = when (this) {
    SnackTone.Error -> ToneErrorAccent
    SnackTone.Warning -> ToneWarningAccent
    SnackTone.Info -> InfoAccent
}

private fun SnackTone.defaultLabel(): String = when (this) {
    SnackTone.Error -> "HATA"
    SnackTone.Warning -> "UYARI"
    SnackTone.Info -> "BİLGİ"
}

/**
 * Top-anchored status banner — slides + fades in from above the content.
 * Left accent bar, uppercase tone label, message, a divider, and a dismiss
 * action on the right, underlined by a thin accent rule along the bottom.
 */
@Composable
fun StatusBanner(
    visible: Boolean,
    tone: SnackTone,
    message: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = tone.defaultLabel(),
    actionLabel: String = "TAMAM"
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(320)) { -it } + fadeIn(animationSpec = tween(240)),
        exit = slideOutVertically(animationSpec = tween(220)) { -it } + fadeOut(animationSpec = tween(160)),
        modifier = modifier
    ) {
        val accent = tone.accent()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .background(accent)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 12.dp)
                ) {
                    Text(
                        text = label,
                        color = accent,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
                Text(
                    text = actionLabel,
                    color = accent,
                    fontSize = 13.sp,
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clickable(onClick = onAction)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accent)
            )
        }
    }
}
