package com.yasincidem.blockcanvas.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals

class GridMathTest {

    @Test
    fun `calculateVisibleGridCells returns correct range for default viewport`() {
        val viewport = Viewport.Default // pan=0, zoom=1
        val spacing = 20f
        val width = 100f
        val height = 100f

        val range = calculateVisibleGridCells(viewport, spacing, width, height)

        // topLeft world = (0,0) -> cell (0,0)
        // bottomRight world = (100,100) -> cell (5,5)
        assertEquals(0, range.startX)
        assertEquals(5, range.endX)
        assertEquals(0, range.startY)
        assertEquals(5, range.endY)
    }

    @Test
    fun `calculateVisibleGridCells returns correct range for panned viewport`() {
        val viewport = Viewport(pan = Offset(-10f, -10f), zoom = 1f)
        val spacing = 20f
        val width = 100f
        val height = 100f

        val range = calculateVisibleGridCells(viewport, spacing, width, height)

        // screen (0,0) -> world (10,10) -> cell 0.5 -> floor=0
        // screen (100,100) -> world (110,110) -> cell 5.5 -> ceil=6
        assertEquals(0, range.startX)
        assertEquals(6, range.endX)
        assertEquals(0, range.startY)
        assertEquals(6, range.endY)
    }

    @Test
    fun `calculateVisibleGridCells returns correct range for zoomed viewport`() {
        val viewport = Viewport(pan = Offset(0f, 0f), zoom = 2f)
        val spacing = 20f
        val width = 100f
        val height = 100f

        val range = calculateVisibleGridCells(viewport, spacing, width, height)

        // screen (0,0) -> world (0,0) -> cell 0
        // screen (100,100) -> world (50,50) -> cell 2.5 -> ceil=3
        assertEquals(0, range.startX)
        assertEquals(3, range.endX)
        assertEquals(0, range.startY)
        assertEquals(3, range.endY)
    }

    @Test
    fun `calculateVisibleGridCells handles negative coordinates`() {
        val viewport = Viewport(pan = Offset(100f, 100f), zoom = 1f)
        val spacing = 20f
        val width = 100f
        val height = 100f

        val range = calculateVisibleGridCells(viewport, spacing, width, height)

        // screen (0,0) -> world (-100,-100) -> cell -5
        // screen (100,100) -> world (0,0) -> cell 0
        assertEquals(-5, range.startX)
        assertEquals(0, range.endX)
        assertEquals(-5, range.startY)
        assertEquals(0, range.endY)
    }
}
