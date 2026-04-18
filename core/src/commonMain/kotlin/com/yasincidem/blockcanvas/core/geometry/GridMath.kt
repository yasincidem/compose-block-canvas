package com.yasincidem.blockcanvas.core.geometry

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Represents a range of grid cell indices currently visible in the viewport.
 */
public data class GridCellRange(
    val startX: Int,
    val endX: Int,
    val startY: Int,
    val endY: Int
)

/**
 * Calculates the range of grid indices (x and y) that are currently visible
 * within a canvas of [width] by [height] pixels, given a [viewport] and [spacing].
 *
 * Spacing is in world-space units.
 */
public fun calculateVisibleGridCells(
    viewport: Viewport,
    spacing: Float,
    width: Float,
    height: Float
): GridCellRange {
    val topLeft = viewport.screenToWorld(Offset.Zero)
    val bottomRight = viewport.screenToWorld(Offset(width, height))

    return GridCellRange(
        startX = floor(topLeft.x / spacing).toInt(),
        endX = ceil(bottomRight.x / spacing).toInt(),
        startY = floor(topLeft.y / spacing).toInt(),
        endY = ceil(bottomRight.y / spacing).toInt()
    )
}
