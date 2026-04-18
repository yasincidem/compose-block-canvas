package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomConstraintsTest {

    @Test
    fun `BlockCanvasState respects viewport zoom limits`() {
        val min = 0.5f
        val max = 2.0f
        val state = BlockCanvasState(
            initialViewport = Viewport(zoom = 1f, minZoom = min, maxZoom = max)
        )

        // Attempt to zoom in beyond max
        state.updateViewport(state.viewport.withZoom(3f, Offset.Zero))
        assertEquals(max, state.viewport.zoom)

        // Attempt to zoom out beyond min
        state.updateViewport(state.viewport.withZoom(0.1f, Offset.Zero))
        assertEquals(min, state.viewport.zoom)
    }

    @Test
    fun `BlockCanvasState respects GridConfig minThreshold`() {
        val customThreshold = 50f
        val state = BlockCanvasState(
            initialGridConfig = GridConfig(minThreshold = customThreshold)
        )
        assertEquals(customThreshold, state.gridConfig.minThreshold)
    }
}
