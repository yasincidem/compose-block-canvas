package com.yasincidem.blockcanvas.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals

class OffsetTest {

    @Test
    fun zero_is_the_origin_point() {
        assertEquals(Offset(0f, 0f), Offset.Zero)
    }

    @Test
    fun plus_adds_components_pairwise() {
        assertEquals(Offset(4f, 6f), Offset(1f, 2f) + Offset(3f, 4f))
    }

    @Test
    fun minus_subtracts_components_pairwise() {
        assertEquals(Offset(4f, 5f), Offset(5f, 7f) - Offset(1f, 2f))
    }

    @Test
    fun distance_between_origin_and_3_4_point_is_5() {
        assertEquals(5f, Offset.Zero.distanceTo(Offset(3f, 4f)))
    }

    @Test
    fun distance_between_a_point_and_itself_is_zero() {
        val p = Offset(2f, 3f)
        assertEquals(0f, p.distanceTo(p))
    }

    @Test
    fun distance_is_symmetric() {
        val a = Offset(-2f, 5f)
        val b = Offset(7f, -1f)
        assertEquals(a.distanceTo(b), b.distanceTo(a))
    }

    @Test
    fun times_scales_both_components() {
        assertEquals(Offset(6f, 15f), Offset(2f, 5f) * 3f)
    }

    @Test
    fun times_by_zero_gives_zero_offset() {
        assertEquals(Offset.Zero, Offset(100f, 50f) * 0f)
    }

    @Test
    fun div_divides_both_components() {
        assertEquals(Offset(3f, 2f), Offset(6f, 4f) / 2f)
    }

    @Test
    fun times_then_div_round_trips() {
        val original = Offset(7f, -3f)
        assertEquals(original, original * 4f / 4f)
    }
}
