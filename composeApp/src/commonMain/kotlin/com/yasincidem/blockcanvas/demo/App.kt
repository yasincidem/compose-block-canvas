package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

/** Pixel size of the port dot indicator. */
private val PORT_DOT_DP = 12.dp

/** Builds a node with one port on each of the four sides. */
private fun fourPortNode(id: String, x: Float, y: Float, w: Float, h: Float) = Node(
    id = NodeId(id),
    position = Offset(x, y),
    width = w,
    height = h,
    ports = listOf(
        Port(PortId("top"),    PortSide.Top),
        Port(PortId("right"),  PortSide.Right),
        Port(PortId("bottom"), PortSide.Bottom),
        Port(PortId("left"),   PortSide.Left),
    ),
)

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current.density

            // World coordinates are screen pixels (at zoom=1, pan=0).
            // At 2× density a 150-pixel-wide node is 75 dp, matching what BlockCanvas renders.
            val nodeW = 150f * density
            val nodeH = 80f  * density
            val gap   = 60f  * density

            val initialCanvas = remember(density) {
                buildCanvasState {
                    // Row 1 — decoration showcase
                    node("node_1") {
                        at(x = gap, y = 160f * density)
                        size(nodeW, nodeH)
                        port("right", PortSide.Right)
                    }
                    node("node_2") {
                        rightOf("node_1", gap = gap)
                        size(nodeW, nodeH)
                        port("left", PortSide.Left)
                        port("right", PortSide.Right)
                    }
                    node("node_3") {
                        rightOf("node_2", gap = gap)
                        size(nodeW, nodeH)
                        port("left", PortSide.Left)
                        port("right", PortSide.Right)
                    }
                    node("node_4") {
                        rightOf("node_3", gap = gap)
                        size(nodeW, nodeH)
                        port("left", PortSide.Left)
                    }

                    // arrow (default)
                    connect("node_1", "right") linksTo connect("node_2", "left")
                    // circle target
                    connect("node_2", "right") linksTo connect("node_3", "left")
                    // diamond target
                    connect("node_3", "right") linksTo connect("node_4", "left")
                }
            }

            // Override edge decorations after build to showcase all types
            val canvasWithDecorations = remember(initialCanvas) {
                val edges = initialCanvas.edges.values.toList()
                var result = initialCanvas
                // edge 0: solid + arrow, no animation
                edges.getOrNull(0)?.let { e ->
                    result = result.copy(edges = result.edges + (e.id to e.copy(
                        sourceEnd = EdgeEnd.None, targetEnd = EdgeEnd.Arrow(size = 10f),
                        stroke = EdgeStroke.Solid(width = 2.5f),
                        animation = EdgeAnimation.None,
                    )))
                }
                // edge 1: dashed + circle + marching ants
                edges.getOrNull(1)?.let { e ->
                    result = result.copy(edges = result.edges + (e.id to e.copy(
                        sourceEnd = EdgeEnd.None, targetEnd = EdgeEnd.Circle(radius = 6f),
                        stroke = EdgeStroke.Dashed(width = 2f, dashLength = 10f, gapLength = 5f),
                        animation = EdgeAnimation.MarchingAnts(speedDpPerSecond = 60f),
                    )))
                }
                // edge 2: dotted + diamond + pulse (2 dots)
                edges.getOrNull(2)?.let { e ->
                    result = result.copy(edges = result.edges + (e.id to e.copy(
                        sourceEnd = EdgeEnd.None, targetEnd = EdgeEnd.Diamond(size = 7f),
                        stroke = EdgeStroke.Dotted(width = 3f, gapLength = 5f),
                        animation = EdgeAnimation.Pulse(dotRadius = 4f, durationMs = 1200, count = 2),
                    )))
                }
                result
            }

            val canvasState = rememberBlockCanvasState(
                initialCanvasState = canvasWithDecorations,
                gridConfig = GridConfig(
                    style = GridStyle.Dots(spacing = 24f),
                    snapToGrid = true,
                    backgroundColor = Color(0xFF_0F0F1A),
                )
            )

            BlockCanvas(
                state = canvasState,
                modifier = Modifier.fillMaxSize(),
                nodeContent = { node, isSelected ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF_1E1E2E), RoundedCornerShape(8.dp))
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else Color(0xFF_5B8DEF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = node.id.value, color = Color.White)

                        // Top port dot
                        Box(Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = -(PORT_DOT_DP / 2))
                            .size(PORT_DOT_DP)
                            .background(Color(0xFF_5B8DEF), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape))

                        // Right port dot
                        Box(Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = PORT_DOT_DP / 2)
                            .size(PORT_DOT_DP)
                            .background(Color(0xFF_5B8DEF), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape))

                        // Bottom port dot
                        Box(Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = PORT_DOT_DP / 2)
                            .size(PORT_DOT_DP)
                            .background(Color(0xFF_5B8DEF), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape))

                        // Left port dot
                        Box(Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = -(PORT_DOT_DP / 2))
                            .size(PORT_DOT_DP)
                            .background(Color(0xFF_5B8DEF), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape))
                    }
                }
            )

            // Overlay Controls
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                    Button(onClick = { canvasState.undo() }, enabled = canvasState.canUndo) { Text("Undo") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { canvasState.redo() }, enabled = canvasState.canRedo) { Text("Redo") }
                }

                Row(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)) {
                    Button(onClick = {
                        val current = canvasState.gridConfig.style
                        val next = when (current) {
                            is GridStyle.Dots -> GridStyle.Lines()
                            is GridStyle.Lines -> GridStyle.Crosses()
                            is GridStyle.Crosses -> GridStyle.None
                            is GridStyle.None -> GridStyle.Dots()
                        }
                        canvasState.gridConfig = canvasState.gridConfig.copy(style = next)
                    }) {
                        val styleName = when (canvasState.gridConfig.style) {
                            is GridStyle.Dots -> "Dots"
                            is GridStyle.Lines -> "Lines"
                            is GridStyle.Crosses -> "Crosses"
                            is GridStyle.None -> "None"
                        }
                        Text("Grid: $styleName")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = {
                        canvasState.gridConfig = canvasState.gridConfig.copy(
                            snapToGrid = !canvasState.gridConfig.snapToGrid
                        )
                    }) {
                        Text(if (canvasState.gridConfig.snapToGrid) "Disable Snap" else "Enable Snap")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { canvasState.snapAllToGrid() },
                        enabled = canvasState.gridConfig.snapToGrid,
                    ) {
                        Text("Snap All to Grid")
                    }
                }
            }
        }
    }
}
