package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PortTest {

    @Test
    fun port_preserves_id_and_side() {
        val port = Port(id = PortId("p1"), side = PortSide.Left)
        assertEquals(PortId("p1"), port.id)
        assertEquals(PortSide.Left, port.side)
    }

    @Test
    fun ports_with_same_id_and_side_are_equal() {
        assertEquals(Port(PortId("p"), PortSide.Right), Port(PortId("p"), PortSide.Right))
    }

    @Test
    fun ports_with_different_sides_are_not_equal() {
        assertNotEquals(Port(PortId("p"), PortSide.Top), Port(PortId("p"), PortSide.Bottom))
    }
}
