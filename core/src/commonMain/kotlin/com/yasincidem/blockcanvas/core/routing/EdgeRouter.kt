package com.yasincidem.blockcanvas.core.routing

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.math.abs

// ── Path result ───────────────────────────────────────────────────────────────

public sealed interface EdgePath {
    /** A cubic bezier curve defined by start, two control points, and end. */
    public data class Bezier(
        val start: Offset,
        val control1: Offset,
        val control2: Offset,
        val end: Offset,
    ) : EdgePath
    // future: Polyline(points: List<Offset>), Line(start, end)
}

// ── Router ────────────────────────────────────────────────────────────────────

public sealed interface EdgeRouter {
    public fun route(
        source: Offset,
        target: Offset,
        sourceSide: PortSide,
        targetSide: PortSide,
    ): EdgePath

    /**
     * Horizontal cubic bezier. Control-point handle = half the horizontal
     * distance between ports, clamped to a minimum of 60 world units so
     * short/vertical edges still curve visibly. This matches the legacy
     * inline rendering exactly.
     */
    public object Bezier : EdgeRouter {
        override fun route(
            source: Offset,
            target: Offset,
            sourceSide: PortSide,
            targetSide: PortSide,
        ): EdgePath.Bezier {
            val handle = abs(target.x - source.x).coerceAtLeast(60f) * 0.5f
            return EdgePath.Bezier(
                start    = source,
                control1 = Offset(source.x + handle, source.y),
                control2 = Offset(target.x - handle, target.y),
                end      = target,
            )
        }
    }
    // future: object Orthogonal : EdgeRouter { ... }
    // future: object Straight   : EdgeRouter { ... }
}
