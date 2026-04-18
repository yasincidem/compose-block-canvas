package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yasincidem.blockcanvas.ui.BlockCanvas
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
                CanvasState()
                    .addNode(fourPortNode("node_1", gap,             200f * density, nodeW, nodeH))
                    .addNode(fourPortNode("node_2", gap + nodeW + gap, 200f * density, nodeW, nodeH))
                    .addNode(fourPortNode("node_3", gap + (nodeW + gap) * 2, 200f * density, nodeW, nodeH))
                    .addEdge(Edge(EdgeId("e1"),
                        from = EndPoint(NodeId("node_1"), PortId("right")),
                        to   = EndPoint(NodeId("node_2"), PortId("left"))))
                    .addEdge(Edge(EdgeId("e2"),
                        from = EndPoint(NodeId("node_2"), PortId("right")),
                        to   = EndPoint(NodeId("node_3"), PortId("left"))))
            }

            val canvasState = rememberBlockCanvasState(initialCanvasState = initialCanvas)

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
        }
    }
}
