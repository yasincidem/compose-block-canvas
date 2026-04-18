package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals

class GridSnappingTest {

    @Test
    fun `snapping at zoom 1_0 without pan should snap to nearest multiple of spacing or half-spacing`() {
        val config = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = true)
        val viewport = Viewport(pan = Offset(0f, 0f), zoom = 1.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config

        val snapped = state.snap(Offset(35f, 35f))
        assertTrue("Snapped world X should be 30f or 40f", snapped.x == 30f || snapped.x == 40f)
    }

    @Test
    fun `snapping with pan should align with visible screen lines or mid-points`() {
        val config = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = true)
        val viewport = Viewport(pan = Offset(5f, 5f), zoom = 1.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config

        // World 30f -> Screen 35f, which is exactly a half-grid midpoint.
        val snapped = state.snap(Offset(30f, 30f))
        assertEquals(Offset(30f, 30f), snapped)
    }

    @Test
    fun `snapping should automatically allow half-grid alignment`() {
        val config = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = true)
        val viewport = Viewport(pan = Offset(0f, 0f), zoom = 1.0f)
        val state = BlockCanvasState(initialViewport = viewport)
        state.gridConfig = config

        // 14f -> nearest half-grid (step=10) is 10f
        val snapped = state.snap(Offset(14f, 14f))
        assertEquals(Offset(10f, 10f), snapped)

        // 6f -> nearest half-grid is 10f
        val snapped2 = state.snap(Offset(6f, 6f))
        assertEquals(Offset(10f, 10f), snapped2)

        // 1f -> nearest half-grid is 0f
        val snapped3 = state.snap(Offset(1f, 1f))
        assertEquals(Offset(0f, 0f), snapped3)
    }

    @Test
    fun `snap is idempotent - pre-snapping base prevents jump on first small delta`() {
        val config = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = true)
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        state.gridConfig = config

        val offGridPos = Offset(27f, 27f)
        val preSnapped = state.snap(offGridPos)

        val tinyDelta = Offset(0.3f, 0.3f)
        val result = state.snap(Offset(preSnapped.x + tinyDelta.x, preSnapped.y + tinyDelta.y))
        assertEquals(preSnapped, result)
    }

    @Test
    fun `snap disabled returns original position unchanged`() {
        val config = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = false)
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        state.gridConfig = config

        val pos = Offset(13f, 27f)
        assertEquals(pos, state.snap(pos))
    }

    @Test
    fun `snap after toggle off then on aligns to grid step`() {
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        val spacing = 20f
        val step = spacing / 2f

        state.gridConfig = GridConfig(style = GridStyle.Dots(spacing = spacing), snapToGrid = false)
        state.gridConfig = GridConfig(style = GridStyle.Dots(spacing = spacing), snapToGrid = true)

        val pos = Offset(27f, 22f)
        val snapped = state.snap(pos)

        assertTrue("x not on step grid: ${snapped.x}", kotlin.math.abs(snapped.x % step) < 0.001f)
        assertTrue("y not on step grid: ${snapped.y}", kotlin.math.abs(snapped.y % step) < 0.001f)
    }

    @Test
    fun `long drag across many grid cells produces no float drift`() {
        val spacing = 20f
        val step = spacing * 0.5f
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        state.gridConfig = GridConfig(style = GridStyle.Dots(spacing = spacing), snapToGrid = true)

        // Simulate landing just past 1000 half-steps — roundToInt must absorb the 0.3f noise
        val totalDelta = 1000f * step + 0.3f
        val snapped = state.snap(Offset(totalDelta, 0f))

        val remainder = snapped.x % step
        assertTrue(
            "x not on step grid after long drag: ${snapped.x}, remainder=$remainder",
            remainder < 0.001f || (step - remainder) < 0.001f,
        )
    }

    @Test
    fun `snapAllToGrid snaps all nodes to nearest grid cell`() {
        val spacing = 20f
        val step = spacing * 0.5f
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        state.gridConfig = GridConfig(style = GridStyle.Dots(spacing = spacing), snapToGrid = true)

        state.addNode(Node(id = NodeId("n1"), position = Offset(13f, 7f), width = 100f, height = 60f, ports = emptyList()))
        state.addNode(Node(id = NodeId("n2"), position = Offset(37f, 24f), width = 100f, height = 60f, ports = emptyList()))

        state.snapAllToGrid()

        for (node in state.canvasState.nodes.values) {
            val pos = state.nodePositions[node.id]!!
            val remX = pos.x % step
            val remY = pos.y % step
            assertTrue("node ${node.id} x not on grid: ${pos.x}", remX < 0.001f || (step - remX) < 0.001f)
            assertTrue("node ${node.id} y not on grid: ${pos.y}", remY < 0.001f || (step - remY) < 0.001f)
        }
    }

    @Test
    fun `snapAllToGrid is no-op when snap disabled`() {
        val state = BlockCanvasState(initialViewport = Viewport(pan = Offset(0f, 0f), zoom = 1f))
        state.gridConfig = GridConfig(style = GridStyle.Dots(spacing = 20f), snapToGrid = false)

        val n = Node(id = NodeId("n"), position = Offset(13f, 7f), width = 100f, height = 60f, ports = emptyList())
        state.addNode(n)
        state.snapAllToGrid()

        assertEquals(Offset(13f, 7f), state.nodePositions[n.id])
    }
}

fun assertTrue(message: String, condition: Boolean) {
    if (!condition) throw AssertionError(message)
}
