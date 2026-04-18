package com.yasincidem.blockcanvas.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
public data class AlignmentGuideStyle(
    val lineColor: Color = Color(0xFFFF6B6B),
    val lineWidth: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
    ),
    val labelBackground: Color = Color(0xFFFF6B6B),
    val labelPaddingPx: Float = 4f,
) {
    public companion object {
        public val Default: AlignmentGuideStyle = AlignmentGuideStyle()
    }
}
