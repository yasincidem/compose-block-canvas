package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.geometry.Viewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ConnectionCreationTest {

    private fun state() = BlockCanvasState(initialViewport = Viewport(Offset.Zero, 1f))

    private fun nodeWithPort(nodeId: String, portId: String): Node = Node(
        id = NodeId(nodeId),
        position = Offset(0f, 0f),
        width = 100f,
        height = 60f,
        ports = listOf(Port(PortId(portId), PortSide.Right)),
    )

    private val ep1 = EndPoint(NodeId("n1"), PortId("p1"))
    private val ep2 = EndPoint(NodeId("n2"), PortId("p2"))

    @Test
    fun `startConnection sets pending connection in Click mode`() {
        val s = state()
        s.startConnection(ep1, Offset(10f, 10f))
        assertNotNull(s.pendingConnection)
        assertEquals(ConnectionMode.Click, s.pendingConnection!!.mode)
        assertEquals(ep1, s.pendingConnection!!.from)
    }

    @Test
    fun `cancelConnection clears pending connection`() {
        val s = state()
        s.startConnection(ep1)
        s.cancelConnection()
        assertNull(s.pendingConnection)
    }

    @Test
    fun `tryCommitConnection creates edge when valid`() {
        val s = state()
        s.addNode(nodeWithPort("n1", "p1"))
        s.addNode(nodeWithPort("n2", "p2"))
        s.startConnection(ep1)
        val result = s.tryCommitConnection(ep2)
        assertTrue(result)
        assertEquals(1, s.canvasState.edges.size)
        assertNull(s.pendingConnection)
    }

    @Test
    fun `tryCommitConnection rejects self-loop`() {
        val s = state()
        s.addNode(nodeWithPort("n1", "p1"))
        s.startConnection(ep1)
        val result = s.tryCommitConnection(ep1)
        assertFalse(result)
        assertEquals(0, s.canvasState.edges.size)
        assertNull(s.pendingConnection)
    }

    @Test
    fun `tryCommitConnection respects onAttempt veto`() {
        val s = state()
        s.addNode(nodeWithPort("n1", "p1"))
        s.addNode(nodeWithPort("n2", "p2"))
        s.startConnection(ep1)
        val result = s.tryCommitConnection(ep2) { _, _ -> false }
        assertFalse(result)
        assertEquals(0, s.canvasState.edges.size)
        assertNull(s.pendingConnection)
    }

    @Test
    fun `tryCommitConnection rejects duplicate edge`() {
        val s = state()
        s.addNode(nodeWithPort("n1", "p1"))
        s.addNode(nodeWithPort("n2", "p2"))
        s.startConnection(ep1)
        s.tryCommitConnection(ep2)  // first edge
        s.startConnection(ep1)
        val result = s.tryCommitConnection(ep2)  // duplicate
        assertFalse(result)
        assertEquals(1, s.canvasState.edges.size)
    }

    @Test
    fun `tryCommitConnection with no pending returns false`() {
        val s = state()
        val result = s.tryCommitConnection(ep2)
        assertFalse(result)
    }

    @Test
    fun `created edges are undoable`() {
        val s = state()
        s.addNode(nodeWithPort("n1", "p1"))
        s.addNode(nodeWithPort("n2", "p2"))
        s.startConnection(ep1)
        s.tryCommitConnection(ep2)
        assertEquals(1, s.canvasState.edges.size)
        s.undo()
        assertEquals(0, s.canvasState.edges.size)
    }
}
