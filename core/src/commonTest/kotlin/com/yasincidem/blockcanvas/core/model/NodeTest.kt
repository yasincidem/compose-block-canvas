package com.yasincidem.blockcanvas.core.model

import com.yasincidem.blockcanvas.core.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NodeTest {

    private val samplePorts = listOf(
        Port(PortId("left"),  PortSide.Left),
        Port(PortId("right"), PortSide.Right),
    )

    @Test
    fun node_preserves_all_components() {
        val node = Node(
            id = NodeId("n1"),
            position = Offset(10f, 20f),
            width = 120f,
            height = 60f,
            ports = samplePorts,
        )
        assertEquals(NodeId("n1"), node.id)
        assertEquals(Offset(10f, 20f), node.position)
        assertEquals(120f, node.width)
        assertEquals(60f, node.height)
        assertEquals(samplePorts, node.ports)
    }

    @Test
    fun node_allows_empty_ports_list() {
        val node = Node(NodeId("n1"), Offset.Zero, 10f, 10f, emptyList())
        assertTrue(node.ports.isEmpty())
    }

    @Test
    fun node_rejects_negative_width() {
        assertFailsWith<IllegalArgumentException> {
            Node(NodeId("n"), Offset.Zero, width = -1f, height = 10f, ports = emptyList())
        }
    }

    @Test
    fun node_rejects_negative_height() {
        assertFailsWith<IllegalArgumentException> {
            Node(NodeId("n"), Offset.Zero, width = 10f, height = -1f, ports = emptyList())
        }
    }

    @Test
    fun node_rejects_duplicate_port_ids() {
        val dup = listOf(
            Port(PortId("p"), PortSide.Left),
            Port(PortId("p"), PortSide.Right),
        )
        assertFailsWith<IllegalArgumentException> {
            Node(NodeId("n"), Offset.Zero, 10f, 10f, dup)
        }
    }
}
