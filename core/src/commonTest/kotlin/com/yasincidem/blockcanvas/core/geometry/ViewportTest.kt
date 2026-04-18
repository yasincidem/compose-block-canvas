package com.yasincidem.blockcanvas.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val EPSILON = 0.001f

private fun assertNear(expected: Float, actual: Float, epsilon: Float = EPSILON) {
    assert(kotlin.math.abs(expected - actual) <= epsilon) {
        "Expected $expected ± $epsilon but was $actual"
    }
}

private fun assertNear(expected: Offset, actual: Offset, epsilon: Float = EPSILON) {
    assertNear(expected.x, actual.x, epsilon)
    assertNear(expected.y, actual.y, epsilon)
}

class ViewportTest {

    // ── defaults ──────────────────────────────────────────────────────────────

    @Test
    fun `default viewport has zero pan and unit zoom`() {
        val vp = Viewport()
        assertEquals(Offset.Zero, vp.pan)
        assertEquals(1f, vp.zoom)
    }

    @Test
    fun `reject zoom below minimum`() {
        assertFailsWith<IllegalArgumentException> {
            Viewport(zoom = 0f)
        }
    }

    @Test
    fun `reject zoom above maximum`() {
        assertFailsWith<IllegalArgumentException> {
            Viewport(zoom = Viewport.MAX_ZOOM + 1f)
        }
    }

    // ── worldToScreen ─────────────────────────────────────────────────────────

    @Test
    fun `worldToScreen is identity at default viewport`() {
        val vp = Viewport()
        val world = Offset(100f, 200f)
        assertEquals(world, vp.worldToScreen(world))
    }

    @Test
    fun `worldToScreen applies pan`() {
        val vp = Viewport(pan = Offset(50f, -30f))
        val result = vp.worldToScreen(Offset(0f, 0f))
        assertNear(Offset(50f, -30f), result)
    }

    @Test
    fun `worldToScreen applies zoom`() {
        val vp = Viewport(zoom = 2f)
        val result = vp.worldToScreen(Offset(10f, 5f))
        assertNear(Offset(20f, 10f), result)
    }

    @Test
    fun `worldToScreen applies zoom then pan`() {
        val vp = Viewport(pan = Offset(100f, 100f), zoom = 2f)
        val result = vp.worldToScreen(Offset(10f, 10f))
        // (10 * 2 + 100, 10 * 2 + 100) = (120, 120)
        assertNear(Offset(120f, 120f), result)
    }

    // ── screenToWorld ─────────────────────────────────────────────────────────

    @Test
    fun `screenToWorld is identity at default viewport`() {
        val vp = Viewport()
        val screen = Offset(100f, 200f)
        assertEquals(screen, vp.screenToWorld(screen))
    }

    @Test
    fun `screenToWorld reverses pan`() {
        val vp = Viewport(pan = Offset(50f, -30f))
        val result = vp.screenToWorld(Offset(50f, -30f))
        assertNear(Offset.Zero, result)
    }

    @Test
    fun `screenToWorld reverses zoom`() {
        val vp = Viewport(zoom = 2f)
        val result = vp.screenToWorld(Offset(20f, 10f))
        assertNear(Offset(10f, 5f), result)
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `round-trip worldToScreen then screenToWorld returns original`() {
        val vp = Viewport(pan = Offset(123f, -45f), zoom = 1.5f)
        val original = Offset(77f, -33f)
        assertNear(original, vp.screenToWorld(vp.worldToScreen(original)))
    }

    @Test
    fun `round-trip screenToWorld then worldToScreen returns original`() {
        val vp = Viewport(pan = Offset(-200f, 80f), zoom = 0.5f)
        val original = Offset(320f, 240f)
        assertNear(original, vp.worldToScreen(vp.screenToWorld(original)))
    }

    // ── withPan ───────────────────────────────────────────────────────────────

    @Test
    fun `withPan returns new viewport with updated pan`() {
        val vp = Viewport()
        val moved = vp.withPan(Offset(10f, 20f))
        assertEquals(Offset(10f, 20f), moved.pan)
        assertEquals(vp.zoom, moved.zoom)
    }

    @Test
    fun `withPan does not mutate original`() {
        val vp = Viewport()
        vp.withPan(Offset(10f, 20f))
        assertEquals(Offset.Zero, vp.pan)
    }

    // ── withZoom ──────────────────────────────────────────────────────────────

    @Test
    fun `withZoom clamps to minimum`() {
        val vp = Viewport()
        val clamped = vp.withZoom(0f, anchor = Offset.Zero)
        assertEquals(Viewport.MIN_ZOOM, clamped.zoom)
    }

    @Test
    fun `withZoom clamps to maximum`() {
        val vp = Viewport()
        val clamped = vp.withZoom(Viewport.MAX_ZOOM + 10f, anchor = Offset.Zero)
        assertEquals(Viewport.MAX_ZOOM, clamped.zoom)
    }

    @Test
    fun `withZoom keeps anchor fixed in screen space`() {
        val vp = Viewport(pan = Offset.Zero, zoom = 1f)
        val anchor = Offset(300f, 200f)
        val zoomed = vp.withZoom(2f, anchor)

        // The world point under the anchor before zoom should still map to the same screen position after zoom.
        val worldAtAnchorBefore = vp.screenToWorld(anchor)
        val screenAfter = zoomed.worldToScreen(worldAtAnchorBefore)
        assertNear(anchor, screenAfter)
    }

    @Test
    fun `withZoom anchor invariant holds at arbitrary pan and zoom`() {
        val vp = Viewport(pan = Offset(150f, -60f), zoom = 1.5f)
        val anchor = Offset(400f, 300f)
        val zoomed = vp.withZoom(3f, anchor)

        val worldAtAnchorBefore = vp.screenToWorld(anchor)
        val screenAfter = zoomed.worldToScreen(worldAtAnchorBefore)
        assertNear(anchor, screenAfter)
    }

    @Test
    fun `withZoom does not mutate original`() {
        val vp = Viewport()
        vp.withZoom(2f, Offset.Zero)
        assertEquals(1f, vp.zoom)
    }
}
