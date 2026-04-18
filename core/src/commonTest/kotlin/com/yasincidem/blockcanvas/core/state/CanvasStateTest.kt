package com.yasincidem.blockcanvas.core.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CanvasStateTest {

    private val port1 = Port(PortId("p1"), PortSide.Right)
    private val port2 = Port(PortId("p2"), PortSide.Left)
    
    private val node1 = Node(
        id = NodeId("n1"),
        position = Offset.Zero,
        width = 100f,
        height = 100f,
        ports = listOf(port1)
    )

    private val node2 = Node(
        id = NodeId("n2"),
        position = Offset(200f, 0f),
        width = 100f,
        height = 100f,
        ports = listOf(port2)
    )

    private val edge = Edge(
        id = EdgeId("e1"),
        from = EndPoint(node1.id, port1.id),
        to = EndPoint(node2.id, port2.id)
    )

    @Test
    fun `starts empty`() {
        val state = CanvasState()
        assertTrue(state.nodes.isEmpty())
        assertTrue(state.edges.isEmpty())
    }

    @Test
    fun `addNode adds a node`() {
        val state = CanvasState().addNode(node1)
        assertEquals(mapOf(node1.id to node1), state.nodes)
    }

    @Test
    fun `addEdge adds an edge`() {
        val state = CanvasState()
            .addNode(node1)
            .addNode(node2)
            .addEdge(edge)
        
        assertEquals(mapOf(edge.id to edge), state.edges)
    }

    @Test
    fun `addEdge throws if source node missing`() {
        var thrown = false
        try {
            CanvasState().addNode(node2).addEdge(edge)
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown, "Expected IllegalArgumentException")
    }

    @Test
    fun `addEdge throws if target node missing`() {
        var thrown = false
        try {
            CanvasState().addNode(node1).addEdge(edge)
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown, "Expected IllegalArgumentException")
    }

    @Test
    fun `removeNode removes the node and its attached edges`() {
        val state = CanvasState()
            .addNode(node1)
            .addNode(node2)
            .addEdge(edge)
            
        val newState = state.removeNode(node1.id)
        
        assertEquals(mapOf(node2.id to node2), newState.nodes)
        assertTrue(newState.edges.isEmpty(), "Edge should be removed when source node is removed")
    }

    @Test
    fun `removeNode removes target attached edges`() {
        val state = CanvasState()
            .addNode(node1)
            .addNode(node2)
            .addEdge(edge)
            
        val newState = state.removeNode(node2.id)
        
        assertEquals(mapOf(node1.id to node1), newState.nodes)
        assertTrue(newState.edges.isEmpty(), "Edge should be removed when target node is removed")
    }

    @Test
    fun `moveNode updates the node position`() {
        val newPos = Offset(50f, 75f)
        val state = CanvasState().addNode(node1).moveNode(node1.id, newPos)
        assertEquals(newPos, state.nodes[node1.id]?.position)
    }

    @Test
    fun `moveNode preserves all other node fields`() {
        val state = CanvasState().addNode(node1).moveNode(node1.id, Offset(10f, 20f))
        val moved = state.nodes[node1.id]!!
        assertEquals(node1.id, moved.id)
        assertEquals(node1.width, moved.width)
        assertEquals(node1.height, moved.height)
        assertEquals(node1.ports, moved.ports)
    }

    @Test
    fun `moveNode on unknown id is a no-op`() {
        val state = CanvasState().addNode(node1)
        val after = state.moveNode(NodeId("unknown"), Offset(1f, 1f))
        assertEquals(state, after)
    }

    @Test
    fun `moveNode does not affect edges`() {
        val state = CanvasState()
            .addNode(node1).addNode(node2).addEdge(edge)
            .moveNode(node1.id, Offset(999f, 999f))
        assertEquals(1, state.edges.size)
    }

    @Test
    fun `removeEdge removes an edge`() {
        val state = CanvasState()
            .addNode(node1)
            .addNode(node2)
            .addEdge(edge)
            
        val newState = state.removeEdge(edge.id)
        
        assertTrue(newState.edges.isEmpty())
        assertEquals(2, newState.nodes.size)
    }
}
