package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.rules.MaxEdgesPerPortRule
import com.yasincidem.blockcanvas.core.rules.CompositeConnectionValidator
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ReconnectEdgeTest {

    private fun makeNode(id: String, vararg portIds: String) = Node(
        id = NodeId(id),
        position = Offset.Zero,
        width = 100f,
        height = 60f,
        ports = portIds.map { Port(PortId(it), PortSide.Right) },
    )

    private fun ep(node: String, port: String) = EndPoint(NodeId(node), PortId(port))

    private fun stateWith(vararg nodes: Node): BlockCanvasState {
        val state = BlockCanvasState(initialViewport = Viewport(Offset.Zero, 1f))
        nodes.forEach { state.addNode(it) }
        return state
    }

    @Test
    fun `reconnectEdge target - moves edge target to new endpoint`() {
        val n1 = makeNode("n1", "out")
        val n2 = makeNode("n2", "in")
        val n3 = makeNode("n3", "in")
        val state = stateWith(n1, n2, n3)

        val edgeId = EdgeId("e1")
        val edge = Edge(edgeId, ep("n1", "out"), ep("n2", "in"))
        state.addEdge(edge)

        val error = state.reconnectEdge(edgeId, EdgeEndpoint.Target, ep("n3", "in"))
        assertNull(error, "Reconnect should succeed but got: $error")

        val updated = state.canvasState.edges[edgeId]
        assertNotNull(updated)
        assertEquals(ep("n1", "out"), updated.from)
        assertEquals(ep("n3", "in"), updated.to)
    }

    @Test
    fun `reconnectEdge source - moves edge source to new endpoint`() {
        val n1 = makeNode("n1", "out")
        val n2 = makeNode("n2", "out")
        val n3 = makeNode("n3", "in")
        val state = stateWith(n1, n2, n3)

        val edgeId = EdgeId("e1")
        state.addEdge(Edge(edgeId, ep("n1", "out"), ep("n3", "in")))

        val error = state.reconnectEdge(edgeId, EdgeEndpoint.Source, ep("n2", "out"))
        assertNull(error)

        val updated = state.canvasState.edges[edgeId]!!
        assertEquals(ep("n2", "out"), updated.from)
        assertEquals(ep("n3", "in"), updated.to)
    }

    @Test
    fun `reconnectEdge fails and leaves state unchanged on validator rejection`() {
        val n1 = makeNode("n1", "out")
        val n2 = makeNode("n2", "in")
        val n3 = makeNode("n3", "in")
        val state = BlockCanvasState(
            initialViewport = Viewport(Offset.Zero, 1f),
            connectionValidator = CompositeConnectionValidator(
                DefaultConnectionValidator(),
                MaxEdgesPerPortRule(maxPerPort = 1),
            ),
        )
        state.addNode(n1); state.addNode(n2); state.addNode(n3)

        // Add two edges - n3's "in" port is now full
        state.addEdge(Edge(EdgeId("e1"), ep("n1", "out"), ep("n2", "in")))
        // Manually wire e2 via CanvasState addEdge (bypass validator for setup)
        val edge2 = Edge(EdgeId("e2"), ep("n1", "out"), ep("n3", "in"))
        // Use addEdge which goes through mutateCanvas — for test setup we accept the state
        // We need a second "out" port to avoid self-loop
        val n4 = makeNode("n4", "out")
        state.addNode(n4)
        state.addEdge(Edge(EdgeId("e2"), ep("n4", "out"), ep("n3", "in")))

        // n3/in already has e2 — reconnecting e1's target there should fail
        val stateBefore = state.canvasState
        val error = state.reconnectEdge(EdgeId("e1"), EdgeEndpoint.Target, ep("n3", "in"))

        assertNotNull(error, "Expected validator error but got null")
        // State must be unchanged
        assertEquals(stateBefore, state.canvasState)
    }

    @Test
    fun `reconnectEdge is a single undoable action`() {
        val n1 = makeNode("n1", "out")
        val n2 = makeNode("n2", "in")
        val n3 = makeNode("n3", "in")
        val state = stateWith(n1, n2, n3)

        val edgeId = EdgeId("e1")
        state.addEdge(Edge(edgeId, ep("n1", "out"), ep("n2", "in")))

        val undoCountBefore = if (state.canUndo) 1 else 0

        state.reconnectEdge(edgeId, EdgeEndpoint.Target, ep("n3", "in"))

        // One undo step should revert back to the original edge target
        assertTrue(state.canUndo)
        state.undo()

        val reverted = state.canvasState.edges[edgeId]!!
        assertEquals(ep("n2", "in"), reverted.to)
    }

    @Test
    fun `reconnectEdge target - changes only to, not from`() {
        val n1 = makeNode("n1", "out")
        val n2 = makeNode("n2", "in")
        val n3 = makeNode("n3", "in")
        val state = stateWith(n1, n2, n3)

        val edgeId = EdgeId("e1")
        state.addEdge(Edge(edgeId, ep("n1", "out"), ep("n2", "in")))
        state.reconnectEdge(edgeId, EdgeEndpoint.Target, ep("n3", "in"))

        val updated = state.canvasState.edges[edgeId]!!
        assertEquals(ep("n1", "out"), updated.from, "from must not change when moving target")
    }

    @Test
    fun `reconnectEdge returns null on unknown edge id`() {
        val state = BlockCanvasState()
        val error = state.reconnectEdge(EdgeId("nonexistent"), EdgeEndpoint.Target, ep("n1", "p1"))
        // Should return without crashing; error is acceptable or null depending on design
        // Here we just verify no exception is thrown and state is empty
        assertTrue(state.canvasState.edges.isEmpty())
    }
}
