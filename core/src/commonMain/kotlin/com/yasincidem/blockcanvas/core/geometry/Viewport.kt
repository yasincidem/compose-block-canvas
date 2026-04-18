package com.yasincidem.blockcanvas.core.geometry

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
public data class Viewport(
    val pan: Offset = Offset.Zero,
    val zoom: Float = 1f,
) {
    init {
        require(zoom in MIN_ZOOM..MAX_ZOOM) {
            "zoom must be in [$MIN_ZOOM, $MAX_ZOOM] but was $zoom"
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
     * Returns a new [Viewport] zoomed to [newZoom], clamped to [[MIN_ZOOM], [MAX_ZOOM]].
     *
     * The [anchor] (a screen-space point) remains stationary after the zoom.
     */
    public fun withZoom(newZoom: Float, anchor: Offset): Viewport {
        val clampedZoom = newZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        val worldAtAnchor = screenToWorld(anchor)
        val newPan = anchor - worldAtAnchor * clampedZoom
        return Viewport(pan = newPan, zoom = clampedZoom)
    }

    public companion object {
        public const val MIN_ZOOM: Float = 0.1f
        public const val MAX_ZOOM: Float = 10f

        public val Default: Viewport = Viewport()
    }
}
