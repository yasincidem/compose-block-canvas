package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoomControlsTest {

    private fun stateAt(zoom: Float, min: Float = 0.2f, max: Float = 2f) = BlockCanvasState(
        initialViewport = Viewport(zoom = zoom, minZoom = min, maxZoom = max)
    )

    @Test
    fun `zoomIn increases zoom by step`() {
        val state = stateAt(zoom = 1f)
        state.zoomIn(anchor = Offset.Zero)
        assertTrue(state.viewport.zoom > 1f)
    }

    @Test
    fun `zoomOut decreases zoom by step`() {
        val state = stateAt(zoom = 1f)
        state.zoomOut(anchor = Offset.Zero)
        assertTrue(state.viewport.zoom < 1f)
    }

    @Test
    fun `zoomIn is clamped at maxZoom`() {
        val state = stateAt(zoom = 2f, max = 2f)
        state.zoomIn(anchor = Offset.Zero)
        assertEquals(2f, state.viewport.zoom)
    }

    @Test
    fun `zoomOut is clamped at minZoom`() {
        val state = stateAt(zoom = 0.2f, min = 0.2f)
        state.zoomOut(anchor = Offset.Zero)
        assertEquals(0.2f, state.viewport.zoom)
    }

    @Test
    fun `zoomIn step is configurable`() {
        val state = stateAt(zoom = 1f)
        state.zoomIn(anchor = Offset.Zero, step = 0.5f)
        assertEquals(1.5f, state.viewport.zoom, absoluteTolerance = 0.001f)
    }

    @Test
    fun `zoomOut step is configurable`() {
        val state = stateAt(zoom = 1f)
        state.zoomOut(anchor = Offset.Zero, step = 0.5f)
        assertEquals(0.5f, state.viewport.zoom, absoluteTolerance = 0.001f)
    }
}
