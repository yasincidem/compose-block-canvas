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
import kotlin.math.roundToInt
import com.yasincidem.blockcanvas.core.alignment.AlignmentDetector
import com.yasincidem.blockcanvas.core.alignment.AlignmentResult
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Viewport
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.routing.EdgeRouter
import com.yasincidem.blockcanvas.core.rules.ConnectionValidator
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.state.SelectionState

/**
 * Hoisted mutable state container for the BlockCanvas composable.
 */
@Stable
public class BlockCanvasState(
    initialCanvasState: CanvasState = CanvasState(),
    initialSelectionState: SelectionState = SelectionState(),
    initialViewport: Viewport = Viewport.Default,
    initialGridConfig: GridConfig = GridConfig.Default,
    public val connectionValidator: ConnectionValidator = DefaultConnectionValidator(),
    public var defaultEdgeRouter: EdgeRouter = EdgeRouter.Bezier,
    public var defaultSourceEnd: EdgeEnd = EdgeEnd.None,
    public var defaultTargetEnd: EdgeEnd = EdgeEnd.Arrow(),
    public var defaultEdgeStroke: EdgeStroke = EdgeStroke.Solid(),
    public var defaultEdgeAnimation: EdgeAnimation = EdgeAnimation.None,
    public var marqueeStyle: MarqueeStyle = MarqueeStyle(),
) {
    public var gridConfig: GridConfig by mutableStateOf(initialGridConfig)

    public var isSpacePressed: Boolean by mutableStateOf(false)
        internal set

    public var interaction: CanvasInteraction by mutableStateOf(CanvasInteraction.Idle)
        internal set

    public var canvasState: CanvasState by mutableStateOf(initialCanvasState)
        private set

    public var selectionState: SelectionState by mutableStateOf(initialSelectionState)
        private set

    public var viewport: Viewport by mutableStateOf(initialViewport)
        private set

    public val nodePositions: SnapshotStateMap<NodeId, Offset> =
        mutableStateMapOf<NodeId, Offset>().also { map ->
            initialCanvasState.nodes.values.forEach { map[it.id] = it.position }
        }

    public var pendingConnection: PendingConnection? by mutableStateOf(null)
        private set

    public var alignmentResult: AlignmentResult? by mutableStateOf(null)
        private set

    private val undoStack = mutableStateListOf<CanvasState>()
    private val redoStack = mutableStateListOf<CanvasState>()
    private var edgeIdCounter = 0

    public val canUndo: Boolean get() = undoStack.isNotEmpty()
    public val canRedo: Boolean get() = redoStack.isNotEmpty()

    private fun mutateCanvas(mutation: (CanvasState) -> CanvasState) {
        val newState = mutation(canvasState)
        if (newState != canvasState) {
            undoStack.add(canvasState)
            redoStack.clear()
            canvasState = newState
        }
    }

    public fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(canvasState)
            val prevState = undoStack.removeAt(undoStack.lastIndex)
            canvasState = prevState
            prevState.nodes.values.forEach { nodePositions[it.id] = it.position }
            nodePositions.keys.retainAll(prevState.nodes.keys)
        }
    }

    public fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(canvasState)
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            canvasState = nextState
            nextState.nodes.values.forEach { nodePositions[it.id] = it.position }
            nodePositions.keys.retainAll(nextState.nodes.keys)
        }
    }

    // ── Connection API ────────────────────────────────────────────────────────

    /** Programmatically begin a connection from [source] in click-click mode. */
    public fun startConnection(source: EndPoint, worldPos: Offset = Offset.Zero) {
        pendingConnection = PendingConnection(source, worldPos, ConnectionMode.Click)
    }

    /** Cancel any in-progress connection without creating an edge. */
    public fun cancelConnection() {
        pendingConnection = null
    }

    /**
     * Attempt to commit a connection from the current pending source to [to].
     * Runs the [connectionValidator]; returns `true` if edge was created.
     * [onAttempt] is called first and can veto by returning `false`.
     */
    internal fun tryCommitConnection(
        to: EndPoint,
        onAttempt: (from: EndPoint, to: EndPoint) -> Boolean = { _, _ -> true },
    ): Boolean {
        val pending = pendingConnection ?: return false
        val from = pending.from
        if (from == to) { cancelConnection(); return false }
        if (!onAttempt(from, to)) { cancelConnection(); return false }

        val portLookup: (EndPoint) -> com.yasincidem.blockcanvas.core.model.Port? = { ep ->
            canvasState.nodes[ep.node]?.ports?.find { it.id == ep.port }
        }
        val error = connectionValidator.validate(from, to, canvasState.edges.values, portLookup)
        if (error != null) { cancelConnection(); return false }

        val edgeId = EdgeId("edge_${edgeIdCounter++}")
        val edge = Edge(id = edgeId, from = from, to = to)
        mutateCanvas { it.addEdge(edge) }
        cancelConnection()
        return true
    }

    internal fun startPendingConnection(from: EndPoint, worldPos: Offset, mode: ConnectionMode = ConnectionMode.Drag) {
        pendingConnection = PendingConnection(from, worldPos, mode)
    }

    internal fun updatePendingConnection(worldPos: Offset) {
        pendingConnection = pendingConnection?.copy(currentPointerWorld = worldPos)
    }

    internal fun clearPendingConnection() {
        pendingConnection = null
    }

    // ── Viewport ──────────────────────────────────────────────────────────────

    public fun updateViewport(new: Viewport) {
        viewport = new
    }

    /** Zooms in by [step] factor, anchored to [anchor] in screen space. */
    public fun zoomIn(anchor: Offset = Offset.Zero, step: Float = DEFAULT_ZOOM_STEP) {
        viewport = viewport.withZoom(viewport.zoom + step, anchor)
    }

    /** Zooms out by [step] factor, anchored to [anchor] in screen space. */
    public fun zoomOut(anchor: Offset = Offset.Zero, step: Float = DEFAULT_ZOOM_STEP) {
        viewport = viewport.withZoom(viewport.zoom - step, anchor)
    }

    public companion object {
        public const val DEFAULT_ZOOM_STEP: Float = 0.1f
    }

    // ── Snapping ──────────────────────────────────────────────────────────────

    /**
     * Snaps a world-space coordinate to the nearest visible grid point on the screen.
     * Uses half-grid precision so nodes land on lines AND midpoints between them.
     *
     * Enabling snap only affects future drags. Call [snapAllToGrid] to immediately
     * move all existing nodes to their nearest grid positions.
     */
    public fun snap(pos: Offset): Offset {
        if (!gridConfig.snapToGrid || gridConfig.style is GridStyle.None) return pos

        val s = gridConfig.style.spacing
        val step = s * 0.5f
        val z = viewport.zoom
        val px = viewport.pan.x
        val py = viewport.pan.y

        val offsetX = px % s
        val offsetY = py % s

        val screenX = pos.x * z + px
        val screenY = pos.y * z + py

        val snappedScreenX = ((screenX - offsetX) / step).roundToInt() * step + offsetX
        val snappedScreenY = ((screenY - offsetY) / step).roundToInt() * step + offsetY

        return Offset(
            x = (snappedScreenX - px) / z,
            y = (snappedScreenY - py) / z,
        )
    }

    /** Immediately snaps every node to its nearest grid position. No-op if snap is disabled. */
    public fun snapAllToGrid() {
        if (!gridConfig.snapToGrid || gridConfig.style is GridStyle.None) return
        mutateCanvas { state ->
            var result = state
            for (node in state.nodes.values) {
                val snapped = snap(node.position)
                nodePositions[node.id] = snapped
                result = result.moveNode(node.id, snapped)
            }
            result
        }
    }

    // ── Node / Edge mutations ─────────────────────────────────────────────────

    public fun moveNode(id: NodeId, newPosition: Offset) {
        val snappedPos = snap(newPosition)
        nodePositions[id] = snappedPos
        mutateCanvas { it.moveNode(id, snappedPos) }
    }

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

    internal fun moveNodeDuringDrag(id: NodeId, newPosition: Offset) {
        nodePositions[id] = newPosition
    }

    internal fun updateAlignmentResult(dragged: Node, threshold: Float = 6f) {
        alignmentResult = AlignmentDetector.detect(dragged, canvasState.nodes.values, threshold)
    }

    internal fun clearAlignmentResult() {
        alignmentResult = null
    }

    public fun addNode(node: Node) {
        nodePositions[node.id] = node.position
        mutateCanvas { it.addNode(node) }
    }

    public fun removeNode(id: NodeId) {
        nodePositions.remove(id)
        mutateCanvas { it.removeNode(id) }
        selectionState = selectionState.remove(id)
    }

    public fun addEdge(edge: Edge) {
        mutateCanvas { it.addEdge(edge) }
    }

    public fun removeEdge(id: EdgeId) {
        mutateCanvas { it.removeEdge(id) }
        selectionState = selectionState.remove(id)
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    public fun toggleSelection(id: NodeId) { selectionState = selectionState.toggle(id) }
    public fun toggleSelection(id: EdgeId) { selectionState = selectionState.toggle(id) }
    public fun selectOnly(id: NodeId)      { selectionState = selectionState.selectOnly(id) }
    public fun selectOnly(id: EdgeId)      { selectionState = selectionState.selectOnly(id) }
    public fun clearSelection()            { selectionState = selectionState.clear() }

    // ── Marquee Selection ─────────────────────────────────────────────────────

    internal fun startMarquee(start: Offset, mode: MarqueeMode) {
        interaction = CanvasInteraction.MarqueeSelecting(start, start, mode)
    }

    internal fun updateMarquee(current: Offset) {
        val currentInteraction = interaction
        if (currentInteraction is CanvasInteraction.MarqueeSelecting) {
            interaction = currentInteraction.copy(current = current)
        }
    }

    internal fun commitMarquee() {
        val currentInteraction = interaction
        if (currentInteraction is CanvasInteraction.MarqueeSelecting) {
            resolveMarqueeSelection(currentInteraction)
            interaction = CanvasInteraction.Idle
        }
    }

    internal fun cancelMarquee() {
        interaction = CanvasInteraction.Idle
    }

    private fun resolveMarqueeSelection(marquee: CanvasInteraction.MarqueeSelecting) {
        val rect = com.yasincidem.blockcanvas.core.geometry.Rect.fromPoints(marquee.start, marquee.current)
        val hits = canvasState.nodes.values.filter { it.boundingBox.intersects(rect) }.map { it.id }.toSet()

        selectionState = when (marquee.mode) {
            MarqueeMode.Replace -> SelectionState(selectedNodes = hits)
            MarqueeMode.Add -> selectionState.copy(selectedNodes = selectionState.selectedNodes + hits)
            MarqueeMode.Subtract -> selectionState.copy(selectedNodes = selectionState.selectedNodes - hits)
        }
    }
}

@Composable
public fun rememberBlockCanvasState(
    initialCanvasState: CanvasState = CanvasState(),
    initialSelectionState: SelectionState = SelectionState(),
    minZoom: Float = Viewport.DEFAULT_MIN_ZOOM,
    maxZoom: Float = Viewport.DEFAULT_MAX_ZOOM,
    initialViewport: Viewport = Viewport.Default.copy(minZoom = minZoom, maxZoom = maxZoom),
    gridConfig: GridConfig = GridConfig.Default,
    connectionValidator: ConnectionValidator = DefaultConnectionValidator(),
): BlockCanvasState {
    return remember {
        BlockCanvasState(initialCanvasState, initialSelectionState, initialViewport, gridConfig, connectionValidator)
    }
}
