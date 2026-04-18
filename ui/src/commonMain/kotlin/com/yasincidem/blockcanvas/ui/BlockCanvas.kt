package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.hittest.DefaultHitTester
import com.yasincidem.blockcanvas.core.hittest.HitResult
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.computePortPosition
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlin.math.abs
import kotlin.math.roundToInt
import com.yasincidem.blockcanvas.core.geometry.Offset as CoreOffset

private fun CoreOffset.toCompose() = androidx.compose.ui.geometry.Offset(x, y)
private fun androidx.compose.ui.geometry.Offset.toCore() = CoreOffset(x, y)

/**
 * The main composable for the node-based editor.
 *
 * Gesture dispatch — single outer [pointerInput] hit-tests on every pointer-down
 * and routes to one of three behaviours:
 *  - **Port**: starts a pending connection; draws a live bezier to the pointer;
 *    fires [onConnectionRequest] when released over a different port.
 *  - **Node**: drags the node in world space.
 *  - **Empty**: falls through to [detectTransformGestures] for pan / zoom.
 *
 * @param state                 Hoisted state managing canvas data, selection, and viewport.
 * @param modifier              Modifier for the outer bounds of the canvas.
 * @param onConnectionRequest   Called when a connection drag is released over a target port.
 * @param nodeContent           Slot for rendering a single node. The container is already
 *                              sized; use [Modifier.fillMaxSize] inside.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    onConnectionRequest: (from: EndPoint, to: EndPoint) -> Unit = { _, _ -> },
    nodeContent: @Composable (node: Node, isSelected: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val hitTester = remember { DefaultHitTester() }
    val onConnectionRequestState by rememberUpdatedState(onConnectionRequest)
    // Reuse Path to avoid allocations in draw loop
    val sharedPath = remember { Path() }

    Box(
        modifier = modifier
            // Pan / zoom — outer modifier, processed SECOND in Main pass.
            // detectTransformGestures checks `canceled = event.changes.any { it.isConsumed }` and
            // skips onGesture when the inner handler has already consumed the events.
            .pointerInput(state) {
                detectTransformGestures { centroid, pan, zoomFactor, _ ->
                    val afterZoom = state.viewport.withZoom(
                        newZoom = state.viewport.zoom * zoomFactor,
                        anchor = centroid.toCore(),
                    )
                    state.updateViewport(afterZoom.withPan(afterZoom.pan + pan.toCore()))
                }
            }
            // Connection drag + node drag — inner modifier, processed FIRST in Main pass.
            // Consumes events for Port/Node hits so the outer pan/zoom handler is suppressed.
            .pointerInput(state) {
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    val down = event.changes.first()
                    val isShiftPressed = event.keyboardModifiers.isShiftPressed
                    val worldPos = state.viewport.screenToWorld(down.position.toCore())

                    // 1. Edge Hit Testing (drawn in UI so hit-tested here)
                    val edgeHit = hitTestEdges(worldPos, state)
                    if (edgeHit != null) {
                        down.consume()
                        if (isShiftPressed) {
                            state.toggleSelection(edgeHit)
                        } else {
                            state.selectOnly(edgeHit)
                        }
                        return@awaitEachGesture
                    }

                    // 2. Core Hit Testing for Ports and Nodes
                    when (val hit = hitTester.hitTest(worldPos, state.canvasState.nodes.values)) {
                        is HitResult.Port -> {
                            down.consume()
                            val clickedEndpoint = EndPoint(hit.nodeId, hit.portId)
                            
                            // Reconnection logic: if port has an edge, rip it off to reposition!
                            val edgeToDetach = state.canvasState.edges.values.find { it.to == clickedEndpoint }
                                ?: state.canvasState.edges.values.find { it.from == clickedEndpoint }
                            
                            val fixedEnd = if (edgeToDetach != null) {
                                state.removeEdge(edgeToDetach.id)
                                if (edgeToDetach.to == clickedEndpoint) edgeToDetach.from else edgeToDetach.to
                            } else {
                                clickedEndpoint
                            }

                            state.startPendingConnection(fixedEnd, worldPos)

                            var upHit: HitResult = HitResult.Empty
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == down.id } ?: break
                                change.consume()
                                val curr = state.viewport.screenToWorld(change.position.toCore())
                                state.updatePendingConnection(curr)
                                if (!change.pressed) {
                                    upHit = hitTester.hitTest(curr, state.canvasState.nodes.values)
                                    break
                                }
                            }

                            if (upHit is HitResult.Port) {
                                val to = EndPoint(upHit.nodeId, upHit.portId)
                                if (fixedEnd != to) onConnectionRequestState(fixedEnd, to)
                            }
                            state.clearPendingConnection()
                        }

                        is HitResult.Node -> {
                            down.consume()
                            
                            // Multi-selection handling
                            if (isShiftPressed) {
                                state.toggleSelection(hit.nodeId)
                            } else if (!state.selectionState.isSelected(hit.nodeId)) {
                                state.selectOnly(hit.nodeId)
                            }

                            val startPositions = state.selectionState.selectedNodes.associateWith {
                                state.nodePositions[it] ?: state.canvasState.nodes[it]?.position ?: CoreOffset(0f, 0f)
                            }
                            var totalDelta = CoreOffset(0f, 0f)

                            while (true) {
                                val dragEvent = awaitPointerEvent()
                                val change = dragEvent.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    // Persist final position to canvasState atomically for all selected
                                    val finalPlacements = buildMap {
                                        state.selectionState.selectedNodes.forEach { id ->
                                            val pos = state.nodePositions[id]
                                            if (pos != null) put(id, pos)
                                        }
                                    }
                                    
                                    state.commitNodePositions(finalPlacements)
                                    break
                                }
                                change.consume()
                                val delta = (change.position - change.previousPosition).toCore() / state.viewport.zoom
                                totalDelta = CoreOffset(totalDelta.x + delta.x, totalDelta.y + delta.y)
                                
                                state.selectionState.selectedNodes.forEach { selectedId ->
                                    val startPos = startPositions[selectedId] ?: return@forEach
                                    val rawPos = CoreOffset(startPos.x + totalDelta.x, startPos.y + totalDelta.y)
                                    
                                    val snappedPos = if (state.snapToGrid > 0f) {
                                        CoreOffset(
                                            x = (rawPos.x / state.snapToGrid).roundToInt() * state.snapToGrid,
                                            y = (rawPos.y / state.snapToGrid).roundToInt() * state.snapToGrid
                                        )
                                    } else {
                                        rawPos
                                    }
                                    state.moveNodeDuringDrag(selectedId, snappedPos)
                                }
                            }
                        }

                        HitResult.Empty -> {
                            if (!isShiftPressed) {
                                state.clearSelection()
                            }
                            /* fall through to pan/zoom */ 
                        }
                    }
                }
            }
    ) {
        // z-order: grid behind, edges behind, nodes above.
        
        // Grid layer
        if (state.snapToGrid > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaledGrid = state.snapToGrid * state.viewport.zoom
                if (scaledGrid < 10f) return@Canvas // Too small to see, and would kill performance
                
                // modulo to find the starting offset
                val panX = state.viewport.pan.x % scaledGrid
                val panY = state.viewport.pan.y % scaledGrid
                val dotRadius = 1.dp.toPx()
                val dotColor = Color.White.copy(alpha = 0.15f)
                
                var x = if (panX > 0) panX - scaledGrid else panX
                while (x < size.width) {
                    var y = if (panY > 0) panY - scaledGrid else panY
                    while (y < size.height) {
                        drawCircle(dotColor, radius = dotRadius, center = androidx.compose.ui.geometry.Offset(x, y))
                        y += scaledGrid
                    }
                    x += scaledGrid
                }
            }
        }

        // Edge layer — drawn in screen space using Viewport.worldToScreen.
        // Reads state.nodePositions (SnapshotStateMap) so position changes during drag
        // trigger a re-draw here, not a recomposition of the whole BlockCanvas.
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.canvasState.edges.values.forEach { edge ->
                val fromNode = state.canvasState.nodes[edge.from.node] ?: return@forEach
                val toNode   = state.canvasState.nodes[edge.to.node]   ?: return@forEach
                val fromPort = fromNode.ports.find { it.id == edge.from.port } ?: return@forEach
                val toPort   = toNode.ports.find   { it.id == edge.to.port }   ?: return@forEach

                // Use live nodePositions so edges follow nodes during drag without recomposition.
                val fromPos = state.nodePositions[fromNode.id] ?: fromNode.position
                val toPos   = state.nodePositions[toNode.id]   ?: toNode.position
                val fromScreen = state.viewport.worldToScreen(
                    computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
                ).toCompose()
                val toScreen = state.viewport.worldToScreen(
                    computePortPosition(toPos, toNode.width, toNode.height, toPort.side)
                ).toCompose()
                
                val isSelected = state.selectionState.isSelected(edge.id)
                drawEdgeBezier(sharedPath, fromScreen, toScreen, selected = isSelected)
            }

            // Live bezier while a connection is being drawn.
            state.pendingConnection?.let { pending ->
                val fromNode = state.canvasState.nodes[pending.from.node] ?: return@let
                val fromPort = fromNode.ports.find { it.id == pending.from.port } ?: return@let
                val fromPos  = state.nodePositions[fromNode.id] ?: fromNode.position
                val fromScreen = state.viewport.worldToScreen(
                    computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
                ).toCompose()
                val toScreen = state.viewport.worldToScreen(pending.currentPointerWorld).toCompose()
                drawEdgeBezier(sharedPath, fromScreen, toScreen, pending = true)
            }
        }

        // Node layer — viewport applied via graphicsLayer (no per-node gesture handling needed).
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = state.viewport.pan.x
                translationY = state.viewport.pan.y
                scaleX = state.viewport.zoom
                scaleY = state.viewport.zoom
                transformOrigin = TransformOrigin(0f, 0f)
            }
        ) {
            state.canvasState.nodes.values.forEach { node ->
                Box(
                    modifier = Modifier
                        .offset {
                            // Read from nodePositions (layout phase) so drag updates
                            // only re-layout this node, not recompose BlockCanvas.
                            val pos = state.nodePositions[node.id] ?: node.position
                            IntOffset(
                                x = pos.x.roundToInt(),
                                y = pos.y.roundToInt(),
                            )
                        }
                        // Size the container in dp so it matches the pixel world-space
                        // dimensions: visual px = node.width, so dp = px / density.
                        .size(
                            width  = with(density) { node.width.toDp() },
                            height = with(density) { node.height.toDp() },
                        )
                ) {
                    val isSelected = state.selectionState.isSelected(node.id)
                    nodeContent(node, isSelected)
                }
            }
        }
    }
}

