package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.yasincidem.blockcanvas.core.geometry.Offset as CoreOffset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlin.math.roundToInt

private fun androidx.compose.ui.geometry.Offset.toCore() = CoreOffset(x, y)

/**
 * The main composable for the node-based editor.
 *
 * Supports pinch-to-zoom and pan via [detectTransformGestures]. The viewport
 * transform is applied via [graphicsLayer] so nodes are rendered in world
 * space and the layer handles scale + translation.
 *
 * @param state         Hoisted state managing canvas data, selection, and viewport.
 * @param modifier      Modifier for the outer bounds of the canvas.
 * @param nodeContent   Slot for rendering a single node. Receives the [Node] model;
 *                      the canvas positions it automatically.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
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
                    modifier = Modifier.offset {
                        IntOffset(
                            x = node.position.x.roundToInt(),
                            y = node.position.y.roundToInt(),
                        )
                    }
                ) {
                    nodeContent(node)
                }
            }
        }
    }
}
