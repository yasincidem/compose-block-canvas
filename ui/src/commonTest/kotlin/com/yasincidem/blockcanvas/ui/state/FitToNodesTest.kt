package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FitToNodesTest {

    private fun node(id: String, x: Float, y: Float, w: Float = 100f, h: Float = 100f) = Node(
        id = NodeId(id),
        position = Offset(x, y),
        width = w,
        height = h,
        ports = emptyList(),
    )

    @Test
    fun `fitToNodes does nothing when canvas is empty`() {
        val state = BlockCanvasState()
        val before = state.viewport
        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f)
        assertEquals(before, state.viewport)
    }

    @Test
    fun `fitToNodes centers a single node`() {
        val state = BlockCanvasState()
        state.addNode(node("n1", x = 0f, y = 0f, w = 100f, h = 100f))

        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f)

        // Node center should map to screen center after fit
        val nodeCenter = Offset(50f, 50f)
        val screenCenter = state.viewport.worldToScreen(nodeCenter)
        assertEquals(400f, screenCenter.x, absoluteTolerance = 1f)
        assertEquals(300f, screenCenter.y, absoluteTolerance = 1f)
    }

    @Test
    fun `fitToNodes zoom is clamped to maxZoom`() {
        val state = BlockCanvasState(
            initialViewport = Viewport(minZoom = 0.2f, maxZoom = 2f)
        )
        // Tiny node on huge canvas → computed zoom would exceed max
        state.addNode(node("n1", x = 0f, y = 0f, w = 10f, h = 10f))
        state.fitToNodes(canvasWidth = 8000f, canvasHeight = 6000f)
        assertTrue(state.viewport.zoom <= 2f)
    }

    @Test
    fun `fitToNodes zoom is clamped to minZoom`() {
        val state = BlockCanvasState(
            initialViewport = Viewport(minZoom = 0.2f, maxZoom = 2f)
        )
        // Giant nodes → computed zoom would go below min
        state.addNode(node("n1", x = 0f, y = 0f, w = 50000f, h = 50000f))
        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f)
        assertTrue(state.viewport.zoom >= 0.2f)
    }

    @Test
    fun `fitToNodes respects padding`() {
        val state = BlockCanvasState()
        state.addNode(node("n1", x = 0f, y = 0f, w = 800f, h = 600f))

        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f, padding = 0f)
        val zoomNoPadding = state.viewport.zoom

        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f, padding = 40f)
        val zoomWithPadding = state.viewport.zoom

        assertTrue(zoomWithPadding < zoomNoPadding)
    }

    @Test
    fun `fitToNodes covers multiple nodes bounding box`() {
        val state = BlockCanvasState()
        state.addNode(node("n1", x = 0f,    y = 0f,    w = 100f, h = 100f))
        state.addNode(node("n2", x = 500f,  y = 400f,  w = 100f, h = 100f))

        state.fitToNodes(canvasWidth = 800f, canvasHeight = 600f)

        // Both node bounding box corners should be on screen after fit
        val topLeft     = state.viewport.worldToScreen(Offset(0f,   0f))
        val bottomRight = state.viewport.worldToScreen(Offset(600f, 500f))
        assertTrue(topLeft.x >= 0f)
        assertTrue(topLeft.y >= 0f)
        assertTrue(bottomRight.x <= 800f)
        assertTrue(bottomRight.y <= 600f)
    }
}
