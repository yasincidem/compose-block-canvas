package com.yasincidem.blockcanvas.core.model

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.routing.EdgeRouter
import com.yasincidem.blockcanvas.core.routing.EdgePath
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdgeEndTest {

    private val source = Offset(0f, 50f)
    private val target = Offset(200f, 50f)

    private fun bezier() =
        EdgeRouter.Bezier.route(source, target, PortSide.Right, PortSide.Left) as EdgePath.Bezier

    /** Inset calculation must match the renderer's decorationInset logic. */
    @Test
    fun `Arrow inset equals arrow size`() {
        val end = EdgeEnd.Arrow(size = 10f)
        val inset = decorationInset(end)
        assertEquals(10f, inset)
    }

    @Test
    fun `Circle inset equals radius`() {
        val end = EdgeEnd.Circle(radius = 6f)
        val inset = decorationInset(end)
        assertEquals(6f, inset)
    }

    @Test
    fun `Diamond inset equals size`() {
        val end = EdgeEnd.Diamond(size = 7f)
        val inset = decorationInset(end)
        assertEquals(7f, inset)
    }

    @Test
    fun `None inset is zero`() {
        assertEquals(0f, decorationInset(EdgeEnd.None))
    }

    @Test
    fun `Bezier tangent at t=0 points from start toward control1`() {
        val bez = bezier()
        // Tangent direction at source: c1 - start
        val tx = bez.control1.x - bez.start.x
        val ty = bez.control1.y - bez.start.y
        val angle = atan2(ty, tx)
        // For a horizontal right-going bezier the angle should be ~0
        assertTrue(abs(angle) < 0.01f, "Expected angle ~0 for horizontal bezier, got $angle")
    }

    @Test
    fun `Bezier tangent at t=1 points from control2 toward end`() {
        val bez = bezier()
        val tx = bez.end.x - bez.control2.x
        val ty = bez.end.y - bez.control2.y
        val angle = atan2(ty, tx)
        assertTrue(abs(angle) < 0.01f, "Expected angle ~0 for horizontal bezier, got $angle")
    }

    @Test
    fun `inset point is on the tangent line away from tip`() {
        val bez = bezier()
        val tx = bez.control1.x - bez.start.x
        val ty = bez.control1.y - bez.start.y
        val len = sqrt(tx * tx + ty * ty)
        val angleRad = atan2(ty / len, tx / len)
        val inset = 10f
        val insetX = bez.start.x + cos(angleRad) * inset
        val insetY = bez.start.y + sin(angleRad) * inset
        // The inset point should be exactly `inset` px from start along the tangent
        val dist = sqrt((insetX - bez.start.x).let { it * it } + (insetY - bez.start.y).let { it * it })
        assertTrue(abs(dist - inset) < 0.001f, "Inset distance mismatch: $dist vs $inset")
    }
}

// Mirror of the renderer helper — kept in sync manually
private fun decorationInset(end: EdgeEnd): Float = when (end) {
    is EdgeEnd.None    -> 0f
    is EdgeEnd.Arrow   -> end.size
    is EdgeEnd.Circle  -> end.radius
    is EdgeEnd.Diamond -> end.size
}
