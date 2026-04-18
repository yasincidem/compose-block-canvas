package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteSelectionTest {

    private fun node(id: String) = Node(
        id = NodeId(id), position = Offset.Zero, width = 100f, height = 100f, ports = emptyList()
    )

    private fun edge(id: String, fromNode: String, toNode: String) = Edge(
        id = EdgeId(id),
        from = EndPoint(NodeId(fromNode), PortId("p")),
        to = EndPoint(NodeId(toNode), PortId("p")),
    )

    @Test
    fun `deleteSelected removes selected nodes`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))
        state.addNode(node("b"))
        state.selectOnly(NodeId("a"))

        state.deleteSelected()

        assertFalse(state.canvasState.nodes.containsKey(NodeId("a")))
        assertTrue(state.canvasState.nodes.containsKey(NodeId("b")))
    }

    @Test
    fun `deleteSelected removes selected edges`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))
        state.addNode(node("b"))
        state.addEdge(edge("e1", "a", "b"))
        state.selectOnly(EdgeId("e1"))

        state.deleteSelected()

        assertFalse(state.canvasState.edges.containsKey(EdgeId("e1")))
    }

    @Test
    fun `deleteSelected clears selection afterwards`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))
        state.selectOnly(NodeId("a"))

        state.deleteSelected()

        assertTrue(state.selectionState.selectedNodes.isEmpty())
    }

    @Test
    fun `deleteSelected on empty selection is a no-op`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))

        state.deleteSelected()

        assertEquals(1, state.canvasState.nodes.size)
    }

    @Test
    fun `deleteSelected deleting node also removes its attached edges`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))
        state.addNode(node("b"))
        state.addEdge(edge("e1", "a", "b"))
        state.selectOnly(NodeId("a"))

        state.deleteSelected()

        assertFalse(state.canvasState.nodes.containsKey(NodeId("a")))
        assertFalse(state.canvasState.edges.containsKey(EdgeId("e1")))
    }

    @Test
    fun `deleteSelected is undoable`() {
        val state = BlockCanvasState()
        state.addNode(node("a"))
        state.selectOnly(NodeId("a"))
        state.deleteSelected()

        assertFalse(state.canvasState.nodes.containsKey(NodeId("a")))
        state.undo()
        assertTrue(state.canvasState.nodes.containsKey(NodeId("a")))
    }
}
