package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.state.SelectionState

/**
 * Hoisted mutable state container for the BlockCanvas composable.
 *
 * This bridges the immutable data layers ([CanvasState], [SelectionState], [Viewport])
 * with Compose's reactivity system.
 *
 * Node positions are tracked in two places:
 *  - [nodePositions]: fine-grained [SnapshotStateMap] updated on every drag frame.
 *    Reads inside `offset {}` / Canvas `onDraw` lambdas re-layout or re-draw only the
 *    affected node — no recomposition.
 *  - [canvasState]: coarse-grained atom updated once at drag-end. Reads in the
 *    composition scope (e.g. the node `forEach`) only recompose on structural changes.
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

    /**
     * Per-node positions for layout/draw-phase reads.
     * Updated every drag frame; does NOT replace [canvasState] until drag ends,
     * so the composition scope never recomposes during a drag.
     */
    public val nodePositions: SnapshotStateMap<NodeId, Offset> =
        mutableStateMapOf<NodeId, Offset>().also { map ->
            initialCanvasState.nodes.values.forEach { map[it.id] = it.position }
        }

    /** Live connection being drawn from a port to the current pointer position. */
    public var pendingConnection: PendingConnection? by mutableStateOf(null)
        private set

    /** Starts a pending connection from [from] at the given world position. */
    public fun startPendingConnection(from: EndPoint, worldPos: Offset) {
        pendingConnection = PendingConnection(from, worldPos)
    }

    /** Updates the tip of the pending connection to [worldPos]. */
    public fun updatePendingConnection(worldPos: Offset) {
        pendingConnection = pendingConnection?.copy(currentPointerWorld = worldPos)
    }

    /** Removes the pending connection (drag ended or was cancelled). */
    public fun clearPendingConnection() {
        pendingConnection = null
    }

    /** Replaces the current viewport (pan + zoom). */
    public fun updateViewport(new: Viewport) {
        viewport = new
    }

    /**
     * Moves a node to [newPosition] in world space, updating both [nodePositions] and
     * [canvasState]. No-op if [id] is not found.
     */
    public fun moveNode(id: NodeId, newPosition: Offset) {
        nodePositions[id] = newPosition
        canvasState = canvasState.moveNode(id, newPosition)
    }

    /**
     * Updates only [nodePositions] for [id] — skips the [canvasState] replacement so
     * the composition scope never recomposes. Call [moveNode] once at drag-end to persist.
     */
    internal fun moveNodeDuringDrag(id: NodeId, newPosition: Offset) {
        nodePositions[id] = newPosition
    }

    /** Adds or updates a node in the canvas. */
    public fun addNode(node: Node) {
        nodePositions[node.id] = node.position
        canvasState = canvasState.addNode(node)
    }

    /**
     * Removes a node and ensures it loses its selection status.
     * Associated edges are removed automatically by [CanvasState].
     */
    public fun removeNode(id: NodeId) {
        nodePositions.remove(id)
        canvasState = canvasState.removeNode(id)
        selectionState = selectionState.remove(id)
    }

    /** Adds an edge to the canvas. */
    public fun addEdge(edge: Edge) {
        canvasState = canvasState.addEdge(edge)
    }

    /** Removes an edge and clears its selection status. */
    public fun removeEdge(id: EdgeId) {
        canvasState = canvasState.removeEdge(id)
        selectionState = selectionState.remove(id)
    }

    /** Toggles selection for a node. */
    public fun toggleSelection(id: NodeId) {
        selectionState = selectionState.toggle(id)
    }

    /** Toggles selection for an edge. */
    public fun toggleSelection(id: EdgeId) {
        selectionState = selectionState.toggle(id)
    }

    /** Exclusively selects a single node, clearing other selections. */
    public fun selectOnly(id: NodeId) {
        selectionState = selectionState.selectOnly(id)
    }

    /** Clears all active selections. */
    public fun clearSelection() {
        selectionState = selectionState.clear()
    }
}

/** Creates and remembers a [BlockCanvasState]. */
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
