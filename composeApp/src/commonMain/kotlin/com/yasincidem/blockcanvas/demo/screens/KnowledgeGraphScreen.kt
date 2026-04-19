package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

private data class Concept(val id: String, val x: Float, val y: Float)
private data class Relation(val from: String, val fromPort: String, val to: String, val toPort: String)

private val concepts = listOf(
    Concept("Kotlin",         300f,  50f),
    Concept("JVM",            520f, 150f),
    Concept("Coroutines",      80f, 150f),
    Concept("Flow",            80f, 280f),
    Concept("Compose",        300f, 280f),
    Concept("KMP",            520f, 280f),
    Concept("Android",        300f, 420f),
    Concept("Desktop",        520f, 420f),
    Concept("iOS",             80f, 420f),
    Concept("Ktor",           740f, 150f),
    Concept("Serialization",  740f, 280f),
)

private val relations = listOf(
    Relation("Kotlin", "r",  "Coroutines",    "t"),
    Relation("Kotlin", "r",  "JVM",           "l"),
    Relation("Kotlin", "r",  "KMP",           "t"),
    Relation("Coroutines", "b", "Flow",       "t"),
    Relation("Kotlin", "b",  "Compose",       "t"),
    Relation("Compose", "b", "Android",       "t"),
    Relation("Compose", "r", "Desktop",       "l"),
    Relation("KMP", "b",     "iOS",           "r"),
    Relation("KMP", "b",     "Android",       "r"),
    Relation("KMP", "r",     "Desktop",       "t"),
    Relation("Kotlin", "r",  "Ktor",          "l"),
    Relation("Kotlin", "r",  "Serialization", "l"),
)

@Composable
fun KnowledgeGraphScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF_0D0D16) else Color(0xFF_F0F0F8)
    val canvasState = rememberBlockCanvasState(
        initialCanvasState = buildKnowledgeGraph(),
        gridConfig = GridConfig(
            style = GridStyle.None,
            backgroundColor = canvasBg,
        ),
    )
    SideEffect { canvasState.gridConfig = canvasState.gridConfig.copy(backgroundColor = canvasBg) }

    ShowcaseScaffold(
        title = "Knowledge Graph",
        description = "Concept map of Kotlin's ecosystem. Pill-shaped nodes, read-only layout.",
        onBack = onBack,
    ) {
        BlockCanvas(
            state = canvasState,
            modifier = Modifier.fillMaxSize(),
            nodeContent = { node, isSelected, _ ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (node.id.value == "Kotlin") Color(0xFF_7F52FF) else Color(0xFF_1E1E32),
                            RoundedCornerShape(50),
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF_7F52FF).copy(alpha = 0.5f),
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = node.id.value, color = Color.White, fontSize = 13.sp)
                }
            },
        )
    }
}

private fun buildKnowledgeGraph(): CanvasState {
    return buildCanvasState {
        concepts.forEach { c ->
            node(c.id) {
                at(x = c.x, y = c.y)
                size(c.id.length * 9f + 28f, 36f)
                port("l", PortSide.Left)
                port("r", PortSide.Right)
                port("t", PortSide.Top)
                port("b", PortSide.Bottom)
            }
        }
        relations.forEachIndexed { i, rel ->
            connect(rel.from, rel.fromPort) linksTo connect(rel.to, rel.toPort) style {
                targetEnd = EdgeEnd.Arrow(size = 7f)
                stroke = EdgeStroke.Solid(width = 1.5f)
            }
        }
    }
}
