package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlin.math.roundToInt

/**
 * The main composable for the node-based editor.
 *
 * @param state The hoisted state object managing the canvas and selection.
 * @param modifier The modifier to be applied to the canvas layout.
 * @param nodeContent A slot for rendering a standalone node. The consumer is responsible for providing
 *   the visual representation. The node is positioned within the canvas automatically.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    nodeContent: @Composable (Node) -> Unit,
) {
    Box(modifier = modifier) {
        // Render nodes 
        // (Edges and selection highlights will be placed below or above nodes respectively in future iterations)
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
