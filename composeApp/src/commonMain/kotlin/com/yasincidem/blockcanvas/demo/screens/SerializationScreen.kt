package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val prettyJson = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }

@Composable
fun SerializationScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF_0F0F1A) else Color(0xFF_EEEEF6)
    val canvasState = rememberBlockCanvasState(
        initialCanvasState = buildSerializationCanvas(),
        gridConfig = GridConfig(
            style = GridStyle.Dots(spacing = 24f),
            backgroundColor = canvasBg,
        ),
    )
    SideEffect { canvasState.gridConfig = canvasState.gridConfig.copy(backgroundColor = canvasBg) }

    var exportedJson by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ShowcaseScaffold(
            title = "Serialization",
            description = "Export canvas state to JSON and re-import it. The state round-trips perfectly.",
            onBack = onBack,
            controls = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        exportedJson = prettyJson.encodeToString(canvasState.canvasState)
                        importError = ""
                    }) { Text("Export JSON") }

                    Button(
                        onClick = {
                            try {
                                val imported = prettyJson.decodeFromString<CanvasState>(exportedJson)
                                canvasState.canvasState.nodes.keys.forEach { canvasState.removeNode(it) }
                                imported.nodes.values.forEach { canvasState.addNode(it) }
                                imported.edges.values.forEach { canvasState.addEdge(it) }
                                importError = "✓ Imported ${imported.nodes.size} nodes, ${imported.edges.size} edges"
                            } catch (e: Exception) {
                                importError = "Import failed: ${e.message}"
                            }
                        },
                        enabled = exportedJson.isNotBlank(),
                    ) { Text("Import JSON") }
                }
            },
        ) {
            BlockCanvas(
                state = canvasState,
                modifier = Modifier.fillMaxSize(),
                nodeContent = { node, isSelected, scale ->
                    BasicNode(node = node, isSelected = isSelected, scale = scale)
                },
            )
        }

        if (exportedJson.isNotBlank() || importError.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF_111120))
                    .padding(8.dp),
            ) {
                if (importError.isNotBlank()) {
                    Text(
                        text = importError,
                        color = if (importError.startsWith("✓")) Color(0xFF5BE28D) else Color(0xFFCC4444),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                if (exportedJson.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color(0xFF_333355), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = exportedJson,
                            color = Color(0xFF_AACCFF),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

private fun buildSerializationCanvas() = buildCanvasState {
    node("alpha") { at(60f, 80f); size(120f, 70f); port("out", PortSide.Right) }
    node("beta")  { at(260f, 80f); size(120f, 70f); port("in", PortSide.Left); port("out", PortSide.Right) }
    node("gamma") { at(460f, 80f); size(120f, 70f); port("in", PortSide.Left) }
    connect("alpha", "out") linksTo connect("beta", "in") style { targetEnd = EdgeEnd.Arrow() }
    connect("beta", "out")  linksTo connect("gamma", "in") style { targetEnd = EdgeEnd.Arrow() }
}
