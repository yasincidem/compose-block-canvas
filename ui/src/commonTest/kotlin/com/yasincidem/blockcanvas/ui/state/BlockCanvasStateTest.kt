package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BlockCanvasStateTest {

    private val port1 = Port(PortId("p1"), PortSide.Right)
    
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

    @Test
    fun `history stack updates correctly`() {
        val state = BlockCanvasState()
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)

        state.addNode(node1)
        assertTrue(state.canUndo)
        assertFalse(state.canRedo)

        state.undo()
        assertFalse(state.canUndo)
        assertTrue(state.canRedo)
        assertTrue(state.canvasState.nodes.isEmpty())

        state.redo()
        assertTrue(state.canUndo)
        assertFalse(state.canRedo)
        assertEquals(1, state.canvasState.nodes.size)
    }

    @Test
    fun `bulk node movement creates exactly one undo step`() {
        val state = BlockCanvasState()
        state.addNode(node1)
        
        val node2 = node1.copy(id = NodeId("n2"))
        state.addNode(node2)
        
        // At this point undoStack has 2 items: [{}, {n1}]
        val baseUndoCount = if (state.canUndo) 2 else 0 
        
        val pos1 = Offset(100f, 100f)
        val pos2 = Offset(200f, 200f)
        
        state.commitNodePositions(mapOf(
            node1.id to pos1,
            node2.id to pos2
        ))
        
        assertEquals(pos1, state.canvasState.nodes[node1.id]?.position)
        assertEquals(pos2, state.canvasState.nodes[node2.id]?.position)
        
        state.undo()
        
        assertEquals(Offset.Zero, state.canvasState.nodes[node1.id]?.position)
        assertEquals(Offset.Zero, state.canvasState.nodes[node2.id]?.position)
        
        // After one undo, we should be back to the state with 2 nodes at (0,0)
        // stack should have the same number of items as before the bulk move
        assertTrue(state.canUndo)
        state.undo() // reverts addNode(node2)
        state.undo() // reverts addNode(node1)
        assertFalse(state.canUndo)
    }

    @Test
    fun `undoing node removal restores it and its positions in the map`() {
        val state = BlockCanvasState()
        state.addNode(node1)
        state.moveNode(node1.id, Offset(50f, 50f))
        
        state.removeNode(node1.id)
        assertFalse(state.canvasState.nodes.containsKey(node1.id))
        assertFalse(state.nodePositions.containsKey(node1.id))
        
        state.undo()
        assertTrue(state.canvasState.nodes.containsKey(node1.id))
        assertEquals(Offset(50f, 50f), state.canvasState.nodes[node1.id]?.position)
        assertEquals(Offset(50f, 50f), state.nodePositions[node1.id])
    }
}
