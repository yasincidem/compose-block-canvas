package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

@Composable
fun CustomNodesScreen(onBack: () -> Unit) {
    val canvasState = rememberBlockCanvasState(
        initialCanvasState = buildCustomNodesCanvas(),
        gridConfig = GridConfig(
            style = GridStyle.Lines(),
            backgroundColor = Color(0xFF_12121C),
        ),
        connectionValidator = DefaultConnectionValidator(),
    )

    ShowcaseScaffold(
        title = "Custom Nodes",
        description = "Every node is rendered via the nodeContent slot — the library imposes no constraints on node UI.",
        onBack = onBack,
    ) {
        BlockCanvas(
            state = canvasState,
            modifier = Modifier.fillMaxSize(),
            nodeContent = { node, isSelected, _ ->
                CustomNodeContent(node = node, isSelected = isSelected)
            },
        )
    }
}

@Composable
private fun BoxScope.CustomNodeContent(node: Node, isSelected: Boolean) {
    val borderColor = if (isSelected) Color.White else Color(0xFF_444466)
    when (node.id) {
        NodeId("text")      -> TextNodeContent(node, borderColor)
        NodeId("image")     -> ImageNodeContent(node, borderColor)
        NodeId("longtext")  -> LongTextNodeContent(node, borderColor)
        NodeId("form")      -> FormNodeContent(node, borderColor)
        NodeId("richcard")  -> RichCardNodeContent(node, borderColor)
        else                -> BasicNode(node, isSelected, 1f)
    }
    // Render port dots for all nodes
    node.ports.forEach { port -> BasicPortDot(port.side) }
}

@Composable
private fun TextNodeContent(node: Node, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_1E1E2E), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Column {
            Text("Text Node", style = MaterialTheme.typography.titleSmall, color = Color(0xFF_5B8DEF))
            Spacer(Modifier.height(4.dp))
            Text("Plain title + subtitle layout", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ImageNodeContent(node: Node, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_1E1E2E), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/blockcanvas/400/200",
            contentDescription = "Image node",
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp),
        ) {
            Text("AsyncImage via Coil 3", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LongTextNodeContent(node: Node, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_1E2E1E), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Long Text Node", style = MaterialTheme.typography.titleSmall, color = Color(0xFF_5BE28D))
            Text(
                text = "This node contains scrollable content. The library places no constraints on what you put inside — interactive composables just work.",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "You can embed lists, markdown renderers, data tables, or any Composable here. Scroll is contained within the node boundary.",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FormNodeContent(node: Node, borderColor: Color) {
    var text by remember { mutableStateOf("") }
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var switchOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_2E1E2E), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Form Node", style = MaterialTheme.typography.titleSmall, color = Color(0xFF_E25BE2))
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Type here…", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${(sliderValue * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                Slider(value = sliderValue, onValueChange = { sliderValue = it }, modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Switch(checked = switchOn, onCheckedChange = { switchOn = it })
            }
        }
    }
}

@Composable
private fun RichCardNodeContent(node: Node, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_1E2A2E), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF_5BE2D8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("YC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Rich Card Node", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("avatar · tags · button", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AssistChip(onClick = {}, label = { Text("Kotlin", fontSize = 10.sp) })
                AssistChip(onClick = {}, label = { Text("Compose", fontSize = 10.sp) })
            }
        }
    }
}

private fun buildCustomNodesCanvas(): CanvasState {
    val w = 220f
    val h = 160f
    val gap = 60f

    return buildCanvasState {
        node("text") {
            at(x = 40f, y = 60f)
            size(w, 80f)
            port("right", PortSide.Right)
            port("left", PortSide.Left)
        }
        node("image") {
            at(x = 40f + w + gap, y = 20f)
            size(w + 40f, h + 20f)
            port("right", PortSide.Right)
            port("left", PortSide.Left)
            port("bottom", PortSide.Bottom)
        }
        node("longtext") {
            at(x = 40f + (w + gap) * 2, y = 40f)
            size(w, h)
            port("left", PortSide.Left)
            port("right", PortSide.Right)
        }
        node("form") {
            at(x = 40f, y = 220f)
            size(w + 20f, h + 60f)
            port("right", PortSide.Right)
            port("top", PortSide.Top)
        }
        node("richcard") {
            at(x = 40f + w + gap, y = 260f)
            size(w, h)
            port("left", PortSide.Left)
            port("top", PortSide.Top)
        }

        connect("text", "right") linksTo connect("image", "left") style {
            targetEnd = EdgeEnd.Arrow()
        }
        connect("image", "right") linksTo connect("longtext", "left") style {
            targetEnd = EdgeEnd.Arrow()
        }
        connect("form", "right") linksTo connect("richcard", "left") style {
            targetEnd = EdgeEnd.Circle(radius = 5f)
        }
        connect("image", "bottom") linksTo connect("richcard", "top") style {
            targetEnd = EdgeEnd.Diamond(size = 6f)
        }
    }
}
