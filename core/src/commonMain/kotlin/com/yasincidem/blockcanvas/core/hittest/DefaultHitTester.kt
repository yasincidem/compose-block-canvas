package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Rect
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.computePortPosition

/**
 * Default hit-test implementation.
 *
 * Pass 1 — ports: for each node, computes each port's world position via
 * [computePortPosition] and returns the first port within [portTolerance].
 *
 * Pass 2 — nodes: returns the first node whose bounding rect contains [point]
 * (half-open: left/top inclusive, right/bottom exclusive).
 *
 * Pass 3 — empty.
 */
public class DefaultHitTester : HitTester {

    override fun hitTest(
        point: Offset,
        nodes: Collection<Node>,
        portTolerance: Float,
    ): HitResult {
        // Pass 1 — ports
        for (node in nodes) {
            for (port in node.ports) {
                val pos = computePortPosition(node, port)
                if (pos.distanceTo(point) <= portTolerance) {
                    return HitResult.Port(node.id, port.id)
                }
            }
        }

        // Pass 2 — nodes
        for (node in nodes) {
            val rect = Rect(
                left   = node.position.x,
                top    = node.position.y,
                right  = node.position.x + node.width,
                bottom = node.position.y + node.height,
            )
            if (rect.contains(point)) return HitResult.Node(node.id)
        }

        return HitResult.Empty
    }

    public companion object {
        public const val DEFAULT_PORT_TOLERANCE: Float = 16f
    }
}
