package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PortTest {

    @Test
    fun port_preserves_id_and_direction() {
        val port = Port(id = PortId("in0"), direction = PortDirection.In)
        assertEquals(PortId("in0"), port.id)
        assertEquals(PortDirection.In, port.direction)
    }

    @Test
    fun ports_with_same_id_and_direction_are_equal() {
        assertEquals(
            Port(PortId("p"), PortDirection.Out),
            Port(PortId("p"), PortDirection.Out),
        )
    }

    @Test
    fun ports_with_different_direction_are_not_equal() {
        assertNotEquals(
            Port(PortId("p"), PortDirection.In),
            Port(PortId("p"), PortDirection.Out),
        )
    }
}
