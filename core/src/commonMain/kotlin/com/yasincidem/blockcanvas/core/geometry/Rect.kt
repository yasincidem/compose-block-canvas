package com.yasincidem.blockcanvas.core.geometry

import kotlin.jvm.JvmStatic

/**
 * An axis-aligned rectangle in canvas-space floating-point coordinates.
 *
 * `Rect` uses the **half-open** convention `[left, right) × [top, bottom)`:
 * the left and top edges belong to the rectangle, the right and bottom edges
 * do not. This choice guarantees that a tiling of adjacent rectangles
 * produces a partition — no point is counted twice — which is the right
 * default for hit testing inside a node graph.
 *
 * The constructor is total for every ordered bounds: invalid rectangles
 * (negative width or height) are rejected eagerly so downstream code never
 * has to guard against them.
 *
 * ```
 * val body = Rect(left = 16f, top = 24f, right = 240f, bottom = 120f)
 * body.contains(Offset(16f, 24f))   // true  — top-left corner is inclusive
 * body.contains(Offset(240f, 24f))  // false — right edge is exclusive
 * ```
 *
 * @property left  Minimum x coordinate, inclusive.
 * @property top   Minimum y coordinate, inclusive.
 * @property right Maximum x coordinate, exclusive.
 * @property bottom Maximum y coordinate, exclusive.
 * @since 0.1.0
 */
public data class Rect(
    public val left: Float,
    public val top: Float,
    public val right: Float,
    public val bottom: Float,
) {
    init {
        require(left <= right) { "Rect left ($left) must be <= right ($right)" }
        require(top <= bottom) { "Rect top ($top) must be <= bottom ($bottom)" }
    }

    /** Width of the rectangle. Always `>= 0`. */
    public val width: Float get() = right - left

    /** Height of the rectangle. Always `>= 0`. */
    public val height: Float get() = bottom - top

    /**
     * Returns `true` when [point] lies inside this rectangle under the
     * half-open `[left, right) × [top, bottom)` convention.
     */
    /**
     * Returns `true` when [point] lies inside this rectangle under the
     * half-open `[left, right) × [top, bottom)` convention.
     */
    public fun contains(point: Offset): Boolean =
        point.x >= left && point.x < right &&
            point.y >= top && point.y < bottom

    /**
     * Returns true if this rectangle intersects [other].
     */
    public fun intersects(other: Rect): Boolean =
        left < other.right && right > other.left &&
            top < other.bottom && bottom > other.top

    public companion object {
        /** Degenerate zero-area rectangle at the origin. Contains no points. */
        @JvmStatic
        public val Zero: Rect = Rect(0f, 0f, 0f, 0f)

        /**
         * Creates a [Rect] defined by two arbitrary points [p1] and [p2].
         * The points are sorted to ensure [left] <= [right] and [top] <= [bottom].
         */
        @JvmStatic
        public fun fromPoints(p1: Offset, p2: Offset): Rect = Rect(
            left = minOf(p1.x, p2.x),
            top = minOf(p1.y, p2.y),
            right = maxOf(p1.x, p2.x),
            bottom = maxOf(p1.y, p2.y)
        )
    }
}
