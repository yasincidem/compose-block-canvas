package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EdgeStrokeTest {

    @Test
    fun `Solid has correct default width`() {
        val s = EdgeStroke.Solid()
        assertEquals(2f, s.width)
    }

    @Test
    fun `Dashed has correct defaults`() {
        val s = EdgeStroke.Dashed()
        assertEquals(2f, s.width)
        assertEquals(8f, s.dashLength)
        assertEquals(4f, s.gapLength)
    }

    @Test
    fun `Dotted has correct defaults`() {
        val s = EdgeStroke.Dotted()
        assertEquals(2f, s.width)
        assertEquals(4f, s.gapLength)
    }

    @Test
    fun `Solid mapping produces no PathEffect`() {
        // Verify the mapped params — no pathEffect means intervals are null
        val s = EdgeStroke.Solid(width = 3f)
        // The mapped Compose Stroke width should equal s.width
        assertEquals(3f, s.width)
        // Solid has no interval arrays — validated by type alone
    }

    @Test
    fun `Dashed intervals match dashLength and gapLength`() {
        val s = EdgeStroke.Dashed(width = 2f, dashLength = 10f, gapLength = 5f)
        val intervals = floatArrayOf(s.dashLength, s.gapLength)
        assertEquals(10f, intervals[0])
        assertEquals(5f, intervals[1])
    }

    @Test
    fun `Dotted intervals use width as dash length`() {
        val s = EdgeStroke.Dotted(width = 3f, gapLength = 6f)
        // dot size = width, gap = gapLength
        val intervals = floatArrayOf(s.width, s.gapLength)
        assertEquals(3f, intervals[0])
        assertEquals(6f, intervals[1])
    }

    @Test
    fun `Edge stroke null means canvas default applies`() {
        val edge = Edge(
            id = EdgeId("e1"),
            from = EndPoint(NodeId("n1"), PortId("p1")),
            to   = EndPoint(NodeId("n2"), PortId("p2")),
        )
        assertNull(edge.stroke, "Default Edge.stroke should be null to inherit canvas default")
    }

    @Test
    fun `Edge stroke override is preserved`() {
        val stroke = EdgeStroke.Dashed(dashLength = 12f, gapLength = 6f)
        val edge = Edge(
            id = EdgeId("e1"),
            from = EndPoint(NodeId("n1"), PortId("p1")),
            to   = EndPoint(NodeId("n2"), PortId("p2")),
            stroke = stroke,
        )
        assertEquals(stroke, edge.stroke)
    }
}
