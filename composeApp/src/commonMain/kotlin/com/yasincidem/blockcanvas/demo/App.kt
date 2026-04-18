package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val initialCanvas = remember {
                CanvasState()
                    .addNode(
                        Node(
                            id = NodeId("node_1"),
                            position = Offset(0f, 0f),
                            width = 150f,
                            height = 80f,
                            ports = listOf(
                                Port(PortId("out_1"), PortDirection.Out),
                                Port(PortId("in_1"), PortDirection.In)
                            )
                        )
                    )
                    .addNode(
                        Node(
                            id = NodeId("node_2"),
                            position = Offset(300f, 250f),
                            width = 150f,
                            height = 150f,
                            ports = listOf(
                                Port(PortId("in_1"), PortDirection.In),
                                Port(PortId("out_1"), PortDirection.Out)
                            )
                        )
                    )
                    .addEdge(
                        Edge(
                            id = EdgeId("edge_1"),
                            from = EndPoint(NodeId("node_1"), PortId("out_1")),
                            to = EndPoint(NodeId("node_2"), PortId("in_1"))
                        )
                    )
            }
            
            val canvasState = rememberBlockCanvasState(initialCanvasState = initialCanvas)

            BlockCanvas(
                state = canvasState,
                modifier = Modifier.fillMaxSize(),
                nodeContent = { node ->
                    Box(
                        modifier = Modifier
                            .size(width = node.width.dp, height = node.height.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Node: ${node.id.value}",
                            color = Color.Black
                        )
                    }
                }
            )
        }
    }
}
