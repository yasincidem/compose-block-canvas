package com.yasincidem.blockcanvas.core.rules

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
import kotlin.test.assertNull

class ConnectionValidatorTest {

    private val validator: ConnectionValidator = DefaultConnectionValidator()

    private val n1 = Node(
        id = NodeId("n1"),
        position = Offset.Zero,
        width = 100f,
        height = 80f,
        ports = listOf(
            Port(PortId("top"),    PortSide.Top),
            Port(PortId("right"),  PortSide.Right),
            Port(PortId("bottom"), PortSide.Bottom),
            Port(PortId("left"),   PortSide.Left),
        ),
    )

    private val n2 = Node(
        id = NodeId("n2"),
        position = Offset(200f, 0f),
        width = 100f,
        height = 80f,
        ports = listOf(
            Port(PortId("top"),    PortSide.Top),
            Port(PortId("right"),  PortSide.Right),
            Port(PortId("bottom"), PortSide.Bottom),
            Port(PortId("left"),   PortSide.Left),
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
    fun `any port to any other port on different node is valid`() {
        PortSide.entries.forEach { fromSide ->
            PortSide.entries.forEach { toSide ->
                val from = EndPoint(NodeId("n1"), PortId(fromSide.name.lowercase()))
                val to   = EndPoint(NodeId("n2"), PortId(toSide.name.lowercase()))
                assertNull(validator.validate(from, to, emptyList(), portLookup),
                    "Expected $fromSide→$toSide to be valid")
            }
        }
    }

    @Test
    fun `self_loop same endpoint rejected`() {
        val ep = EndPoint(NodeId("n1"), PortId("right"))
        val err = validator.validate(ep, ep, emptyList(), portLookup)
        assertEquals(ConnectionError.SelfLoop(ep), err)
    }

    @Test
    fun `same node different ports allowed`() {
        val from = EndPoint(NodeId("n1"), PortId("right"))
        val to   = EndPoint(NodeId("n1"), PortId("left"))
        assertNull(validator.validate(from, to, emptyList(), portLookup))
    }

    @Test
    fun `missing from port returns PortNotFound`() {
        val from = EndPoint(NodeId("nX"), PortId("right"))
        val to   = EndPoint(NodeId("n2"), PortId("left"))
        assertEquals(ConnectionError.PortNotFound(from),
            validator.validate(from, to, emptyList(), portLookup))
    }

    @Test
    fun `missing to port returns PortNotFound`() {
        val from = EndPoint(NodeId("n1"), PortId("right"))
        val to   = EndPoint(NodeId("n2"), PortId("nope"))
        assertEquals(ConnectionError.PortNotFound(to),
            validator.validate(from, to, emptyList(), portLookup))
    }

    @Test
    fun `duplicate edge rejected`() {
        val from = EndPoint(NodeId("n1"), PortId("right"))
        val to   = EndPoint(NodeId("n2"), PortId("left"))
        val existing = Edge(EdgeId("e1"), from, to)
        assertEquals(ConnectionError.DuplicateEdge(EdgeId("e1")),
            validator.validate(from, to, listOf(existing), portLookup))
    }

    @Test
    fun `reverse of existing edge is not duplicate`() {
        val a = EndPoint(NodeId("n1"), PortId("right"))
        val b = EndPoint(NodeId("n2"), PortId("left"))
        val reverse = Edge(EdgeId("e1"), b, a)
        assertNull(validator.validate(a, b, listOf(reverse), portLookup))
    }
}