private fun DrawScope.drawEdgeBezier(
    path: Path,
    from: androidx.compose.ui.geometry.Offset,
    to: androidx.compose.ui.geometry.Offset,
    pending: Boolean = false,
    selected: Boolean = false,
) {
    val handle = abs(to.x - from.x).coerceAtLeast(60f) * 0.5f
    path.reset()
    path.moveTo(from.x, from.y)
    path.cubicTo(
        x1 = from.x + handle, y1 = from.y,
        x2 = to.x - handle,   y2 = to.y,
        x3 = to.x,            y3 = to.y,
    )
    
    if (selected) {
        val highlightColor = Color.White.copy(alpha = 0.5f)
        drawPath(path, color = highlightColor, style = Stroke(width = 8f))
    }
    
    val color = if (pending) Color(0xFF_5B8DEF).copy(alpha = 0.55f) else Color(0xFF_5B8DEF)
    val stroke = if (pending) {
        Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
    } else {
        Stroke(width = 2.5f)
    }
    drawPath(path, color = color, style = stroke)
}

private fun hitTestEdges(
    worldPos: CoreOffset,
    state: BlockCanvasState,
    tolerancePx: Float = 10f
): EdgeId? {
    val scaledTolerance = tolerancePx / state.viewport.zoom
    val squaredTolerance = scaledTolerance * scaledTolerance

    for (edge in state.canvasState.edges.values.reversed()) {
        val fromNode = state.canvasState.nodes[edge.from.node] ?: continue
        val toNode = state.canvasState.nodes[edge.to.node] ?: continue
        val fromPort = fromNode.ports.find { it.id == edge.from.port } ?: continue
        val toPort = toNode.ports.find { it.id == edge.to.port } ?: continue

        val fromPos = state.nodePositions[fromNode.id] ?: fromNode.position
        val toPos = state.nodePositions[toNode.id] ?: toNode.position
        val p0 = computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
        val p3 = computePortPosition(toPos, toNode.width, toNode.height, toPort.side)

        val handle = abs(p3.x - p0.x).coerceAtLeast(60f) * 0.5f
        val p1 = CoreOffset(p0.x + handle, p0.y)
        val p2 = CoreOffset(p3.x - handle, p3.y)

        // Sample points along the bezier curve
        for (i in 0..20) {
            val t = i / 20f
            val u = 1 - t
            val u2 = u * u
            val u3 = u2 * u
            val t2 = t * t
            val t3 = t2 * t

            val x = u3 * p0.x + 3 * u2 * t * p1.x + 3 * u * t2 * p2.x + t3 * p3.x
            val y = u3 * p0.y + 3 * u2 * t * p1.y + 3 * u * t2 * p2.y + t3 * p3.y

            val dx = x - worldPos.x
            val dy = y - worldPos.y
            if (dx * dx + dy * dy <= squaredTolerance) {
                return edge.id
            }
        }
    }
    return null
}
