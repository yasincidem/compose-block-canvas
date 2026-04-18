package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Defines the type of grid pattern to display.
 */
@Immutable
public enum class GridType {
    None,
    Dots,
    Lines
}

/**
 * Configuration for the canvas background and grid behavior.
 */
@Immutable
public data class GridConfig(
    val type: GridType = GridType.Dots,
    val spacing: Float = 20f, // Screen pixels
    val snapToGrid: Boolean = false,
    val gridColor: Color = Color.White.copy(alpha = 0.15f),
    val backgroundColor: Color = Color(0xFF_0F0F1A),
) {
    public companion object {
        public val Default: GridConfig = GridConfig()
    }
}
