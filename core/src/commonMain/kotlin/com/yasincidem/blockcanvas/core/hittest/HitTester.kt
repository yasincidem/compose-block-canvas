package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId

/**
 * Determines what canvas element a world-space point lands on.
 *
 * Priority: [HitResult.Port] > [HitResult.Node] > [HitResult.Empty].
 *
 * Port positions are computed from node geometry via [computePortPosition],
 * so no external resolver is required.
 */
public interface HitTester {
    /**
     * @param positionOverrides Live drag positions keyed by [NodeId]. When present,
     *   these override [Node.position] for both body and port hit-testing, so that
     *   mid-drag positions are reflected without waiting for a canvas commit.
     */
    public fun hitTest(
        point: Offset,
        nodes: Collection<Node>,
        portTolerance: Float = DefaultHitTester.DEFAULT_PORT_TOLERANCE,
        positionOverrides: Map<NodeId, Offset> = emptyMap(),
    ): HitResult
}
