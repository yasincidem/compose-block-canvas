package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortId

/**
 * Determines what canvas element a world-space point lands on.
 *
 * The [portPosition] resolver decouples the hit tester from UI layout —
 * the caller supplies port world-positions so this interface stays in `:core`.
 *
 * Priority: [HitResult.Port] > [HitResult.Node] > [HitResult.Empty].
 */
public interface HitTester {
    public fun hitTest(
        point: Offset,
        nodes: Collection<Node>,
        portPosition: (nodeId: NodeId, portId: PortId) -> Offset?,
        portTolerance: Float = DefaultHitTester.DEFAULT_PORT_TOLERANCE,
    ): HitResult
}
