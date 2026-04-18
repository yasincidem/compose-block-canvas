package com.yasincidem.blockcanvas.core.builder

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertEquals
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
