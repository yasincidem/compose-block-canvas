package com.yasincidem.blockcanvas.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RectTest {

    private val unitSquare = Rect(left = 0f, top = 0f, right = 10f, bottom = 20f)

    @Test
    fun width_is_right_minus_left() {
        assertEquals(10f, unitSquare.width)
    }

    @Test
    fun height_is_bottom_minus_top() {
        assertEquals(20f, unitSquare.height)
    }

    @Test
    fun contains_point_strictly_inside_returns_true() {
        assertTrue(unitSquare.contains(Offset(5f, 10f)))
    }

    @Test
    fun contains_point_on_left_edge_is_inclusive() {
        assertTrue(unitSquare.contains(Offset(0f, 10f)))
    }

    @Test
    fun contains_point_on_top_edge_is_inclusive() {
        assertTrue(unitSquare.contains(Offset(5f, 0f)))
    }

    @Test
    fun contains_point_on_right_edge_is_exclusive() {
        assertFalse(unitSquare.contains(Offset(10f, 10f)))
    }

    @Test
    fun contains_point_on_bottom_edge_is_exclusive() {
        assertFalse(unitSquare.contains(Offset(5f, 20f)))
    }

    @Test
    fun contains_point_outside_returns_false() {
        assertFalse(unitSquare.contains(Offset(-1f, 10f)))
        assertFalse(unitSquare.contains(Offset(5f, -1f)))
        assertFalse(unitSquare.contains(Offset(11f, 10f)))
        assertFalse(unitSquare.contains(Offset(5f, 21f)))
    }

    @Test
    fun zero_is_a_degenerate_rect_at_origin() {
        assertEquals(0f, Rect.Zero.width)
        assertEquals(0f, Rect.Zero.height)
        assertFalse(Rect.Zero.contains(Offset.Zero))
    }

    @Test
    fun constructor_rejects_negative_width() {
        assertFailsWith<IllegalArgumentException> {
            Rect(left = 10f, top = 0f, right = 0f, bottom = 10f)
        }
    }

    @Test
    fun constructor_rejects_negative_height() {
        assertFailsWith<IllegalArgumentException> {
            Rect(left = 0f, top = 10f, right = 10f, bottom = 0f)
        }
    }
}
