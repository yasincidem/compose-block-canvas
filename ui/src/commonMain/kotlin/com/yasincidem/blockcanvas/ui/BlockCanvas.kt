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
import com.yasincidem.blockcanvas.core.hittest.DefaultHitTester
import com.yasincidem.blockcanvas.core.hittest.HitResult
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

                    when (val hit = hitTester.hitTest(worldPos, state.canvasState.nodes.values)) {
                        is HitResult.Port -> {
                            down.consume()
                            val from = EndPoint(hit.nodeId, hit.portId)
                            state.startPendingConnection(from, worldPos)

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
                                if (from != to) onConnectionRequestState(from, to)
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

                            while (true) {
                                val dragEvent = awaitPointerEvent()
                                val change = dragEvent.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    // Persist final position to canvasState (one recomposition at drag-end) for all selected
                                    state.selectionState.selectedNodes.forEach { selectedId ->
                                        val finalPos = state.nodePositions[selectedId]
                                        if (finalPos != null) state.moveNode(selectedId, finalPos)
                                    }
                                    break
                                }
                                change.consume()
                                val delta = (change.position - change.previousPosition).toCore() / state.viewport.zoom
                                
                                state.selectionState.selectedNodes.forEach { selectedId ->
                                    val current = state.nodePositions[selectedId] ?: return@forEach
                                    state.moveNodeDuringDrag(selectedId, current + delta)
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
        // z-order: edges behind, nodes above.

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
                    computePortPosition(fromNode.copy(position = fromPos), fromPort)
                ).toCompose()
                val toScreen = state.viewport.worldToScreen(
                    computePortPosition(toNode.copy(position = toPos), toPort)
                ).toCompose()
                drawEdgeBezier(fromScreen, toScreen)
            }

            // Live bezier while a connection is being drawn.
            state.pendingConnection?.let { pending ->
                val fromNode = state.canvasState.nodes[pending.from.node] ?: return@let
                val fromPort = fromNode.ports.find { it.id == pending.from.port } ?: return@let
                val fromPos  = state.nodePositions[fromNode.id] ?: fromNode.position
                val fromScreen = state.viewport.worldToScreen(
                    computePortPosition(fromNode.copy(position = fromPos), fromPort)
                ).toCompose()
                val toScreen = state.viewport.worldToScreen(pending.currentPointerWorld).toCompose()
                drawEdgeBezier(fromScreen, toScreen, pending = true)
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
    from: androidx.compose.ui.geometry.Offset,
    to: androidx.compose.ui.geometry.Offset,
    pending: Boolean = false,
) {
    val handle = abs(to.x - from.x).coerceAtLeast(60f) * 0.5f
    val path = Path().apply {
        moveTo(from.x, from.y)
        cubicTo(
            x1 = from.x + handle, y1 = from.y,
            x2 = to.x - handle,   y2 = to.y,
            x3 = to.x,            y3 = to.y,
        )
    }
    val color = if (pending) Color(0xFF_5B8DEF).copy(alpha = 0.55f) else Color(0xFF_5B8DEF)
    val stroke = if (pending) {
        Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
    } else {
        Stroke(width = 2f)
    }
    drawPath(path, color = color, style = stroke)
}
