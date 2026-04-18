package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.state.SelectionState

/**
 * Hoisted mutable state container for the BlockCanvas composable.
 *
 * This bridges the immutable data layers ([CanvasState], [SelectionState], [Viewport])
 * with Compose's reactivity system.
 */
@Stable
public class BlockCanvasState(
    initialCanvasState: CanvasState = CanvasState(),
    initialSelectionState: SelectionState = SelectionState(),
    initialViewport: Viewport = Viewport.Default,
) {
    public var canvasState: CanvasState by mutableStateOf(initialCanvasState)
        private set

    public var selectionState: SelectionState by mutableStateOf(initialSelectionState)
        private set

    public var viewport: Viewport by mutableStateOf(initialViewport)
        private set

    /** Replaces the current viewport (pan + zoom). */
    public fun updateViewport(new: Viewport) {
        viewport = new
    }

    /**
     * Adds or updates a node in the canvas.
     */
    public fun addNode(node: Node) {
        canvasState = canvasState.addNode(node)
    }

    /**
     * Removes a node and ensures it loses its selection status.
     * Associated edges are removed automatically by [CanvasState].
     */
    public fun removeNode(id: NodeId) {
        canvasState = canvasState.removeNode(id)
        selectionState = selectionState.remove(id)
    }

    /**
     * Adds an edge to the canvas.
     */
    public fun addEdge(edge: Edge) {
        canvasState = canvasState.addEdge(edge)
    }

    /**
     * Removes an edge and clears its selection status.
     */
    public fun removeEdge(id: EdgeId) {
        canvasState = canvasState.removeEdge(id)
        selectionState = selectionState.remove(id)
    }

    /**
     * Toggles selection for a node.
     */
    public fun toggleSelection(id: NodeId) {
        selectionState = selectionState.toggle(id)
    }

    /**
     * Toggles selection for an edge.
     */
    public fun toggleSelection(id: EdgeId) {
        selectionState = selectionState.toggle(id)
    }
    
    /**
     * Exclusively selects a single node, clearing other selections.
     */
    public fun selectOnly(id: NodeId) {
        selectionState = selectionState.selectOnly(id)
    }
    
    /**
     * Clears all active selections.
     */
    public fun clearSelection() {
        selectionState = selectionState.clear()
    }
}

/**
 * Creates and remembers a [BlockCanvasState].
 */
@Composable
public fun rememberBlockCanvasState(
    initialCanvasState: CanvasState = CanvasState(),
    initialSelectionState: SelectionState = SelectionState(),
    initialViewport: Viewport = Viewport.Default,
): BlockCanvasState {
    return remember {
        BlockCanvasState(initialCanvasState, initialSelectionState, initialViewport)
    }
}
