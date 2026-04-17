package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortDirection
import com.yasincidem.blockcanvas.core.model.PortId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConnectionValidatorTest {

    private val validator: ConnectionValidator = DefaultConnectionValidator()

    private val n1 = Node(
        id = NodeId("n1"),
        position = Offset.Zero,
        width = 10f,
        height = 10f,
        ports = listOf(
            Port(PortId("in"), PortDirection.In),
            Port(PortId("out"), PortDirection.Out),
        ),
    )

    private val n2 = Node(
        id = NodeId("n2"),
        position = Offset.Zero,
        width = 10f,
        height = 10f,
        ports = listOf(
            Port(PortId("in"), PortDirection.In),
            Port(PortId("out"), PortDirection.Out),
        ),
    )

    private val portLookup: (EndPoint) -> Port? = { ep ->
        val node = when (ep.node) {
            NodeId("n1") -> n1
            NodeId("n2") -> n2
            else -> null
        }
        node?.ports?.firstOrNull { it.id == ep.port }
    }

    @Test
    fun valid_out_to_in_returns_null() {
        val from = EndPoint(NodeId("n1"), PortId("out"))
        val to = EndPoint(NodeId("n2"), PortId("in"))
        assertNull(validator.validate(from, to, emptyList(), portLookup))
    }

    @Test
    fun in_to_out_rejected_as_direction_mismatch() {
        val from = EndPoint(NodeId("n1"), PortId("in"))
        val to = EndPoint(NodeId("n2"), PortId("out"))
        val err = validator.validate(from, to, emptyList(), portLookup)
        assertEquals(
            ConnectionError.DirectionMismatch(PortDirection.In, PortDirection.Out),
            err,
        )
    }

    @Test
    fun in_to_in_rejected_as_direction_mismatch() {
        val from = EndPoint(NodeId("n1"), PortId("in"))
        val to = EndPoint(NodeId("n2"), PortId("in"))
        val err = validator.validate(from, to, emptyList(), portLookup)
        assertEquals(
            ConnectionError.DirectionMismatch(PortDirection.In, PortDirection.In),
            err,
        )
    }

    @Test
    fun out_to_out_rejected_as_direction_mismatch() {
        val from = EndPoint(NodeId("n1"), PortId("out"))
        val to = EndPoint(NodeId("n2"), PortId("out"))
        val err = validator.validate(from, to, emptyList(), portLookup)
        assertEquals(
            ConnectionError.DirectionMismatch(PortDirection.Out, PortDirection.Out),
            err,
        )
    }

    @Test
    fun self_loop_same_endpoint_rejected() {
        val ep = EndPoint(NodeId("n1"), PortId("out"))
        val err = validator.validate(ep, ep, emptyList(), portLookup)
        assertEquals(ConnectionError.SelfLoop(ep), err)
    }

    @Test
    fun same_node_different_ports_allowed() {
        val from = EndPoint(NodeId("n1"), PortId("out"))
        val to = EndPoint(NodeId("n1"), PortId("in"))
        assertNull(validator.validate(from, to, emptyList(), portLookup))
    }

    @Test
    fun missing_from_port_returns_PortNotFound() {
        val from = EndPoint(NodeId("nX"), PortId("out"))
        val to = EndPoint(NodeId("n2"), PortId("in"))
        val err = validator.validate(from, to, emptyList(), portLookup)
        assertEquals(ConnectionError.PortNotFound(from), err)
    }

    @Test
    fun missing_to_port_returns_PortNotFound() {
        val from = EndPoint(NodeId("n1"), PortId("out"))
        val to = EndPoint(NodeId("n2"), PortId("nope"))
        val err = validator.validate(from, to, emptyList(), portLookup)
        assertEquals(ConnectionError.PortNotFound(to), err)
    }

    @Test
    fun duplicate_edge_rejected() {
        val from = EndPoint(NodeId("n1"), PortId("out"))
        val to = EndPoint(NodeId("n2"), PortId("in"))
        val existing = Edge(EdgeId("e1"), from, to)
        val err = validator.validate(from, to, listOf(existing), portLookup)
        assertEquals(ConnectionError.DuplicateEdge(EdgeId("e1")), err)
    }

    @Test
    fun reverse_existing_edge_is_not_duplicate() {
        val a = EndPoint(NodeId("n1"), PortId("out"))
        val b = EndPoint(NodeId("n2"), PortId("in"))
        // (b, a) already exists; candidate (a, b) is a different directed edge.
        val reverse = Edge(EdgeId("e1"), b, a)
        assertNull(validator.validate(a, b, listOf(reverse), portLookup))
    }
}
