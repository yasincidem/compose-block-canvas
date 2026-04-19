package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.demo.nav.DemoTopBar
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

private data class Theme(
    val name: String,
    val bg: Color,
    val nodeBg: Color,
    val nodeBorder: Color,
    val textColor: Color,
    val portColor: Color,
    val gridConfig: GridConfig,
)

private val darkTheme = Theme(
    name = "Dark",
    bg = Color(0xFF_0F0F1A),
    nodeBg = Color(0xFF_1E1E2E),
    nodeBorder = Color(0xFF_5B8DEF),
    textColor = Color.White,
    portColor = Color(0xFF_5B8DEF),
    gridConfig = GridConfig(style = GridStyle.Dots(spacing = 24f), backgroundColor = Color(0xFF_0F0F1A)),
)

private val lightTheme = Theme(
    name = "Light",
    bg = Color(0xFF_F5F5F0),
    nodeBg = Color.White,
    nodeBorder = Color(0xFF_3366CC),
    textColor = Color(0xFF_111133),
    portColor = Color(0xFF_3366CC),
    gridConfig = GridConfig(style = GridStyle.Lines(color = Color(0x22_000000)), backgroundColor = Color(0xFF_F5F5F0)),
)

private val neonTheme = Theme(
    name = "Neon",
    bg = Color(0xFF_000008),
    nodeBg = Color(0xFF_080818),
    nodeBorder = Color(0xFF_FF00FF),
    textColor = Color(0xFF_00FFFF),
    portColor = Color(0xFF_FF00FF),
    gridConfig = GridConfig(style = GridStyle.Lines(color = Color(0x22_FF00FF)), backgroundColor = Color(0xFF_000008)),
)

private val themes = listOf(darkTheme, lightTheme, neonTheme)

@Composable
fun ThemingScreen(onBack: () -> Unit) {
    val density = LocalDensity.current.density
    val canvas = remember(density) { buildThemingCanvas(density) }

    Column(Modifier.fillMaxSize()) {
        DemoTopBar(title = "Theming", onBack = onBack)
        Text(
            text = "Same graph, three different visual themes — all via public API.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            themes.forEach { theme ->
                ThemeCanvas(canvas = canvas, theme = theme, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ThemeCanvas(canvas: CanvasState, theme: Theme, modifier: Modifier = Modifier) {
    val state = rememberBlockCanvasState(
        initialCanvasState = canvas,
        gridConfig = theme.gridConfig,
    )
    Box(modifier = modifier.border(1.dp, theme.nodeBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))) {
        BlockCanvas(
            state = state,
            modifier = Modifier.fillMaxSize(),
            nodeContent = { node, isSelected, _ ->
                ThemedNode(node = node, isSelected = isSelected, theme = theme)
            },
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(theme.nodeBorder.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(theme.name, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun BoxScope.ThemedNode(node: Node, isSelected: Boolean, theme: Theme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.nodeBg, RoundedCornerShape(6.dp))
            .border(1.dp, if (isSelected) Color.White else theme.nodeBorder, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = node.id.value, color = theme.textColor, style = MaterialTheme.typography.labelSmall)
    }
    node.ports.forEach { port ->
        val (align, xOff, yOff) = when (port.side) {
            PortSide.Left   -> Triple(Alignment.CenterStart, (-5).dp, 0.dp)
            PortSide.Right  -> Triple(Alignment.CenterEnd, 5.dp, 0.dp)
            PortSide.Top    -> Triple(Alignment.TopCenter, 0.dp, (-5).dp)
            PortSide.Bottom -> Triple(Alignment.BottomCenter, 0.dp, 5.dp)
        }
        Box(
            Modifier
                .align(align)
                .offset(x = xOff, y = yOff)
                .size(10.dp)
                .background(theme.portColor, CircleShape)
                .border(1.dp, Color.White, CircleShape),
        )
    }
}

private fun buildThemingCanvas(density: Float): CanvasState {
    val s = density
    return buildCanvasState {
        node("Alpha") { at(40f * s, 10f * s); size(100f * s, 50f * s); port("r", PortSide.Right) }
        node("Beta")  { at(200f * s, 10f * s); size(100f * s, 50f * s); port("l", PortSide.Left); port("r", PortSide.Right) }
        node("Gamma") { at(360f * s, 10f * s); size(100f * s, 50f * s); port("l", PortSide.Left) }
        connect("Alpha", "r") linksTo connect("Beta", "l") style { targetEnd = EdgeEnd.Arrow(); stroke = EdgeStroke.Solid(width = 2f) }
        connect("Beta", "r")  linksTo connect("Gamma", "l") style { targetEnd = EdgeEnd.Arrow(); stroke = EdgeStroke.Dashed(width = 2f); animation = EdgeAnimation.MarchingAnts() }
    }
}
