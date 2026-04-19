package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.geometry.Rect
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.computePortPosition

/**
 * Default hit-test implementation.
 *
 * Pass 1 — ports: for each node, computes each port's world position via
 * [computePortPosition] and returns the first port within [portTolerance].
 * Live drag positions in [positionOverrides] are used instead of [Node.position]
 * so that mid-drag state is reflected correctly.
 *
 * Pass 2 — nodes: returns the first node whose bounding rect contains [point].
 *
 * Pass 3 — empty.
 */
public class DefaultHitTester : HitTester {

    override fun hitTest(
        point: Offset,
        nodes: Collection<Node>,
        portTolerance: Float,
        positionOverrides: Map<NodeId, Offset>,
    ): HitResult {
        // Pass 1 — ports
        for (node in nodes) {
            val pos = positionOverrides[node.id] ?: node.position
            for (port in node.ports) {
                val portPos = computePortPosition(pos, node.width, node.height, port.side)
                if (portPos.distanceTo(point) <= portTolerance) {
                    return HitResult.Port(node.id, port.id)
                }
            }
        }

        // Pass 2 — nodes
        for (node in nodes) {
            val pos = positionOverrides[node.id] ?: node.position
            val rect = Rect(
                left   = pos.x,
                top    = pos.y,
                right  = pos.x + node.width,
                bottom = pos.y + node.height,
            )
            if (rect.contains(point)) return HitResult.Node(node.id)
        }

        return HitResult.Empty
    }

    public companion object {
        public const val DEFAULT_PORT_TOLERANCE: Float = 24f
    }
}
