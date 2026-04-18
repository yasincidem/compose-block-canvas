package com.yasincidem.blockcanvas.core.model

public sealed interface EdgeAnimation {
    public object None : EdgeAnimation

    /**
     * Animates the dash offset of the edge stroke, creating a "marching ants"
     * flow effect. Requires [EdgeStroke.Dashed] or [EdgeStroke.Dotted]; ignored
     * silently on [EdgeStroke.Solid] edges (no auto-upgrade).
     */
    public data class MarchingAnts(
        val speedDpPerSecond: Float = 40f,
        /** `false` = source→target, `true` = target→source. */
        val reverse: Boolean = false,
    ) : EdgeAnimation

    /**
     * Draws [count] filled dots travelling along the edge path from source to target.
     */
    public data class Pulse(
        val dotRadius: Float = 3f,
        val durationMs: Int = 1500,
        val count: Int = 1,
    ) : EdgeAnimation
}
