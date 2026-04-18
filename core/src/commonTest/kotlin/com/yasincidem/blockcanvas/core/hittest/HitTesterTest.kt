package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortDirection
import com.yasincidem.blockcanvas.core.model.PortId
import kotlin.test.Test
import kotlin.test.assertEquals

class HitTesterTest {

    private val tester = DefaultHitTester()

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun node(
        id: String,
        x: Float,
        y: Float,
        w: Float = 100f,
        h: Float = 80f,
        vararg ports: Port = emptyArray(),
    ) = Node(
        id = NodeId(id),
        position = Offset(x, y),
        width = w,
        height = h,
        ports = ports.toList(),
    )

    private fun port(id: String, dir: PortDirection = PortDirection.Out) =
        Port(PortId(id), dir)

    /** No-op resolver — all port positions unknown. */
    private val noPortPositions: (NodeId, PortId) -> Offset? = { _, _ -> null }

    // ── empty scene ───────────────────────────────────────────────────────────

    @Test
    fun `empty node list returns Empty`() {
        val result = tester.hitTest(Offset(50f, 50f), emptyList(), noPortPositions)
        assertEquals(HitResult.Empty, result)
    }

    // ── node hits ─────────────────────────────────────────────────────────────

    @Test
    fun `point inside node bounds returns Node`() {
        val n = node("n1", 0f, 0f)
        val result = tester.hitTest(Offset(50f, 40f), listOf(n), noPortPositions)
        assertEquals(HitResult.Node(NodeId("n1")), result)
    }

    @Test
    fun `point on node top-left corner returns Node`() {
        val n = node("n1", 10f, 20f)
        val result = tester.hitTest(Offset(10f, 20f), listOf(n), noPortPositions)
        assertEquals(HitResult.Node(NodeId("n1")), result)
    }

    @Test
    fun `point exactly on node right edge returns Empty (half-open rect)`() {
        val n = node("n1", 0f, 0f, w = 100f, h = 80f)
        // Rect is half-open [left, right) × [top, bottom), so right edge is excluded
        val result = tester.hitTest(Offset(100f, 40f), listOf(n), noPortPositions)
        assertEquals(HitResult.Empty, result)
    }

    @Test
    fun `point outside all nodes returns Empty`() {
        val n = node("n1", 0f, 0f)
        val result = tester.hitTest(Offset(200f, 200f), listOf(n), noPortPositions)
        assertEquals(HitResult.Empty, result)
    }

    @Test
    fun `hits the correct node among multiple`() {
        val n1 = node("n1", 0f, 0f)
        val n2 = node("n2", 200f, 200f)
        val result = tester.hitTest(Offset(250f, 230f), listOf(n1, n2), noPortPositions)
        assertEquals(HitResult.Node(NodeId("n2")), result)
    }

    // ── port hits ─────────────────────────────────────────────────────────────

    @Test
    fun `point exactly on port position returns Port`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        val portPos = Offset(0f, 40f)
        val result = tester.hitTest(
            point = portPos,
            nodes = listOf(n),
            portPosition = { _, portId -> if (portId == PortId("p1")) portPos else null },
        )
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p1")), result)
    }

    @Test
    fun `point within port tolerance returns Port`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        val portPos = Offset(0f, 40f)
        val tolerance = DefaultHitTester.DEFAULT_PORT_TOLERANCE
        val nearPort = Offset(portPos.x + tolerance - 0.1f, portPos.y)
        val result = tester.hitTest(
            point = nearPort,
            nodes = listOf(n),
            portPosition = { _, portId -> if (portId == PortId("p1")) portPos else null },
        )
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p1")), result)
    }

    @Test
    fun `point just outside port tolerance falls through to Node`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        val portPos = Offset(50f, 40f) // inside node rect
        val tolerance = DefaultHitTester.DEFAULT_PORT_TOLERANCE
        val outsidePort = Offset(portPos.x + tolerance + 1f, portPos.y)
        val result = tester.hitTest(
            point = outsidePort,
            nodes = listOf(n),
            portPosition = { _, portId -> if (portId == PortId("p1")) portPos else null },
        )
        // outsidePort is still inside the node rect (node is 100×80 from origin)
        assertEquals(HitResult.Node(NodeId("n1")), result)
    }

    @Test
    fun `port wins over node when point is within tolerance and inside node`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        val portPos = Offset(50f, 40f) // inside node rect
        val result = tester.hitTest(
            point = portPos,
            nodes = listOf(n),
            portPosition = { _, _ -> portPos },
        )
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p1")), result)
    }

    @Test
    fun `port with unknown position does not produce Port hit`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        // resolver returns null → port position unknown, skip port hit
        val result = tester.hitTest(Offset(50f, 40f), listOf(n), noPortPositions)
        assertEquals(HitResult.Node(NodeId("n1")), result)
    }

    // ── custom tolerance ──────────────────────────────────────────────────────

    @Test
    fun `custom tolerance zero only hits exact port position`() {
        val p = port("p1")
        val n = node("n1", 0f, 0f, ports = arrayOf(p))
        val portPos = Offset(50f, 40f)
        val exactHit = tester.hitTest(
            point = portPos,
            nodes = listOf(n),
            portPosition = { _, _ -> portPos },
            portTolerance = 0f,
        )
        assertEquals(HitResult.Port(NodeId("n1"), PortId("p1")), exactHit)

        val missHit = tester.hitTest(
            point = Offset(portPos.x + 0.1f, portPos.y),
            nodes = listOf(n),
            portPosition = { _, _ -> portPos },
            portTolerance = 0f,
        )
        assertEquals(HitResult.Node(NodeId("n1")), missHit)
    }
}
