package com.yasincidem.blockcanvas.core.alignment

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlignmentDetectorTest {

    private fun node(id: String, x: Float, y: Float, w: Float = 100f, h: Float = 60f) = Node(
        id = NodeId(id),
        position = Offset(x, y),
        width = w,
        height = h,
        ports = emptyList(),
    )

    @Test
    fun `same left edge produces a vertical guide at that x`() {
        // Different widths so only the left edges align
        val dragged = node("d", x = 0f, y = 200f, w = 80f)
        val other   = node("a", x = 0f, y = 0f,   w = 120f)
        val result = AlignmentDetector.detect(dragged, listOf(dragged, other), threshold = 6f)

        val verticals = result.guides.filter { it.axis == Axis.Vertical }
        assertTrue(verticals.any { it.position == 0f && it.edge == EdgeAlignment.Start })
    }

    @Test
    fun `beyond threshold produces no guide`() {
        val dragged = node("d", x = 7f, y = 200f)   // left = 7, other left = 0 → diff 7 > threshold 6
        val other   = node("a", x = 0f, y = 0f)
        val result = AlignmentDetector.detect(dragged, listOf(dragged, other), threshold = 6f)

        assertTrue(result.guides.isEmpty())
        assertEquals(Offset.Zero, result.snapOffset)
    }

    @Test
    fun `snap offset is zero when already exactly aligned`() {
        val dragged = node("d", x = 0f, y = 200f)
        val other   = node("a", x = 0f, y = 0f)
        val result = AlignmentDetector.detect(dragged, listOf(dragged, other), threshold = 6f)

        assertEquals(0f, result.snapOffset.x)
    }

    @Test
    fun `snap offset pulls dragged onto aligned position`() {
        // dragged left = 4, other left = 0 → within threshold 6, snapDx = -4
        val dragged = node("d", x = 4f, y = 200f)
        val other   = node("a", x = 0f, y = 0f)
        val result = AlignmentDetector.detect(dragged, listOf(dragged, other), threshold = 6f)

        assertEquals(-4f, result.snapOffset.x)
    }

    @Test
    fun `three nodes centered produces guide and distance labels`() {
        // node A at x=0..100, node B at x=200..300 — dragged centered at x=100..200
        val nodeA   = node("a", x = 0f,   y = 0f)
        val nodeB   = node("b", x = 200f, y = 0f)
        val dragged = node("d", x = 100f, y = 0f)   // centerX = 150, A centerX=50, B centerX=250

        // Align dragged left (100) with A right (100) → diff=0 → guide
        val result = AlignmentDetector.detect(dragged, listOf(dragged, nodeA, nodeB), threshold = 6f)

        val verticals = result.guides.filter { it.axis == Axis.Vertical }
        assertTrue(verticals.isNotEmpty())
    }

    @Test
    fun `horizontal alignment detected for same top edge`() {
        val dragged = node("d", x = 200f, y = 0f)
        val other   = node("a", x = 0f,   y = 0f)
        val result = AlignmentDetector.detect(dragged, listOf(dragged, other), threshold = 6f)

        val horizontals = result.guides.filter { it.axis == Axis.Horizontal }
        assertTrue(horizontals.any { it.position == 0f && it.edge == EdgeAlignment.Start })
    }

    @Test
    fun `dragged node is excluded from others comparison`() {
        val dragged = node("d", x = 0f, y = 0f)
        // Only the dragged node in the list — no guides should appear
        val result = AlignmentDetector.detect(dragged, listOf(dragged), threshold = 6f)

        assertTrue(result.guides.isEmpty())
        assertEquals(Offset.Zero, result.snapOffset)
    }
}
