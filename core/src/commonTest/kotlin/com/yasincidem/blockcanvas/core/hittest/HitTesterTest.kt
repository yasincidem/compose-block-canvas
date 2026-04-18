package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertEquals

class HitTesterTest {

    private val tester = DefaultHitTester()

    private fun node(
        id: String,
        x: Float = 0f,
        y: Float = 0f,
        w: Float = 100f,
        h: Float = 80f,
        vararg ports: Port = emptyArray(),
    ) = Node(NodeId(id), Offset(x, y), w, h, ports.toList())

    private fun port(id: String, side: PortSide) = Port(PortId(id), side)

    // ── empty scene ───────────────────────────────────────────────────────────

    @Test
    fun `empty node list returns Empty`() {
        assertEquals(HitResult.Empty, tester.hitTest(Offset(50f, 50f), emptyList()))
    }

    // ── node hits ─────────────────────────────────────────────────────────────

    @Test
    fun `point inside node bounds returns Node`() {
        val n = node("n1")
        assertEquals(HitResult.Node(NodeId("n1")),
            tester.hitTest(Offset(50f, 40f), listOf(n)))
    }

    @Test
    fun `point on node top-left corner returns Node`() {
        val n = node("n1", x = 10f, y = 20f)
        assertEquals(HitResult.Node(NodeId("n1")),
            tester.hitTest(Offset(10f, 20f), listOf(n)))
    }

    @Test
    fun `point on right edge excluded by half-open rect`() {
        val n = node("n1", w = 100f, h = 80f)
        assertEquals(HitResult.Empty,
            tester.hitTest(Offset(100f, 40f), listOf(n)))
    }

    @Test
    fun `point outside all nodes returns Empty`() {
        assertEquals(HitResult.Empty,
            tester.hitTest(Offset(200f, 200f), listOf(node("n1"))))
    }

    @Test
    fun `hits the correct node among multiple`() {
        val n1 = node("n1", 0f, 0f)
        val n2 = node("n2", 200f, 200f)
        assertEquals(HitResult.Node(NodeId("n2")),
            tester.hitTest(Offset(250f, 230f), listOf(n1, n2)))
    }

    // ── port hits — positions from computePortPosition ────────────────────────

    @Test
    fun `right port is at right-center of node`() {
        val n = node("n1", 0f, 0f, 100f, 80f, port("p", PortSide.Right))
        // computePortPosition → Right = (0+100, 0+40) = (100, 40)
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(Offset(100f, 40f), listOf(n), portTolerance = 1f))
    }

    @Test
    fun `left port is at left-center of node`() {
        val n = node("n1", 50f, 50f, 100f, 80f, port("p", PortSide.Left))
        // Left = (50, 50+40) = (50, 90)
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(Offset(50f, 90f), listOf(n), portTolerance = 1f))
    }

    @Test
    fun `top port is at top-center of node`() {
        val n = node("n1", 0f, 0f, 100f, 80f, port("p", PortSide.Top))
        // Top = (50, 0)
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(Offset(50f, 0f), listOf(n), portTolerance = 1f))
    }

    @Test
    fun `bottom port is at bottom-center of node`() {
        val n = node("n1", 0f, 0f, 100f, 80f, port("p", PortSide.Bottom))
        // Bottom = (50, 80)
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(Offset(50f, 80f), listOf(n), portTolerance = 1f))
    }

    @Test
    fun `point within default tolerance hits port`() {
        val n = node("n1", 0f, 0f, 100f, 80f, port("p", PortSide.Right))
        val portPos = Offset(100f, 40f)
        val near = Offset(portPos.x + DefaultHitTester.DEFAULT_PORT_TOLERANCE - 1f, portPos.y)
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(near, listOf(n)))
    }

    @Test
    fun `point beyond tolerance falls through to Node`() {
        val n = node("n1", 0f, 0f, 200f, 80f, port("p", PortSide.Right))
        // Right port at (200, 40); point at (183, 40) = 17px away > default tolerance 16
        val beyond = Offset(200f - DefaultHitTester.DEFAULT_PORT_TOLERANCE - 1f, 40f)
        assertEquals(HitResult.Node(NodeId("n1")),
            tester.hitTest(beyond, listOf(n)))
    }

    @Test
    fun `port wins over node when within tolerance`() {
        // Right port of a node whose right edge is inside its own rect
        // (not possible for Right since right edge is excluded by half-open rect,
        // but tolerance extends OUTSIDE the rect)
        val n = node("n1", 0f, 0f, 100f, 80f, port("p", PortSide.Left))
        // Left port at (0, 40), which is ON the included left edge of the rect
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p")),
            tester.hitTest(Offset(0f, 40f), listOf(n), portTolerance = 1f))
    }
}
