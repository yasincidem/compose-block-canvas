package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortDirection
import com.yasincidem.blockcanvas.core.model.PortId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BlockCanvasStateTest {

    private val port1 = Port(PortId("p1"), PortDirection.Out)
    
    private val node1 = Node(
        id = NodeId("n1"),
        position = Offset.Zero,
        width = 100f,
        height = 100f,
        ports = listOf(port1)
    )

    @Test
    fun `starts with empty state by default`() {
        val state = BlockCanvasState()
        assertTrue(state.canvasState.nodes.isEmpty())
        assertTrue(state.selectionState.selectedNodes.isEmpty())
    }

    @Test
    fun `addNode updates canvas state`() {
        val state = BlockCanvasState()
        state.addNode(node1)
        
        assertEquals(1, state.canvasState.nodes.size)
        assertEquals(node1, state.canvasState.nodes[node1.id])
    }

    @Test
    fun `selection toggles work properly`() {
        val state = BlockCanvasState()
        state.toggleSelection(node1.id)
        
        assertTrue(state.selectionState.isSelected(node1.id))
        
        state.toggleSelection(node1.id)
        assertFalse(state.selectionState.isSelected(node1.id))
    }

    @Test
    fun `default viewport is identity`() {
        val state = BlockCanvasState()
        assertEquals(Viewport.Default, state.viewport)
    }

    @Test
    fun `updateViewport replaces viewport`() {
        val state = BlockCanvasState()
        val newViewport = Viewport(pan = Offset(10f, 20f), zoom = 2f)
        state.updateViewport(newViewport)
        assertEquals(newViewport, state.viewport)
    }

    @Test
    fun `initialViewport is reflected on construction`() {
        val vp = Viewport(pan = Offset(50f, 50f), zoom = 0.5f)
        val state = BlockCanvasState(initialViewport = vp)
        assertEquals(vp, state.viewport)
    }

    @Test
    fun `removeNode cleans up selection state for node`() {
        val state = BlockCanvasState()
        state.addNode(node1)
        state.toggleSelection(node1.id)
        
        assertTrue(state.selectionState.isSelected(node1.id))
        
        state.removeNode(node1.id)
        
        assertTrue(state.canvasState.nodes.isEmpty())
        assertTrue(state.selectionState.selectedNodes.isEmpty(), "Selection was cleared on node removal")
    }
}
