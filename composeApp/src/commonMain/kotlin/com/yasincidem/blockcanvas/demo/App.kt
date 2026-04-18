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
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortDirection
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

private val PORT_DOT_SIZE = 12.dp

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val initialCanvas = remember {
                CanvasState()
                    .addNode(Node(
                        id = NodeId("node_1"),
                        position = Offset(80f, 120f),
                        width = 150f,
                        height = 80f,
                        ports = listOf(
                            Port(PortId("out_1"), PortDirection.Out),
                        )
                    ))
                    .addNode(Node(
                        id = NodeId("node_2"),
                        position = Offset(380f, 200f),
                        width = 150f,
                        height = 80f,
                        ports = listOf(
                            Port(PortId("in_1"), PortDirection.In),
                            Port(PortId("out_1"), PortDirection.Out),
                        )
                    ))
                    .addNode(Node(
                        id = NodeId("node_3"),
                        position = Offset(680f, 120f),
                        width = 150f,
                        height = 80f,
                        ports = listOf(
                            Port(PortId("in_1"), PortDirection.In),
                        )
                    ))
                    .addEdge(Edge(
                        id = EdgeId("edge_1"),
                        from = EndPoint(NodeId("node_1"), PortId("out_1")),
                        to   = EndPoint(NodeId("node_2"), PortId("in_1"))
                    ))
                    .addEdge(Edge(
                        id = EdgeId("edge_2"),
                        from = EndPoint(NodeId("node_2"), PortId("out_1")),
                        to   = EndPoint(NodeId("node_3"), PortId("in_1"))
                    ))
            }

            val canvasState = rememberBlockCanvasState(initialCanvasState = initialCanvas)

            BlockCanvas(
                state = canvasState,
                modifier = Modifier.fillMaxSize(),
                portPosition = { nodeId, portId ->
                    val node = canvasState.canvasState.nodes[nodeId] ?: return@BlockCanvas null
                    val port = node.ports.find { it.id == portId } ?: return@BlockCanvas null
                    when (port.direction) {
                        PortDirection.Out -> Offset(node.position.x + node.width, node.position.y + node.height / 2)
                        PortDirection.In  -> Offset(node.position.x,              node.position.y + node.height / 2)
                    }
                },
                nodeContent = { node ->
                    Box(
                        modifier = Modifier
                            .size(width = node.width.dp, height = node.height.dp)
                            .background(Color(0xFF_1E1E2E), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF_5B8DEF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = node.id.value, color = Color.White)

                        // In-port dot (left edge, vertically centred)
                        node.ports.filter { it.direction == PortDirection.In }.forEachIndexed { _, _ ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(x = (-PORT_DOT_SIZE / 2))
                                    .size(PORT_DOT_SIZE)
                                    .background(Color(0xFF_5B8DEF), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        // Out-port dot (right edge, vertically centred)
                        node.ports.filter { it.direction == PortDirection.Out }.forEachIndexed { _, _ ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = (PORT_DOT_SIZE / 2))
                                    .size(PORT_DOT_SIZE)
                                    .background(Color(0xFF_FF7B72), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            )
        }
    }
}
