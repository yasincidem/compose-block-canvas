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
import com.yasincidem.blockcanvas.ui.state.GridType
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
                    node("node_1") {
                        at(x = gap, y = 200f * density)
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
                    }

                    connect("node_1", "right") linksTo connect("node_2", "left")
                    connect("node_2", "right") linksTo connect("node_3", "left")
                }
            }

            val canvasState = rememberBlockCanvasState(
                initialCanvasState = initialCanvas,
                gridConfig = GridConfig(
                    type = GridType.Dots,
                    spacing = 20f,
                    snapToGrid = true,
                    backgroundColor = Color.Unspecified,
                    gridColor = Color.White.copy(alpha = 0.1f)
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
                        canvasState.gridConfig = canvasState.gridConfig.copy(
                            type = if (canvasState.gridConfig.type == GridType.Dots) GridType.Lines else GridType.Dots
                        )
                    }) {
                        Text(if (canvasState.gridConfig.type == GridType.Dots) "Switch to Lines" else "Switch to Dots")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = {
                        canvasState.gridConfig = canvasState.gridConfig.copy(
                            snapToGrid = !canvasState.gridConfig.snapToGrid
                        )
                    }) {
                        Text(if (canvasState.gridConfig.snapToGrid) "Disable Snap" else "Enable Snap")
                    }
                }
            }
        }
    }
}
