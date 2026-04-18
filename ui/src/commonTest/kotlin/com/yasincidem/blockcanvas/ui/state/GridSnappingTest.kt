package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.state.CanvasState
import kotlin.test.Test
import kotlin.test.assertEquals

class GridSnappingTest {

    @Test
    fun `snapping at zoom 1_0 without pan should snap to nearest multiple of spacing`() {
        val config = GridConfig(spacing = 20f, snapToGrid = true)
        val viewport = Viewport(pan = Offset(0f, 0f), zoom = 1.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config

        // 35.0 is closer to 40.0 than 20.0
        val snapped = state.snap(Offset(35f, 35f))
        assertEquals(Offset(40f, 40f), snapped)
        
        // 15.0 is closer to 20.0 than 0.0
        val snapped2 = state.snap(Offset(15f, 15f))
        assertEquals(Offset(20f, 20f), snapped2)
    }

    @Test
    fun `snapping at zoom 2_0 should snap to multiples of spacing divided by zoom`() {
        val config = GridConfig(spacing = 20f, snapToGrid = true) // visually 20px on screen
        val viewport = Viewport(pan = Offset(0f, 0f), zoom = 2.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config
        
        // At zoom 2.0, the grid dots are at 10 world units (20/2)
        // 15.0 is exactly on a dot (15 * 2 = 30, which is a multiple of 20 - wait, no)
        // 20 world units * 2.0 zoom = 40 screen pixels.
        // 10 world units * 2.0 zoom = 20 screen pixels.
        
        val snapped = state.snap(Offset(14f, 14f))
        assertEquals(Offset(10f, 10f), snapped)
        
        val snappedHalf = state.snap(Offset(16f, 16f))
        assertEquals(Offset(20f, 20f), snappedHalf)
    }

    @Test
    fun `snapping with pan should align with visible screen dots`() {
        val config = GridConfig(spacing = 20f, snapToGrid = true)
        // Pan of 5 means screen origin (0,0) is world (-5,0)? No.
        // ScreenPos = WorldPos * Zoom + Pan
        // If pan is 5.0, then World 0.0 is at Screen 5.0.
        // The first dot on screen is at 0.0 (if pan allows) or 20.0 etc.
        val viewport = Viewport(pan = Offset(5f, 5f), zoom = 1.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config
        
        // World pos 30.0 -> Screen pos 35.0
        // Nearest dot on screen is 40.0
        // Screen 40.0 -> World (40.0 - 5.0) / 1.0 = 35.0
        val snapped = state.snap(Offset(30f, 30f))
        assertEquals(Offset(35f, 35f), snapped)
    }
}
