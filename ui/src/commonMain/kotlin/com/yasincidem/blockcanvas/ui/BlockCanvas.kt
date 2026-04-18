package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.yasincidem.blockcanvas.core.geometry.Offset as CoreOffset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlin.math.abs
import kotlin.math.roundToInt

private fun CoreOffset.toCompose() = androidx.compose.ui.geometry.Offset(x, y)
private fun androidx.compose.ui.geometry.Offset.toCore() = CoreOffset(x, y)

/**
 * The main composable for the node-based editor.
 *
 * Renders edges as cubic beziers on a [Canvas] layer beneath the nodes, then
 * positions nodes in world space via a [graphicsLayer] viewport transform.
 * Pinch-to-zoom and pan are handled by [detectTransformGestures]; per-node
 * drag is handled by [detectDragGestures].
 *
 * @param state         Hoisted state managing canvas data, selection, and viewport.
 * @param modifier      Modifier for the outer bounds of the canvas.
 * @param portPosition  Resolves a port's world-space position given its node and port ids.
 *                      Return `null` to skip drawing edges that involve that port.
 * @param nodeContent   Slot for rendering a single node. The canvas positions it automatically.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    portPosition: (nodeId: NodeId, portId: PortId) -> CoreOffset? = { _, _ -> null },
    nodeContent: @Composable (Node) -> Unit,
) {
    Box(
        modifier = modifier.pointerInput(state) {
            detectTransformGestures { centroid, pan, zoomFactor, _ ->
                val afterZoom = state.viewport.withZoom(
                    newZoom = state.viewport.zoom * zoomFactor,
                    anchor = centroid.toCore(),
                )
                state.updateViewport(afterZoom.withPan(afterZoom.pan + pan.toCore()))
            }
        }
    ) {
        // z-order: edges behind, nodes above.

        // Edge layer — drawn in screen space with manual viewport transform so the
        // Canvas can fill the full layout bounds.
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.canvasState.edges.values.forEach { edge ->
                val fromWorld = portPosition(edge.from.node, edge.from.port) ?: return@forEach
                val toWorld = portPosition(edge.to.node, edge.to.port) ?: return@forEach
                val from = state.viewport.worldToScreen(fromWorld).toCompose()
                val to = state.viewport.worldToScreen(toWorld).toCompose()
                drawEdgeBezier(from, to)
            }
        }

        // Node layer — world-to-screen transform applied by graphicsLayer so each
        // node is positioned with its world-space Offset directly.
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
                            IntOffset(
                                x = node.position.x.roundToInt(),
                                y = node.position.y.roundToInt(),
                            )
                        }
                        .pointerInput(node.id) {
                            detectDragGestures { _, dragAmount ->
                                val worldDelta = dragAmount.toCore() / state.viewport.zoom
                                val current = state.canvasState.nodes[node.id]?.position
                                    ?: return@detectDragGestures
                                state.moveNode(node.id, current + worldDelta)
                            }
                        }
                ) {
                    nodeContent(node)
                }
            }
        }
    }
}

private fun DrawScope.drawEdgeBezier(
    from: androidx.compose.ui.geometry.Offset,
    to: androidx.compose.ui.geometry.Offset,
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
    drawPath(path, color = Color(0xFF_5B8DEF), style = Stroke(width = 2f))
}
