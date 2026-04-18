package com.yasincidem.blockcanvas.core.builder

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CanvasDSLTest {

    @Test
    fun `should build a simple canvas state with two nodes and one edge`() {
        val state = buildCanvasState {
            node("n1") {
                at(10f, 20f)
                size(100f, 50f)
                port("p1", PortSide.Right)
            }
            node("n2") {
                at(150f, 20f)
                size(100f, 50f)
                port("p2", PortSide.Left)
            }
            
            connect("n1", "p1") linksTo connect("n2", "p2")
        }

        assertEquals(2, state.nodes.size)
        assertEquals(1, state.edges.size)
        
        val n1 = state.nodes.values.find { it.id.value == "n1" }!!
        assertEquals(Offset(10f, 20f), n1.position)
        assertEquals(100f, n1.width)
        
        val edge = state.edges.values.first()
        assertEquals("n1", edge.from.node.value)
        assertEquals("p1", edge.from.port.value)
        assertEquals("n2", edge.to.node.value)
        assertEquals("p2", edge.to.port.value)
    }

    @Test
    fun `linksTo with style block sets edge decoration`() {
        val state = buildCanvasState {
            node("a") { at(0f, 0f); port("out", PortSide.Right) }
            node("b") { at(200f, 0f); port("in", PortSide.Left) }

            connect("a", "out") linksTo connect("b", "in") style {
                stroke = EdgeStroke.Dashed(dashLength = 10f, gapLength = 5f)
                targetEnd = EdgeEnd.Arrow(size = 12f)
                animation = EdgeAnimation.MarchingAnts(speedDpPerSecond = 60f)
            }
        }

        val edge = state.edges.values.single()
        assertTrue(edge.stroke is EdgeStroke.Dashed)
        assertTrue(edge.targetEnd is EdgeEnd.Arrow)
        assertTrue(edge.animation is EdgeAnimation.MarchingAnts)
        assertNull(edge.sourceEnd)
    }

    @Test
    fun `edge() builder function sets decoration`() {
        val state = buildCanvasState {
            node("a") { at(0f, 0f); port("out", PortSide.Right) }
            node("b") { at(200f, 0f); port("in", PortSide.Left) }

            edge(
                from = EndPoint(NodeId("a"), PortId("out")),
                to = EndPoint(NodeId("b"), PortId("in")),
            ) {
                stroke = EdgeStroke.Dotted(width = 3f)
                targetEnd = EdgeEnd.Circle(radius = 6f)
            }
        }

        val edge = state.edges.values.single()
        assertTrue(edge.stroke is EdgeStroke.Dotted)
        assertTrue(edge.targetEnd is EdgeEnd.Circle)
    }

    @Test
    fun `linksTo without style has null decoration fields`() {
        val state = buildCanvasState {
            node("a") { at(0f, 0f); port("out", PortSide.Right) }
            node("b") { at(200f, 0f); port("in", PortSide.Left) }
            connect("a", "out") linksTo connect("b", "in")
        }

        val edge = state.edges.values.single()
        assertNull(edge.stroke)
        assertNull(edge.targetEnd)
        assertNull(edge.sourceEnd)
        assertNull(edge.animation)
    }

    @Test
    fun `should support relative positioning`() {
        val state = buildCanvasState {
            val node1 = node("n1") {
                at(100f, 100f)
                size(150f, 80f)
            }
            
            node("n2") {
                rightOf("n1", gap = 50f)
                size(150f, 80f)
            }
            
            node("n3") {
                below("n1", gap = 40f)
                size(150f, 80f)
            }
        }

        val n1 = state.nodes.values.find { it.id.value == "n1" }!!
        val n2 = state.nodes.values.find { it.id.value == "n2" }!!
        val n3 = state.nodes.values.find { it.id.value == "n3" }!!

        // n2 = n1.x (100) + n1.width (150) + gap (50) = 300
        assertEquals(300f, n2.position.x)
        assertEquals(100f, n2.position.y)

        // n3 = n1.x (100), n1.y (100) + n1.height (80) + gap (40) = 220
        assertEquals(100f, n3.position.x)
        assertEquals(220f, n3.position.y)
    }
}
