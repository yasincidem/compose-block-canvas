package com.yasincidem.blockcanvas.core.routing

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EdgeRouterTest {

    private val source = Offset(0f, 50f)
    private val target = Offset(200f, 50f)

    @Test
    fun `Bezier route returns EdgePath_Bezier`() {
        val result = EdgeRouter.Bezier.route(source, target, PortSide.Right, PortSide.Left)
        assertIs<EdgePath.Bezier>(result)
    }

    @Test
    fun `Bezier start and end match source and target`() {
        val result = EdgeRouter.Bezier.route(source, target, PortSide.Right, PortSide.Left) as EdgePath.Bezier
        assertEquals(source, result.start)
        assertEquals(target, result.end)
    }

    @Test
    fun `Bezier control points match legacy inline math`() {
        // Legacy: handle = abs(target.x - source.x).coerceAtLeast(60f) * 0.5f
        //         c1 = (source.x + handle, source.y)
        //         c2 = (target.x - handle, target.y)
        val handle = abs(target.x - source.x).coerceAtLeast(60f) * 0.5f
        val expectedC1 = Offset(source.x + handle, source.y)
        val expectedC2 = Offset(target.x - handle, target.y)

        val result = EdgeRouter.Bezier.route(source, target, PortSide.Right, PortSide.Left) as EdgePath.Bezier
        assertEquals(expectedC1, result.control1)
        assertEquals(expectedC2, result.control2)
    }

    @Test
    fun `Bezier clamps handle to 60f minimum for short horizontal distance`() {
        val close = Offset(10f, 50f)  // source.x=0, target.x=10 → raw half=5, clamped to 30
        val result = EdgeRouter.Bezier.route(source, close, PortSide.Right, PortSide.Left) as EdgePath.Bezier
        val handle = 60f * 0.5f  // clamped min
        assertEquals(source.x + handle, result.control1.x)
        assertEquals(close.x  - handle, result.control2.x)
    }

    @Test
    fun `Bezier handle is symmetric for same-width distance`() {
        val result = EdgeRouter.Bezier.route(source, target, PortSide.Right, PortSide.Left) as EdgePath.Bezier
        val fromHandleX = result.control1.x - source.x
        val toHandleX   = target.x - result.control2.x
        assertTrue(abs(fromHandleX - toHandleX) < 0.001f)
    }
}
