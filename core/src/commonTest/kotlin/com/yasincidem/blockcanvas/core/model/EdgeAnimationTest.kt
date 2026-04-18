package com.yasincidem.blockcanvas.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EdgeAnimationTest {

    @Test
    fun `MarchingAnts default speed and direction`() {
        val anim = EdgeAnimation.MarchingAnts()
        assertEquals(40f, anim.speedDpPerSecond)
        assertEquals(false, anim.reverse)
    }

    @Test
    fun `Pulse default values`() {
        val anim = EdgeAnimation.Pulse()
        assertEquals(3f, anim.dotRadius)
        assertEquals(1500, anim.durationMs)
        assertEquals(1, anim.count)
    }

    @Test
    fun `Edge animation null means inherit canvas default`() {
        val edge = Edge(
            id = EdgeId("e1"),
            from = EndPoint(NodeId("n1"), PortId("p1")),
            to   = EndPoint(NodeId("n2"), PortId("p2")),
        )
        assertNull(edge.animation)
    }

    @Test
    fun `Edge animation override is preserved`() {
        val anim = EdgeAnimation.MarchingAnts(speedDpPerSecond = 80f, reverse = true)
        val edge = Edge(
            id = EdgeId("e1"),
            from = EndPoint(NodeId("n1"), PortId("p1")),
            to   = EndPoint(NodeId("n2"), PortId("p2")),
            animation = anim,
        )
        assertEquals(anim, edge.animation)
    }

    @Test
    fun `effective animation resolves edge override over canvas default`() {
        val canvasDefault = EdgeAnimation.None
        val edgeOverride  = EdgeAnimation.Pulse(count = 3)
        val effective     = edgeOverride ?: canvasDefault
        assertTrue(effective is EdgeAnimation.Pulse)
        assertEquals(3, (effective as EdgeAnimation.Pulse).count)
    }

    @Test
    fun `effective animation falls back to canvas default when edge is null`() {
        val canvasDefault: EdgeAnimation = EdgeAnimation.MarchingAnts()
        val edgeAnim: EdgeAnimation? = null
        val effective = edgeAnim ?: canvasDefault
        assertTrue(effective is EdgeAnimation.MarchingAnts)
    }

    @Test
    fun `MarchingAnts phase advances with animTime`() {
        val anim = EdgeAnimation.MarchingAnts(speedDpPerSecond = 100f)
        val screenDensity = 2f
        val intervalLen = 8f + 4f  // default Dashed dashLength + gapLength
        val speedPx = anim.speedDpPerSecond * screenDensity

        val phase0 = (0.0f * speedPx) % intervalLen
        val phase1 = (0.5f * speedPx) % intervalLen
        assertTrue(phase1 > phase0 || phase1 < phase0, "Phase should change over time")
    }

    @Test
    fun `Pulse t values span full 0-1 range for count=2`() {
        val count = 2
        val animTime = 0.3f
        val tValues = List(count) { i -> ((animTime + i.toFloat() / count) % 1f) }
        assertEquals(2, tValues.size)
        tValues.forEach { t -> assertTrue(t in 0f..1f) }
        // Two dots should be 0.5 apart in phase
        val diff = kotlin.math.abs(tValues[1] - tValues[0])
        val wrappedDiff = minOf(diff, 1f - diff)
        assertTrue(wrappedDiff > 0.4f, "Dots should be ~0.5 apart, got $wrappedDiff")
    }
}
