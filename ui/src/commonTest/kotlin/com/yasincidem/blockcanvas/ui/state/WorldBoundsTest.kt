package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Rect
import com.yasincidem.blockcanvas.core.geometry.Viewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldBoundsTest {

    private val bounds = Rect(left = -500f, top = -500f, right = 500f, bottom = 500f)
    private val canvasW = 800f
    private val canvasH = 600f

    private fun stateWithBounds() = BlockCanvasState(worldBounds = bounds)

    @Test
    fun `pan is unconstrained when worldBounds is null`() {
        val state = BlockCanvasState(worldBounds = null)
        val farPan = Offset(-99999f, -99999f)
        state.updateViewport(state.viewport.copy(pan = farPan))
        assertEquals(farPan, state.viewport.pan)
    }

    @Test
    fun `updateViewport clamps pan so left world edge stays on screen`() {
        val state = stateWithBounds()
        // Try to pan so that the left edge of the world is far to the right of screen
        val newViewport = state.viewport.copy(
            pan = Offset(canvasW + 1000f, 0f),
            zoom = 1f,
        )
        state.updateViewport(newViewport, canvasWidth = canvasW, canvasHeight = canvasH)
        // Left world edge (x = -500) in screen space must be <= canvasW
        val leftEdgeScreen = state.viewport.worldToScreen(Offset(bounds.left, 0f)).x
        assertTrue(leftEdgeScreen <= canvasW, "Left edge must not be pushed beyond right of screen: $leftEdgeScreen")
    }

    @Test
    fun `updateViewport clamps pan so right world edge stays on screen`() {
        val state = stateWithBounds()
        val newViewport = state.viewport.copy(
            pan = Offset(-canvasW - 1000f, 0f),
            zoom = 1f,
        )
        state.updateViewport(newViewport, canvasWidth = canvasW, canvasHeight = canvasH)
        val rightEdgeScreen = state.viewport.worldToScreen(Offset(bounds.right, 0f)).x
        assertTrue(rightEdgeScreen >= 0f, "Right edge must not be pushed beyond left of screen: $rightEdgeScreen")
    }

    @Test
    fun `updateViewport clamps pan so top world edge stays on screen`() {
        val state = stateWithBounds()
        val newViewport = state.viewport.copy(
            pan = Offset(0f, canvasH + 1000f),
            zoom = 1f,
        )
        state.updateViewport(newViewport, canvasWidth = canvasW, canvasHeight = canvasH)
        val topEdgeScreen = state.viewport.worldToScreen(Offset(0f, bounds.top)).y
        assertTrue(topEdgeScreen <= canvasH, "Top edge must not be pushed beyond bottom of screen: $topEdgeScreen")
    }

    @Test
    fun `updateViewport clamps pan so bottom world edge stays on screen`() {
        val state = stateWithBounds()
        val newViewport = state.viewport.copy(
            pan = Offset(0f, -canvasH - 1000f),
            zoom = 1f,
        )
        state.updateViewport(newViewport, canvasWidth = canvasW, canvasHeight = canvasH)
        val bottomEdgeScreen = state.viewport.worldToScreen(Offset(0f, bounds.bottom)).y
        assertTrue(bottomEdgeScreen >= 0f, "Bottom edge must not be pushed beyond top of screen: $bottomEdgeScreen")
    }

    @Test
    fun `pan within bounds passes through unchanged`() {
        val state = stateWithBounds()
        // zoom=1, pan=0 → world center is at screen center, well within bounds
        val validViewport = state.viewport.copy(pan = Offset(0f, 0f), zoom = 1f)
        state.updateViewport(validViewport, canvasWidth = canvasW, canvasHeight = canvasH)
        assertEquals(Offset(0f, 0f), state.viewport.pan)
    }
}
