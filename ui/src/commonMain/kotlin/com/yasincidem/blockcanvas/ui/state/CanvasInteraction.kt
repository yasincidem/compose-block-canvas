package com.yasincidem.blockcanvas.ui.state

import androidx.compose.ui.graphics.Color
import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.EdgeId

/**
 * Defines how marquee selection interacts with the existing selection.
 */
public enum class MarqueeMode {
    /** New selection replaces the old one. */
    Replace,
    /** New selection is added to the old one (Shift held). */
    Add,
    /** New selection is removed from the old one (Alt held). */
    Subtract
}

/**
 * Visual style for the marquee selection rectangle.
 */
public data class MarqueeStyle(
    val fillColor: Color = Color(0x332196F3),
    val borderColor: Color = Color(0xFF2196F3),
    val borderDashPattern: FloatArray = floatArrayOf(5f, 5f)
)

/** Which endpoint of an edge is being dragged during a reconnect gesture. */
public enum class EdgeEndpoint { Source, Target }

/**
 * Represents the current high-level interaction state of the canvas.
 */
public sealed interface CanvasInteraction {
    /** No special interaction is active. */
    public object Idle : CanvasInteraction

    /** User is dragging a selection box. */
    public data class MarqueeSelecting(
        val start: Offset,
        val current: Offset,
        val mode: MarqueeMode = MarqueeMode.Replace
    ) : CanvasInteraction

    /** User is panning the canvas manually (e.g. via Space + Drag). */
    public object Panning : CanvasInteraction

    /**
     * User is dragging one endpoint of a selected edge to reconnect it.
     * The edge is NOT removed until the gesture resolves.
     */
    public data class DraggingEdgeEndpoint(
        val edgeId: EdgeId,
        val endpoint: EdgeEndpoint,
        val cursorWorld: Offset,
    ) : CanvasInteraction
}
