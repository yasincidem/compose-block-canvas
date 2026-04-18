package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
public sealed interface GridStyle {
    public val spacing: Float            // world-space spacing between cells
    public val color: Color
    public val majorEvery: Int           // every Nth line is "major"; 0 = no subdivision
    public val majorColor: Color

    @Immutable
    public data class Dots(
        override val spacing: Float = 24f,
        override val color: Color = Color.LightGray.copy(alpha = 0.3f),
        override val majorEvery: Int = 5,
        override val majorColor: Color = Color.LightGray.copy(alpha = 0.6f),
        val dotRadius: Float = 1.5f,
    ) : GridStyle

    @Immutable
    public data class Lines(
        override val spacing: Float = 24f,
        override val color: Color = Color.LightGray.copy(alpha = 0.15f),
        override val majorEvery: Int = 5,
        override val majorColor: Color = Color.LightGray.copy(alpha = 0.35f),
        val lineWidth: Float = 1f,
    ) : GridStyle

    @Immutable
    public data class Crosses(
        override val spacing: Float = 24f,
        override val color: Color = Color.LightGray.copy(alpha = 0.4f),
        override val majorEvery: Int = 0,
        override val majorColor: Color = color,
        val size: Float = 3f,
        val thickness: Float = 1f,
    ) : GridStyle

    @Immutable
    public object None : GridStyle {
        override val spacing: Float = 24f
        override val color: Color = Color.Transparent
        override val majorEvery: Int = 0
        override val majorColor: Color = Color.Transparent
    }
}
