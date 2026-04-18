package com.yasincidem.blockcanvas.core.persistence

import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CanvasSerializationTest {

    @Test
    fun `should round-trip canvas state through JSON using coroutines`() = runTest {
        val original = buildCanvasState {
            node("n1") {
                at(10f, 20f)
                port("p1", PortSide.Right)
            }
            node("n2") {
                at(100f, 200f)
                port("p2", PortSide.Left)
            }
            connect("n1", "p1") linksTo connect("n2", "p2")
        }

        // Serialization
        val json = original.encodeToJson()
        
        // Deserialization
        val deserialized = json.decodeToCanvasState()

        assertEquals(original.nodes.size, deserialized.nodes.size)
        assertEquals(original.edges.size, deserialized.edges.size)
        
        val n1 = deserialized.nodes.values.find { it.id.value == "n1" }!!
        assertEquals(10f, n1.position.x)
    }
}
