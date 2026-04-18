package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.yasincidem.blockcanvas.core.geometry.Offset as CoreOffset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.computePortPosition
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlin.math.abs
import kotlin.math.roundToInt

private fun CoreOffset.toCompose() = androidx.compose.ui.geometry.Offset(x, y)
private fun androidx.compose.ui.geometry.Offset.toCore() = CoreOffset(x, y)

/**
 * The main composable for the node-based editor.
 *
 * Each node is wrapped in a container sized to [Node.width] × [Node.height]
 * in screen pixels (converted to dp via [LocalDensity]) so that port
 * positions computed by [computePortPosition] align exactly with the visual
 * node edges. Edges are rendered as cubic beziers on a [Canvas] layer beneath
 * the nodes.
 *
 * @param state       Hoisted state managing canvas data, selection, and viewport.
 * @param modifier    Modifier for the outer bounds of the canvas.
 * @param nodeContent Slot for rendering a single node. The node container is
 *                    already sized; use [Modifier.fillMaxSize] inside.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    nodeContent: @Composable (Node) -> Unit,
) {
    val density = LocalDensity.current

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

        // Edge layer — drawn in screen space using Viewport.worldToScreen.
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.canvasState.edges.values.forEach { edge ->
                val fromNode = state.canvasState.nodes[edge.from.node] ?: return@forEach
                val toNode   = state.canvasState.nodes[edge.to.node]   ?: return@forEach
                val fromPort = fromNode.ports.find { it.id == edge.from.port } ?: return@forEach
                val toPort   = toNode.ports.find   { it.id == edge.to.port }   ?: return@forEach

                val fromScreen = state.viewport.worldToScreen(computePortPosition(fromNode, fromPort)).toCompose()
                val toScreen   = state.viewport.worldToScreen(computePortPosition(toNode,   toPort)).toCompose()
                drawEdgeBezier(fromScreen, toScreen)
            }
        }

        // Node layer — viewport applied via graphicsLayer.
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
                        // Size the container in dp so it matches the pixel world-space
                        // dimensions: visual px = node.width, so dp = px / density.
                        .size(
                            width  = with(density) { node.width.toDp() },
                            height = with(density) { node.height.toDp() },
                        )
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
