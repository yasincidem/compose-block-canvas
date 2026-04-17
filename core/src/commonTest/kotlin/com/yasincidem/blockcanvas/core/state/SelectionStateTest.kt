package com.yasincidem.blockcanvas.core.state

import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectionStateTest {

    private val nodeId1 = NodeId("n1")
    private val nodeId2 = NodeId("n2")
    private val edgeId1 = EdgeId("e1")
    private val edgeId2 = EdgeId("e2")

    @Test
    fun `starts empty`() {
        val state = SelectionState()
        assertTrue(state.selectedNodes.isEmpty())
        assertTrue(state.selectedEdges.isEmpty())
    }

    @Test
    fun `returns true for selected elements`() {
        val state = SelectionState()
            .add(nodeId1)
            .add(edgeId1)

        assertTrue(state.isSelected(nodeId1))
        assertTrue(state.isSelected(edgeId1))
    }

    @Test
    fun `selectOnly clears other selections`() {
        var state = SelectionState()
            .add(nodeId1)
            .add(nodeId2)
            .add(edgeId1)

        state = state.selectOnly(nodeId1)

        assertEquals(setOf(nodeId1), state.selectedNodes)
        assertTrue(state.selectedEdges.isEmpty())

        state = state.selectOnly(edgeId2)

        assertTrue(state.selectedNodes.isEmpty())
        assertEquals(setOf(edgeId2), state.selectedEdges)
    }

    @Test
    fun `toggle adds when not selected and removes when selected`() {
        var state = SelectionState().toggle(nodeId1)
        assertTrue(state.isSelected(nodeId1))

        state = state.toggle(nodeId1)
        assertTrue(!state.isSelected(nodeId1))

        state = state.toggle(edgeId1)
        assertTrue(state.isSelected(edgeId1))

        state = state.toggle(edgeId1)
        assertTrue(!state.isSelected(edgeId1))
    }

    @Test
    fun `clear removes all selections`() {
        val state = SelectionState()
            .add(nodeId1)
            .add(edgeId1)
            .clear()

        assertTrue(state.selectedNodes.isEmpty())
        assertTrue(state.selectedEdges.isEmpty())
    }

    @Test
    fun `remove takes out specific elements`() {
        val state = SelectionState()
            .add(nodeId1)
            .add(nodeId2)
            .add(edgeId1)
            .remove(nodeId1)
            .remove(edgeId1)

        assertEquals(setOf(nodeId2), state.selectedNodes)
        assertTrue(state.selectedEdges.isEmpty())
    }
}
