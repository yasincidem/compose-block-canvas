package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.core.rules.CompositeConnectionValidator
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import com.yasincidem.blockcanvas.core.rules.MaxEdgesPerPortRule
import com.yasincidem.blockcanvas.ui.state.LodLevel
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

private val PORT_DOT_DP = 12.dp

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current.density
            val initialCanvas = remember(density) { buildDemoCanvas(density) }
            val canvasState = rememberBlockCanvasState(
                initialCanvasState = initialCanvas,
                gridConfig = GridConfig(
                    style = GridStyle.Dots(spacing = 24f),
                    snapToGrid = true,
                    backgroundColor = Color(0xFF_0F0F1A),
                ),
                connectionValidator = CompositeConnectionValidator(
                    DefaultConnectionValidator(),
                    MaxEdgesPerPortRule(maxPerPort = 1),
                ),
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val canvasWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val canvasHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

                BlockCanvas(
                    state = canvasState,
                    modifier = Modifier.fillMaxSize(),
                    nodeContent = { node, isSelected, scale ->
                        DemoNode(node = node, isSelected = isSelected, scale = scale)
                    }
                )

                DemoOverlay(
                    canvasState = canvasState,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                )
            }
        }
    }
}

@Composable
private fun DemoNode(node: Node, isSelected: Boolean, scale: Float) {
    val lod = LodLevel.of(scale)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_1E1E2E), RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White else Color(0xFF_5B8DEF),
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (lod >= LodLevel.MEDIUM) {
            Text(text = node.id.value, color = Color.White)
            node.ports.forEach { port -> PortDot(port.side) }
        }
    }
}

@Composable
private fun BoxScope.PortDot(side: PortSide) {
    val alignment = when (side) {
        PortSide.Top    -> Alignment.TopCenter
        PortSide.Right  -> Alignment.CenterEnd
        PortSide.Bottom -> Alignment.BottomCenter
        PortSide.Left   -> Alignment.CenterStart
    }
    val modifier = when (side) {
        PortSide.Top    -> Modifier.align(alignment).offset(y = -(PORT_DOT_DP / 2))
        PortSide.Right  -> Modifier.align(alignment).offset(x = PORT_DOT_DP / 2)
        PortSide.Bottom -> Modifier.align(alignment).offset(y = PORT_DOT_DP / 2)
        PortSide.Left   -> Modifier.align(alignment).offset(x = -(PORT_DOT_DP / 2))
    }
    Box(
        modifier
            .size(PORT_DOT_DP)
            .background(Color(0xFF_5B8DEF), CircleShape)
            .border(1.5.dp, Color.White, CircleShape)
    )
}

@Composable
private fun DemoOverlay(
    canvasState: BlockCanvasState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        UndoRedoBar(
            canvasState = canvasState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )

        GridControlBar(
            canvasState = canvasState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        )

        ZoomControls(
            state = canvasState,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
        )

        FitButton(
            onClick = { canvasState.fitToNodes(canvasWidthPx, canvasHeightPx) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 72.dp),
        )
    }
}

@Composable
private fun FitButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color(0xFF_2A2A3E),
            contentColor = Color.White,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text("⊡", fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp))
    }
}

@Composable
private fun UndoRedoBar(canvasState: BlockCanvasState, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Button(onClick = { canvasState.undo() }, enabled = canvasState.canUndo) { Text("Undo") }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = { canvasState.redo() }, enabled = canvasState.canRedo) { Text("Redo") }
    }
}

@Composable
private fun GridControlBar(canvasState: BlockCanvasState, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        GridStyleButton(canvasState = canvasState)
        Spacer(Modifier.width(16.dp))
        SnapToggleButton(canvasState = canvasState)
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = { canvasState.snapAllToGrid() },
            enabled = canvasState.gridConfig.snapToGrid,
        ) {
            Text("Snap All")
        }
    }
}

@Composable
private fun GridStyleButton(canvasState: BlockCanvasState) {
    val styleName = when (canvasState.gridConfig.style) {
        is GridStyle.Dots -> "Dots"
        is GridStyle.Lines -> "Lines"
        is GridStyle.Crosses -> "Crosses"
        is GridStyle.None -> "None"
    }
    Button(onClick = {
        val next = when (canvasState.gridConfig.style) {
            is GridStyle.Dots -> GridStyle.Lines()
            is GridStyle.Lines -> GridStyle.Crosses()
            is GridStyle.Crosses -> GridStyle.None
            is GridStyle.None -> GridStyle.Dots()
        }
        canvasState.gridConfig = canvasState.gridConfig.copy(style = next)
    }) {
        Text("Grid: $styleName")
    }
}

@Composable
private fun SnapToggleButton(canvasState: BlockCanvasState) {
    Button(onClick = {
        canvasState.gridConfig = canvasState.gridConfig.copy(
            snapToGrid = !canvasState.gridConfig.snapToGrid
        )
    }) {
        Text(if (canvasState.gridConfig.snapToGrid) "Snap: On" else "Snap: Off")
    }
}

private fun buildDemoCanvas(density: Float): CanvasState {
    val nodeW = 150f * density
    val nodeH = 80f * density
    val gap = 60f * density

    return buildCanvasState {
        node("node_1") {
            at(x = gap, y = 160f * density)
            size(nodeW, nodeH)
            port("top", PortSide.Top)
            port("right", PortSide.Right)
            port("bottom", PortSide.Bottom)
            port("left", PortSide.Left)
        }
        node("node_2") {
            rightOf("node_1", gap = gap)
            size(nodeW, nodeH)
            port("top", PortSide.Top)
            port("right", PortSide.Right)
            port("bottom", PortSide.Bottom)
            port("left", PortSide.Left)
        }
        node("node_3") {
            rightOf("node_2", gap = gap)
            size(nodeW, nodeH)
            port("top", PortSide.Top)
            port("right", PortSide.Right)
            port("bottom", PortSide.Bottom)
            port("left", PortSide.Left)
        }
        node("node_4") {
            rightOf("node_3", gap = gap)
            size(nodeW, nodeH)
            port("top", PortSide.Top)
            port("right", PortSide.Right)
            port("bottom", PortSide.Bottom)
            port("left", PortSide.Left)
        }

        connect("node_1", "right") linksTo connect("node_2", "left") style {
            targetEnd = EdgeEnd.Arrow(size = 10f)
            stroke = EdgeStroke.Solid(width = 2.5f)
        }
        connect("node_2", "right") linksTo connect("node_3", "left") style {
            targetEnd = EdgeEnd.Circle(radius = 6f)
            stroke = EdgeStroke.Dashed(width = 2f, dashLength = 10f, gapLength = 5f)
            animation = EdgeAnimation.MarchingAnts(speedDpPerSecond = 60f)
        }
        connect("node_3", "right") linksTo connect("node_4", "left") style {
            targetEnd = EdgeEnd.Diamond(size = 7f)
            stroke = EdgeStroke.Dotted(width = 3f, gapLength = 5f)
            animation = EdgeAnimation.Pulse(dotRadius = 4f, durationMs = 1200, count = 2)
        }
    }
}
