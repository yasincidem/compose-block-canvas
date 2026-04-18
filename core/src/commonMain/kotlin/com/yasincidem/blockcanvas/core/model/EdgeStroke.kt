package com.yasincidem.blockcanvas.core.model

/**
 * Stroke style for an edge.
 *
 * Dash/dot intervals are in **screen space** so patterns stay visually
 * consistent regardless of zoom level.
 */
public sealed interface EdgeStroke {
    public val width: Float

    public data class Solid(
        override val width: Float = 2f,
    ) : EdgeStroke

    public data class Dashed(
        override val width: Float = 2f,
        val dashLength: Float = 8f,
        val gapLength: Float = 4f,
    ) : EdgeStroke

    /** Rendered as dot-sized dashes (length = [width]) with round caps. */
    public data class Dotted(
        override val width: Float = 2f,
        val gapLength: Float = 4f,
    ) : EdgeStroke
}
