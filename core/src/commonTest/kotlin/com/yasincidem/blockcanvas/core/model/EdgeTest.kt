package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EdgeTest {

    private val a = EndPoint(NodeId("a"), PortId("out0"))
    private val b = EndPoint(NodeId("b"), PortId("in0"))

    @Test
    fun endpoint_preserves_node_and_port_references() {
        assertEquals(NodeId("a"), a.node)
        assertEquals(PortId("out0"), a.port)
    }

    @Test
    fun endpoints_with_same_components_are_equal() {
        assertEquals(EndPoint(NodeId("a"), PortId("out0")), a)
    }

    @Test
    fun edge_preserves_id_and_endpoints() {
        val edge = Edge(id = EdgeId("e1"), from = a, to = b)
        assertEquals(EdgeId("e1"), edge.id)
        assertEquals(a, edge.from)
        assertEquals(b, edge.to)
    }

    @Test
    fun edges_are_directed_so_swapping_endpoints_produces_a_different_edge() {
        val forward = Edge(EdgeId("e1"), a, b)
        val reverse = Edge(EdgeId("e1"), b, a)
        assertNotEquals(forward, reverse)
    }
}
