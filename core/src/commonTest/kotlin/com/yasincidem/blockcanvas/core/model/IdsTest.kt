package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class IdsTest {

    @Test
    fun node_id_exposes_its_raw_value() {
        assertEquals("input", NodeId("input").value)
    }

    @Test
    fun port_id_exposes_its_raw_value() {
        assertEquals("in0", PortId("in0").value)
    }

    @Test
    fun edge_id_exposes_its_raw_value() {
        assertEquals("e42", EdgeId("e42").value)
    }

    @Test
    fun node_ids_with_equal_values_are_equal() {
        assertEquals(NodeId("a"), NodeId("a"))
    }

    @Test
    fun node_ids_with_different_values_are_not_equal() {
        assertNotEquals(NodeId("a"), NodeId("b"))
    }

    @Test
    fun node_id_rejects_blank_value() {
        assertFailsWith<IllegalArgumentException> { NodeId("") }
    }

    @Test
    fun node_id_rejects_whitespace_only_value() {
        assertFailsWith<IllegalArgumentException> { NodeId("   ") }
    }

    @Test
    fun port_id_rejects_blank_value() {
        assertFailsWith<IllegalArgumentException> { PortId("") }
    }

    @Test
    fun edge_id_rejects_blank_value() {
        assertFailsWith<IllegalArgumentException> { EdgeId("") }
    }

    @Test
    fun id_types_can_be_used_as_map_keys_without_collision_with_plain_strings() {
        val byNode: Map<NodeId, Int> = mapOf(NodeId("k") to 1)
        val byPort: Map<PortId, Int> = mapOf(PortId("k") to 2)

        assertEquals(1, byNode[NodeId("k")])
        assertEquals(2, byPort[PortId("k")])
    }
}
