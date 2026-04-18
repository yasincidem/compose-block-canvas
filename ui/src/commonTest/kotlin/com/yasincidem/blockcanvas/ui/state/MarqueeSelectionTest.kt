package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Rect
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.state.SelectionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarqueeSelectionTest {

    @Test
    fun `rect intersection handles common cases`() {
        val nodeRect = Rect(10f, 10f, 60f, 60f)

        // Fully overlapping
        assertTrue(nodeRect.intersects(Rect(0f, 0f, 100f, 100f)))
        // Fully contained
        assertTrue(nodeRect.intersects(Rect(20f, 20f, 30f, 30f)))
        // Partial overlap (top-left)
        assertTrue(nodeRect.intersects(Rect(0f, 0f, 20f, 20f)))
        // No overlap
        assertFalse(nodeRect.intersects(Rect(70f, 70f, 80f, 80f)))
    }

    @Test
    fun `resolveMarqueeSelection Replace mode clears old selection`() {
        val n1 = Node(NodeId("n1"), Offset(10f, 10f), 50f, 50f, emptyList())
        val n2 = Node(NodeId("n2"), Offset(100f, 100f), 50f, 50f, emptyList())
        
        val state = BlockCanvasState(
            initialCanvasState = CanvasState(nodes = mapOf(n1.id to n1, n2.id to n2)),
            initialSelectionState = SelectionState(selectedNodes = setOf(n2.id))
        )

        // Select Marquee covering n1
        val marquee = CanvasInteraction.MarqueeSelecting(
            start = Offset(0f, 0f),
            current = Offset(70f, 70f),
            mode = MarqueeMode.Replace
        )
        
        state.interaction = marquee
        state.commitMarquee()
        
        assertEquals(setOf(n1.id), state.selectionState.selectedNodes)
    }

    @Test
    fun `resolveMarqueeSelection Add mode unions with existing selection`() {
        val n1 = Node(NodeId("n1"), Offset(10f, 10f), 50f, 50f, emptyList())
        val n2 = Node(NodeId("n2"), Offset(100f, 100f), 50f, 50f, emptyList())

        val state = BlockCanvasState(
            initialCanvasState = CanvasState(nodes = mapOf(n1.id to n1, n2.id to n2)),
            initialSelectionState = SelectionState(selectedNodes = setOf(n2.id))
        )

        val marquee = CanvasInteraction.MarqueeSelecting(
            start = Offset(0f, 0f),
            current = Offset(70f, 70f),
            mode = MarqueeMode.Add
        )

        state.interaction = marquee
        state.commitMarquee()

        assertEquals(setOf(n1.id, n2.id), state.selectionState.selectedNodes)
    }

    @Test
    fun `resolveMarqueeSelection Subtract mode removes overlapping from selection`() {
        val n1 = Node(NodeId("n1"), Offset(10f, 10f), 50f, 50f, emptyList())
        val n2 = Node(NodeId("n2"), Offset(100f, 100f), 50f, 50f, emptyList())

        val state = BlockCanvasState(
            initialCanvasState = CanvasState(nodes = mapOf(n1.id to n1, n2.id to n2)),
            initialSelectionState = SelectionState(selectedNodes = setOf(n1.id, n2.id))
        )

        val marquee = CanvasInteraction.MarqueeSelecting(
            start = Offset(0f, 0f),
            current = Offset(70f, 70f),
            mode = MarqueeMode.Subtract
        )

        state.interaction = marquee
        state.commitMarquee()

        assertEquals(setOf(n2.id), state.selectionState.selectedNodes)
    }
}
