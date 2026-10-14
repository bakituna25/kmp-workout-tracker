package com.example.aigymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single-edge rule lines for the Modernist Dark screens, which use flush
 * hairline dividers instead of card elevation/rounding. Plain
 * `Modifier.border(...)` draws all four edges, which is wrong for a
 * "border-top" / "border-left" divider — these draw exactly one edge.
 */
fun Modifier.topRule(thickness: Dp, color: Color): Modifier = this.then(
    Modifier.drawBehind {
        drawRect(color = color, topLeft = Offset.Zero, size = Size(size.width, thickness.toPx()))
    }
)

fun Modifier.leftRule(thickness: Dp, color: Color): Modifier = this.then(
    Modifier.drawBehind {
        drawRect(color = color, topLeft = Offset.Zero, size = Size(thickness.toPx(), size.height))
    }
)

@Composable
fun HorizontalRule(thickness: Dp, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
