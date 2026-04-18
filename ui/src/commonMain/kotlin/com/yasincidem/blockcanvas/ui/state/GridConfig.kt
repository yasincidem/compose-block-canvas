package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Configuration for the canvas background and grid behavior.
 */
@Immutable
public data class GridConfig(
    val style: GridStyle = GridStyle.Dots(),
    val snapToGrid: Boolean = false,
    val backgroundColor: Color = Color(0xFF_0F0F1A),
) {
    public companion object {
        public val Default: GridConfig = GridConfig()
    }
}
