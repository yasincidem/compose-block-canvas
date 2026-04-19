package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
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
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState
import kotlin.math.sqrt

@Composable
fun PerformanceScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF_080810) else Color(0xFF_F0F0F8)
    var nodeCount by remember { mutableStateOf(0) }
    val canvasState = rememberBlockCanvasState(
        gridConfig = GridConfig(
            style = GridStyle.None,
            backgroundColor = canvasBg,
        ),
    )
    SideEffect { canvasState.gridConfig = canvasState.gridConfig.copy(backgroundColor = canvasBg) }

    // FPS sampler
    var fps by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastMs = 0L
        var frames = 0
        while (true) {
            withFrameMillis { ms ->
                frames++
                val elapsed = ms - lastMs
                if (elapsed >= 500) {
                    fps = frames * 1000L / elapsed
                    frames = 0
                    lastMs = ms
                }
            }
        }
    }

    ShowcaseScaffold(
        title = "Performance",
        description = "Stress test: spawn nodes and measure frame rate.",
        onBack = onBack,
        controls = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100, 500, 1000).forEach { count ->
                    Button(onClick = {
                        canvasState.canvasState.nodes.keys.toList().forEach { canvasState.removeNode(it) }
                        val (nodes, edges) = buildGrid(count)
                        nodes.forEach { canvasState.addNode(it) }
                        edges.forEach { canvasState.addEdge(it) }
                        nodeCount = count
                    }) { Text("$count") }
                }
            }
        },
    ) {
        BlockCanvas(
            state = canvasState,
            modifier = Modifier.fillMaxSize(),
            nodeContent = { node, isSelected, _ ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isSelected) Color(0xFF_5B8DEF) else Color(0xFF_1A1A2A),
                            RoundedCornerShape(4.dp),
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) Color.White else Color(0xFF_333355),
                            RoundedCornerShape(4.dp),
                        ),
                )
            },
        )

        // FPS counter overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF_5B8DEF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = if (nodeCount > 0) "$fps fps  •  $nodeCount nodes" else "Tap a button to spawn nodes",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun buildGrid(count: Int): Pair<List<Node>, List<Edge>> {
    val cols = sqrt(count.toFloat()).toInt().coerceAtLeast(1)
    val nodeW = 60f; val nodeH = 40f; val gapX = 30f; val gapY = 20f
    val nodes = mutableListOf<Node>()
    val edges  = mutableListOf<Edge>()
    var edgeIdx = 0

    repeat(count) { i ->
        val col = i % cols
        val row = i / cols
        val node = Node(
            id = NodeId("n$i"),
            position = Offset(col * (nodeW + gapX) + 40f, row * (nodeH + gapY) + 40f),
            width = nodeW,
            height = nodeH,
            ports = listOf(
                Port(PortId("l"), PortSide.Left),
                Port(PortId("r"), PortSide.Right),
            ),
        )
        nodes.add(node)

        // Connect to right neighbour (same row) with ~30% probability
        if (col < cols - 1 && i % 3 == 0) {
            val rightIdx = i + 1
            if (rightIdx < count) {
                edges.add(
                    Edge(
                        id = EdgeId("e${edgeIdx++}"),
                        from = EndPoint(NodeId("n$i"), PortId("r")),
                        to   = EndPoint(NodeId("n$rightIdx"), PortId("l")),
                    )
                )
            }
        }
    }

    return nodes to edges
}
