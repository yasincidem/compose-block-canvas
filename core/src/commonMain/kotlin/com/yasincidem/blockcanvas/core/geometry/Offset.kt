package com.yasincidem.blockcanvas.core.geometry

import kotlin.jvm.JvmStatic
import kotlin.math.hypot
import kotlinx.serialization.Serializable

/**
 * An immutable 2D point or vector expressed in canvas-space floating-point
 * coordinates.
 *
 * `Offset` is the fundamental geometric primitive of compose-block-canvas.
 * It is returned from hit tests, produced by gesture handlers, and consumed
 * by every layout and rendering primitive. Because the class is a data class
 * whose only properties are primitives, the Compose compiler infers it as
 * stable, so consumers may freely read [x] and [y] from `@Composable`
 * functions without triggering recomposition churn.
 *
 * ```
 * val origin = Offset.Zero
 * val tip = Offset(x = 100f, y = 50f)
 * val shifted = origin + tip            // Offset(100f, 50f)
 * val dist = origin.distanceTo(tip)     // ≈ 111.80
 * ```
 *
 * @property x Horizontal component.
 * @property y Vertical component.
 * @since 0.1.0
 */
@Serializable
public data class Offset(
    public val x: Float,
    public val y: Float,
) {

    /**
     * Component-wise addition. Treats both operands as vectors so the result
     * represents translation of `this` by [other].
     */
    public operator fun plus(other: Offset): Offset =
        Offset(x + other.x, y + other.y)

    /**
     * Component-wise subtraction. The result is the displacement that, when
     * added to [other], yields `this`.
     */
    public operator fun minus(other: Offset): Offset =
        Offset(x - other.x, y - other.y)

    /**
     * Euclidean distance between `this` point and [other].
     *
     * Implemented using [kotlin.math.hypot] to avoid intermediate overflow
     * for large coordinates.
     */
    public fun distanceTo(other: Offset): Float =
        hypot(x - other.x, y - other.y)

    /** Scales both components by [factor]. */
    public operator fun times(factor: Float): Offset =
        Offset(x * factor, y * factor)

    /** Divides both components by [divisor]. */
    public operator fun div(divisor: Float): Offset =
        Offset(x / divisor, y / divisor)

    public companion object {
        /** The origin `(0, 0)`. */
        @JvmStatic
        public val Zero: Offset = Offset(0f, 0f)
    }
}
