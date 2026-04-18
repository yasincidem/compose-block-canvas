package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import kotlinx.coroutines.delay

private val BUTTON_SIZE = 40.dp
private val BUTTON_GAP = 8.dp
private const val HOLD_INITIAL_DELAY_MS = 400L
private const val HOLD_REPEAT_INTERVAL_MS = 80L

@Composable
fun ZoomControls(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    buttonSize: Dp = BUTTON_SIZE,
    anchor: Offset = Offset.Zero,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BUTTON_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ZoomButton(
            label = "+",
            size = buttonSize,
            onPress = { state.zoomIn(anchor = anchor) },
        )
        ZoomButton(
            label = "−",
            size = buttonSize,
            onPress = { state.zoomOut(anchor = anchor) },
        )
    }
}

@Composable
private fun ZoomButton(
    label: String,
    size: Dp,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onPress()
            delay(HOLD_INITIAL_DELAY_MS)
            while (true) {
                onPress()
                delay(HOLD_REPEAT_INTERVAL_MS)
            }
        }
    }

    Button(
        onClick = {},
        modifier = modifier.size(size),
        shape = CircleShape,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF_2A2A3E),
            contentColor = Color.White,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(label, fontSize = 20.sp)
    }
}
