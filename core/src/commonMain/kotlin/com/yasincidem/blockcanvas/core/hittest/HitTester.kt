package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node

/**
 * Determines what canvas element a world-space point lands on.
 *
 * Priority: [HitResult.Port] > [HitResult.Node] > [HitResult.Empty].
 *
 * Port positions are computed from node geometry via [computePortPosition],
 * so no external resolver is required.
 */
public interface HitTester {
    public fun hitTest(
        point: Offset,
        nodes: Collection<Node>,
        portTolerance: Float = DefaultHitTester.DEFAULT_PORT_TOLERANCE,
    ): HitResult
}
