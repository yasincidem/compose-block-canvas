package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateListOf
import kotlin.math.round
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
    initialGridConfig: GridConfig = GridConfig.Default,
) {
    public var gridConfig: GridConfig by mutableStateOf(initialGridConfig)

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

    /** Stack of past states for [undo]. */
    private val undoStack = mutableStateListOf<CanvasState>()

    /** Stack of reverted states for [redo]. */
    private val redoStack = mutableStateListOf<CanvasState>()

    /** Whether an [undo] operation can be performed. */
    public val canUndo: Boolean get() = undoStack.isNotEmpty()

    /** Whether a [redo] operation can be performed. */
    public val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Centralized state mutator that pushes to the history stack. */
    private fun mutateCanvas(mutation: (CanvasState) -> CanvasState) {
        val newState = mutation(canvasState)
        if (newState != canvasState) {
            undoStack.add(canvasState)
            redoStack.clear()
            canvasState = newState
        }
    }

    /** Reverts the last layout/structure modification. */
    public fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(canvasState)
            val prevState = undoStack.removeAt(undoStack.lastIndex)
            canvasState = prevState
            prevState.nodes.values.forEach { nodePositions[it.id] = it.position }
            // Remove lingering tracked positions for deleted nodes
            nodePositions.keys.retainAll(prevState.nodes.keys)
        }
    }

    /** Re-applies the last reverted layout/structure modification. */
    public fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(canvasState)
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            canvasState = nextState
            nextState.nodes.values.forEach { nodePositions[it.id] = it.position }
            nodePositions.keys.retainAll(nextState.nodes.keys)
        }
    }

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
     * Snaps a world-space coordinate to the nearest visible grid point on the screen.
     * accounts for current zoom and pan to achieve pixel-perfect alignment.
     */
    public fun snap(pos: Offset): Offset {
        if (!gridConfig.snapToGrid || gridConfig.type == GridType.None) return pos
        
        val s = gridConfig.spacing
        val z = viewport.zoom
        val px = viewport.pan.x
        val py = viewport.pan.y
        
        return Offset(
            x = (round((pos.x * z + px) / s) * s - px) / z,
            y = (round((pos.y * z + py) / s) * s - py) / z
        )
    }

    /**
     * Moves a node to [newPosition] in world space, updating both [nodePositions] and
     * [canvasState] atomically.
     */
    public fun moveNode(id: NodeId, newPosition: Offset) {
        val snappedPos = snap(newPosition)
        nodePositions[id] = snappedPos
        mutateCanvas { it.moveNode(id, snappedPos) }
    }

    /**
     * Moves multiple nodes atomically, producing exactly one frame in the undo history.
     */
    public fun commitNodePositions(positions: Map<NodeId, Offset>) {
        mutateCanvas { state ->
            var finalState = state
            for ((id, pos) in positions) {
                nodePositions[id] = pos
                finalState = finalState.moveNode(id, pos)
            }
            finalState
        }
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
        mutateCanvas { it.addNode(node) }
    }

    /**
     * Removes a node and ensures it loses its selection status.
     * Associated edges are removed automatically by [CanvasState].
     */
    public fun removeNode(id: NodeId) {
        nodePositions.remove(id)
        mutateCanvas { it.removeNode(id) }
        selectionState = selectionState.remove(id)
    }

    /** Adds an edge to the canvas. */
    public fun addEdge(edge: Edge) {
        mutateCanvas { it.addEdge(edge) }
    }

    /** Removes an edge and clears its selection status. */
    public fun removeEdge(id: EdgeId) {
        mutateCanvas { it.removeEdge(id) }
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

    /** Exclusively selects a single edge, clearing other selections. */
    public fun selectOnly(id: EdgeId) {
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
    gridConfig: GridConfig = GridConfig.Default,
): BlockCanvasState {
    return remember {
        BlockCanvasState(initialCanvasState, initialSelectionState, initialViewport, gridConfig)
    }
}
