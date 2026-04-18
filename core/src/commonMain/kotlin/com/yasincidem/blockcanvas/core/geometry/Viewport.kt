package com.yasincidem.blockcanvas.core.geometry

import kotlinx.serialization.Serializable

/**
 * Immutable viewport for the block canvas, encoding pan and zoom.
 *
 * Coordinate convention:
 *   screenPoint = worldPoint * zoom + pan
 *   worldPoint  = (screenPoint - pan) / zoom
 *
 * @property pan   Translation applied after scaling — the screen-space origin of the world origin.
 * @property zoom  Uniform scale factor, clamped to [[MIN_ZOOM], [MAX_ZOOM]].
 */
@Serializable
public data class Viewport(
    val pan: Offset = Offset.Zero,
    val zoom: Float = 1f,
    val minZoom: Float = DEFAULT_MIN_ZOOM,
    val maxZoom: Float = DEFAULT_MAX_ZOOM,
) {
    init {
        require(zoom in minZoom..maxZoom) {
            "zoom must be in [$minZoom, $maxZoom] but was $zoom"
        }
        require(minZoom <= maxZoom) {
            "minZoom ($minZoom) must be <= maxZoom ($maxZoom)"
        }
    }

    /** Maps a point from world space to screen space. */
    public fun worldToScreen(worldPoint: Offset): Offset =
        worldPoint * zoom + pan

    /** Maps a point from screen space to world space. */
    public fun screenToWorld(screenPoint: Offset): Offset =
        (screenPoint - pan) / zoom

    /** Returns a new [Viewport] with the given [newPan], keeping zoom unchanged. */
    public fun withPan(newPan: Offset): Viewport = copy(pan = newPan)

    /**
     * Returns a new [Viewport] zoomed to [newZoom], clamped to [[minZoom], [maxZoom]].
     *
     * The [anchor] (a screen-space point) remains stationary after the zoom.
     */
    public fun withZoom(newZoom: Float, anchor: Offset): Viewport {
        val clampedZoom = newZoom.coerceIn(minZoom, maxZoom)
        val worldAtAnchor = screenToWorld(anchor)
        val newPan = anchor - worldAtAnchor * clampedZoom
        return copy(pan = newPan, zoom = clampedZoom)
    }

    public companion object {
        public const val DEFAULT_MIN_ZOOM: Float = 0.2f
        public const val DEFAULT_MAX_ZOOM: Float = 2f

        public val Default: Viewport = Viewport()
    }
}
