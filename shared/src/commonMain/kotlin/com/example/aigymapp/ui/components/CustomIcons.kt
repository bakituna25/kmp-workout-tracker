package com.example.aigymapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom icons for bottom navigation
 * All icons use 24x24 viewport for consistent sizing with Material Icons
 */
object CustomIcons {
    
    /**
     * Dumbbell icon - Filled version
     * For workout/training navigation item
     */
    val DumbbellFilled: ImageVector
        get() = ImageVector.Builder(
            name = "DumbbellFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Left weight (main)
            path(fill = SolidColor(Color.Black)) {
                moveTo(5.25f, 18.75f)
                curveToRelative(-1.275f, 0f, -2.25f, -0.975f, -2.25f, -2.25f)
                verticalLineTo(7.5f)
                curveToRelative(0f, -1.275f, 0.975f, -2.25f, 2.25f, -2.25f)
                reflectiveCurveToRelative(2.25f, 0.975f, 2.25f, 2.25f)
                verticalLineToRelative(9f)
                curveToRelative(0f, 1.275f, -0.975f, 2.25f, -2.25f, 2.25f)
                close()
            }
            // Right weight (main)
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.75f, 18.75f)
                curveToRelative(-1.275f, 0f, -2.25f, -0.975f, -2.25f, -2.25f)
                verticalLineTo(7.5f)
                curveToRelative(0f, -1.275f, 0.975f, -2.25f, 2.25f, -2.25f)
                reflectiveCurveToRelative(2.25f, 0.975f, 2.25f, 2.25f)
                verticalLineToRelative(9f)
                curveToRelative(0f, 1.275f, -0.975f, 2.25f, -2.25f, 2.25f)
                close()
            }
            // Center bar
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.25f, 12.75f)
                horizontalLineTo(6.75f)
                curveToRelative(-0.45f, 0f, -0.75f, -0.3f, -0.75f, -0.75f)
                reflectiveCurveToRelative(0.3f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(10.5f)
                curveToRelative(0.45f, 0f, 0.75f, 0.3f, 0.75f, 0.75f)
                reflectiveCurveToRelative(-0.3f, 0.75f, -0.75f, 0.75f)
                close()
            }
            // Left end cap
            path(fill = SolidColor(Color.Black)) {
                moveTo(1.5f, 7.65f)
                curveToRelative(-0.9f, 0.3f, -1.5f, 1.125f, -1.5f, 2.1f)
                verticalLineToRelative(4.5f)
                curveToRelative(0f, 0.975f, 0.6f, 1.8f, 1.5f, 2.1f)
                verticalLineTo(7.65f)
                close()
            }
            // Right end cap
            path(fill = SolidColor(Color.Black)) {
                moveTo(22.5f, 7.65f)
                verticalLineToRelative(8.7f)
                curveToRelative(0.9f, -0.3f, 1.5f, -1.125f, 1.5f, -2.1f)
                verticalLineToRelative(-4.5f)
                curveToRelative(0f, -0.975f, -0.6f, -1.8f, -1.5f, -2.1f)
                close()
            }
        }.build()
    
    /**
     * Dumbbell icon - Outlined version
     * For workout/training navigation item (unselected state)
     */
    val DumbbellOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "DumbbellOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Left weight (main)
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5.25f, 18f)
                curveToRelative(-0.825f, 0f, -1.5f, -0.675f, -1.5f, -1.5f)
                verticalLineTo(7.5f)
                curveToRelative(0f, -0.825f, 0.675f, -1.5f, 1.5f, -1.5f)
                curveToRelative(0.825f, 0f, 1.5f, 0.675f, 1.5f, 1.5f)
                verticalLineToRelative(9f)
                curveToRelative(0f, 0.825f, -0.675f, 1.5f, -1.5f, 1.5f)
                close()
            }
            // Left end cap
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2.25f, 15.75f)
                curveToRelative(-0.825f, 0f, -1.5f, -0.675f, -1.5f, -1.5f)
                verticalLineToRelative(-4.5f)
                curveToRelative(0f, -0.825f, 0.675f, -1.5f, 1.5f, -1.5f)
                curveToRelative(0.825f, 0f, 1.5f, 0.675f, 1.5f, 1.5f)
                verticalLineToRelative(4.5f)
                curveToRelative(0f, 0.825f, -0.675f, 1.5f, -1.5f, 1.5f)
                close()
            }
            // Right weight (main)
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18.75f, 6f)
                curveToRelative(0.825f, 0f, 1.5f, 0.675f, 1.5f, 1.5f)
                verticalLineToRelative(9f)
                curveToRelative(0f, 0.825f, -0.675f, 1.5f, -1.5f, 1.5f)
                curveToRelative(-0.825f, 0f, -1.5f, -0.675f, -1.5f, -1.5f)
                verticalLineTo(7.5f)
                curveToRelative(0f, -0.825f, 0.675f, -1.5f, 1.5f, -1.5f)
                close()
            }
            // Right end cap
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.75f, 8.25f)
                curveToRelative(0.825f, 0f, 1.5f, 0.675f, 1.5f, 1.5f)
                verticalLineToRelative(4.5f)
                curveToRelative(0f, 0.825f, -0.675f, 1.5f, -1.5f, 1.5f)
                curveToRelative(-0.825f, 0f, -1.5f, -0.675f, -1.5f, -1.5f)
                verticalLineToRelative(-4.5f)
                curveToRelative(0f, -0.825f, 0.675f, -1.5f, 1.5f, -1.5f)
                close()
            }
            // Center bar
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.75f, 12f)
                lineTo(17.25f, 12f)
            }
        }.build()
    
    /**
     * Calendar/Progress icon - Filled version
     * Simple modern calendar with filled body
     */
    val CalendarFilled: ImageVector
        get() = ImageVector.Builder(
            name = "CalendarFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Bottom filled area
                moveTo(2f, 19f)
                curveToRelative(0f, 1.7f, 1.3f, 3f, 3f, 3f)
                horizontalLineToRelative(14f)
                curveToRelative(1.7f, 0f, 3f, -1.3f, 3f, -3f)
                verticalLineToRelative(-8f)
                horizontalLineTo(2f)
                close()
                // Top header
                moveTo(19f, 4f)
                horizontalLineToRelative(-2f)
                verticalLineTo(3f)
                curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.4f, -1f, 1f)
                verticalLineToRelative(1f)
                horizontalLineTo(9f)
                verticalLineTo(3f)
                curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.4f, -1f, 1f)
                verticalLineToRelative(1f)
                horizontalLineTo(5f)
                curveTo(3.3f, 4f, 2f, 5.3f, 2f, 7f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(20f)
                verticalLineTo(7f)
                curveToRelative(0f, -1.7f, -1.3f, -3f, -3f, -3f)
            }
        }.build()
    
    /**
     * Calendar/Progress icon - Outlined version
     * Simple modern calendar with outline only
     */
    val CalendarOutlined: ImageVector
        get() = ImageVector.Builder(
            name = "CalendarOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Main frame outline
                moveTo(19.5f, 4f)
                horizontalLineToRelative(-3f)
                verticalLineTo(2.5f)
                curveToRelative(0f, -0.276f, -0.224f, -0.5f, -0.5f, -0.5f)
                reflectiveCurveToRelative(-0.5f, 0.224f, -0.5f, 0.5f)
                verticalLineTo(4f)
                horizontalLineToRelative(-7f)
                verticalLineTo(2.5f)
                curveToRelative(0f, -0.276f, -0.224f, -0.5f, -0.5f, -0.5f)
                reflectiveCurveToRelative(-0.5f, 0.224f, -0.5f, 0.5f)
                verticalLineTo(4f)
                horizontalLineToRelative(-3f)
                curveTo(2.567f, 4f, 1f, 5.567f, 1f, 7.5f)
                verticalLineToRelative(12f)
                curveTo(1f, 21.433f, 2.567f, 23f, 4.5f, 23f)
                horizontalLineToRelative(15f)
                curveToRelative(1.933f, 0f, 3.5f, -1.567f, 3.5f, -3.5f)
                verticalLineToRelative(-12f)
                curveTo(23f, 5.567f, 21.433f, 4f, 19.5f, 4f)
                close()
                // Inner outline
                moveTo(22f, 19.5f)
                curveToRelative(0f, 1.378f, -1.122f, 2.5f, -2.5f, 2.5f)
                horizontalLineToRelative(-15f)
                curveTo(3.122f, 22f, 2f, 20.878f, 2f, 19.5f)
                verticalLineTo(12f)
                horizontalLineToRelative(20f)
                close()
                moveTo(22f, 11f)
                horizontalLineTo(2f)
                verticalLineTo(7.5f)
                curveTo(2f, 6.122f, 3.122f, 5f, 4.5f, 5f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(1.5f)
                curveToRelative(0f, 0.276f, 0.224f, 0.5f, 0.5f, 0.5f)
                reflectiveCurveToRelative(0.5f, -0.224f, 0.5f, -0.5f)
                verticalLineTo(5f)
                horizontalLineToRelative(7f)
                verticalLineToRelative(1.5f)
                curveToRelative(0f, 0.276f, 0.224f, 0.5f, 0.5f, 0.5f)
                reflectiveCurveToRelative(0.5f, -0.224f, 0.5f, -0.5f)
                verticalLineTo(5f)
                horizontalLineToRelative(3f)
                curveTo(20.878f, 5f, 22f, 6.122f, 22f, 7.5f)
                close()
            }
        }.build()
}
