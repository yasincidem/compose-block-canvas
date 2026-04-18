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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CanvasSerializationTest {

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
    fun `CanvasState roundtrips through JSON serialization`() {
        val state = CanvasState()
            .addNode(node1)
            .addNode(node2)
            .addEdge(edge)

        val json = Json.encodeToString(state)
        val decoded = Json.decodeFromString<CanvasState>(json)

        assertEquals(state, decoded)
        assertEquals(2, decoded.nodes.size)
        assertEquals(1, decoded.edges.size)
    }

    @Test
    fun `SelectionState roundtrips through JSON serialization`() {
        val state = SelectionState(
            selectedNodes = setOf(node1.id),
            selectedEdges = setOf(edge.id)
        )

        val json = Json.encodeToString(state)
        val decoded = Json.decodeFromString<SelectionState>(json)

        assertEquals(state, decoded)
        assertEquals(1, decoded.selectedNodes.size)
        assertEquals(1, decoded.selectedEdges.size)
    }
}
