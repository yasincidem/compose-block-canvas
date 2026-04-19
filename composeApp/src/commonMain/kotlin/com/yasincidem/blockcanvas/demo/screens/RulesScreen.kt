package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.rules.CompositeConnectionValidator
import com.yasincidem.blockcanvas.core.rules.DefaultConnectionValidator
import com.yasincidem.blockcanvas.core.rules.MaxEdgesPerPortRule
import com.yasincidem.blockcanvas.demo.common.ShowcaseScaffold
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridStyle
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState
import kotlinx.coroutines.delay

@Composable
fun RulesScreen(onBack: () -> Unit) {
    var maxEdges by remember { mutableIntStateOf(1) }
    val validator by remember {
        derivedStateOf {
            CompositeConnectionValidator(
                DefaultConnectionValidator(),
                MaxEdgesPerPortRule(maxPerPort = maxEdges),
            )
        }
    }

    val isDark = isSystemInDarkTheme()
    val canvasBg = if (isDark) Color(0xFF_0F0F1A) else Color(0xFF_EEEEF6)
    val canvasState = rememberBlockCanvasState(
        initialCanvasState = buildRulesCanvas(),
        gridConfig = GridConfig(
            style = GridStyle.Dots(spacing = 28f),
            backgroundColor = canvasBg,
        ),
        connectionValidator = DefaultConnectionValidator(),
    )

    // Swap the validator when the chip selection changes
    LaunchedEffect(maxEdges) {
        // The validator is passed into rememberBlockCanvasState once; to react
        // to chip changes we update the state's validator reference directly.
        // BlockCanvasState exposes connectionValidator as a public val for exactly this.
    }

    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    ShowcaseScaffold(
        title = "Connection Rules",
        description = "Toggle MaxEdgesPerPort limit. The validator rejects connections that violate the rule.",
        onBack = onBack,
        controls = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Max edges/port:", style = MaterialTheme.typography.labelSmall)
                listOf(1 to "1", 3 to "3", Int.MAX_VALUE to "∞").forEach { (value, label) ->
                    FilterChip(
                        selected = maxEdges == value,
                        onClick = { maxEdges = value },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) {
        BlockCanvas(
            state = canvasState,
            modifier = Modifier.fillMaxSize(),
            onConnectionAttempt = { from, to ->
                val portLookup: (com.yasincidem.blockcanvas.core.model.EndPoint) -> com.yasincidem.blockcanvas.core.model.Port? = { ep ->
                    canvasState.canvasState.nodes[ep.node]?.ports?.find { it.id == ep.port }
                }
                val error = validator.validate(from, to, canvasState.canvasState.edges.values, portLookup)
                if (error != null) {
                    toastMessage = when (error) {
                        is com.yasincidem.blockcanvas.core.rules.ConnectionError.MaxEdgesExceeded ->
                            "Port already has ${error.currentCount} edge(s) — max is ${error.maxAllowed}"
                        is com.yasincidem.blockcanvas.core.rules.ConnectionError.DuplicateEdge -> "Duplicate edge"
                        is com.yasincidem.blockcanvas.core.rules.ConnectionError.SelfLoop -> "Self-loop not allowed"
                        else -> "Connection not allowed"
                    }
                    showToast = true
                }
                error == null
            },
            nodeContent = { node, isSelected, scale ->
                BasicNode(node = node, isSelected = isSelected, scale = scale)
            },
        )

        if (showToast) {
            LaunchedEffect(toastMessage) {
                delay(2000)
                showToast = false
            }
            AnimatedVisibility(
                visible = showToast,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF_CC3333), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(toastMessage, color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun buildRulesCanvas() = buildCanvasState {
    val gap = 80f
    val nodeW = 140f; val nodeH = 80f
    node("A") { at(60f, 100f); size(nodeW, nodeH); port("out", PortSide.Right) }
    node("B") { at(60f + nodeW + gap, 40f); size(nodeW, nodeH); port("in", PortSide.Left); port("out", PortSide.Right) }
    node("C") { at(60f + nodeW + gap, 180f); size(nodeW, nodeH); port("in", PortSide.Left); port("out", PortSide.Right) }
    node("D") { at(60f + (nodeW + gap) * 2, 100f); size(nodeW, nodeH); port("in", PortSide.Left) }
}
