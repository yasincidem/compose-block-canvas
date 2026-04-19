package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.rules.ConnectionError
import com.yasincidem.blockcanvas.core.rules.ConnectionValidator
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

// Port type metadata stored in port id with a prefix convention: "text_in", "vec_out", etc.
private enum class PortType { Text, Vector, Document, Any }

private fun PortId.portType(): PortType = when {
    value.startsWith("text")   -> PortType.Text
    value.startsWith("vec")    -> PortType.Vector
    value.startsWith("doc")    -> PortType.Document
    else                        -> PortType.Any
}

private fun PortType.color(): Color = when (this) {
    PortType.Text     -> Color(0xFF5B8DEF)
    PortType.Vector   -> Color(0xFFBB5BE2)
    PortType.Document -> Color(0xFF5BE28D)
    PortType.Any      -> Color(0xFFFFAA44)
}

/** Allows connection only when the port types on both ends are compatible. */
private class TypedPortValidator : ConnectionValidator {
    private val default = DefaultConnectionValidator()

    override fun validate(
        from: EndPoint,
        to: EndPoint,
        existingEdges: Collection<Edge>,
        portLookup: (EndPoint) -> Port?,
    ): ConnectionError? {
        default.validate(from, to, existingEdges, portLookup)?.let { return it }
        val fromType = from.port.portType()
        val toType   = to.port.portType()
        val compatible = fromType == PortType.Any || toType == PortType.Any || fromType == toType
        return if (compatible) null else ConnectionError.PortNotFound(to)
    }
}

@Composable
fun WorkflowScreen(onBack: () -> Unit) {
    val canvasState = rememberBlockCanvasState(
        initialCanvasState = buildWorkflowCanvas(),
        gridConfig = GridConfig(
            style = GridStyle.Lines(spacing = 32f),
            backgroundColor = Color(0xFF_0A0A14),
        ),
        connectionValidator = TypedPortValidator(),
    )

    ShowcaseScaffold(
        title = "Workflow — AI Pipeline",
        description = "Typed ports: 🔵 Text  🟣 Vector  🟢 Doc. Only matching types connect. Edges show marching-ant animation.",
        onBack = onBack,
    ) {
        BlockCanvas(
            state = canvasState,
            modifier = Modifier.fillMaxSize(),
            nodeContent = { node, isSelected, _ ->
                WorkflowNodeContent(node = node, isSelected = isSelected)
            },
        )
    }
}

@Composable
private fun BoxScope.WorkflowNodeContent(node: Node, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF_171726), RoundedCornerShape(10.dp))
            .border(
                1.5.dp,
                if (isSelected) Color.White else Color(0xFF_333355),
                RoundedCornerShape(10.dp),
            )
            .padding(10.dp),
    ) {
        val (label, subtitle) = workflowNodeMeta(node.id)
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        }
    }
    node.ports.forEach { port ->
        val alignment = when (port.side) {
            PortSide.Left   -> Alignment.CenterStart
            PortSide.Right  -> Alignment.CenterEnd
            PortSide.Top    -> Alignment.TopCenter
            PortSide.Bottom -> Alignment.BottomCenter
        }
        val offsetModifier = when (port.side) {
            PortSide.Left   -> Modifier.align(alignment).offset(x = (-6).dp)
            PortSide.Right  -> Modifier.align(alignment).offset(x = 6.dp)
            PortSide.Top    -> Modifier.align(alignment).offset(y = (-6).dp)
            PortSide.Bottom -> Modifier.align(alignment).offset(y = 6.dp)
        }
        val portColor = port.id.portType().color()
        Box(
            offsetModifier
                .size(12.dp)
                .background(portColor, CircleShape)
                .border(1.5.dp, Color.White, CircleShape),
        )
    }
}

private fun workflowNodeMeta(id: NodeId): Pair<String, String> = when (id.value) {
    "input"    -> "📥 Input"      to "text prompt"
    "embed"    -> "🔢 Embed"      to "text → vector"
    "retrieve" -> "🔍 Retrieve"   to "vector → docs"
    "generate" -> "✨ Generate"   to "text + docs → text"
    "output"   -> "📤 Output"     to "final response"
    else       -> id.value        to ""
}

private fun buildWorkflowCanvas(): CanvasState {
    val w = 160f; val h = 80f; val gap = 60f
    var x = 40f
    val y = 120f

    val nodes = mutableListOf<Node>()
    val edges = mutableListOf<Edge>()

    fun step(id: String, outPort: PortId, inPort: PortId, prevId: String? = null): Node {
        val ports = buildList {
            if (prevId != null) add(Port(inPort, PortSide.Left))
            if (id != "output") add(Port(outPort, PortSide.Right))
        }
        val node = Node(NodeId(id), com.yasincidem.blockcanvas.core.geometry.Offset(x, y), w, h, ports)
        nodes.add(node)
        if (prevId != null && nodes.size > 1) {
            val prev = nodes[nodes.size - 2]
            val prevOutPort = prev.ports.first { it.side == PortSide.Right }
            edges.add(
                Edge(
                    id = EdgeId("e_${prevId}_${id}"),
                    from = EndPoint(prev.id, prevOutPort.id),
                    to = EndPoint(node.id, inPort),
                    animation = EdgeAnimation.MarchingAnts(speedDpPerSecond = 80f),
                    targetEnd = EdgeEnd.Arrow(size = 8f),
                )
            )
        }
        x += w + gap
        return node
    }

    step("input",    PortId("text_out"),   PortId("text_in"))
    step("embed",    PortId("vec_out"),    PortId("text_in"),  prevId = "input")
    step("retrieve", PortId("doc_out"),    PortId("vec_in"),   prevId = "embed")
    step("generate", PortId("text_out2"),  PortId("doc_in"),   prevId = "retrieve")
    step("output",   PortId("_none"),      PortId("text_in2"), prevId = "generate")

    var state = CanvasState()
    nodes.forEach { state = state.addNode(it) }
    edges.forEach { state = state.addEdge(it) }
    return state
}
