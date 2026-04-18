package com.yasincidem.blockcanvas.core.hittest

import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.PortId

/**
 * The outcome of a canvas hit test, ordered by priority:
 * [Port] > [Node] > [Empty].
 */
public sealed class HitResult {

    /** A port was the closest target within the tolerance radius. */
    public data class Port(val nodeId: NodeId, val portId: PortId) : HitResult()

    /** The point landed inside a node's bounding rectangle (but no port was close enough). */
    public data class Node(val nodeId: NodeId) : HitResult()

    /** The point did not land on any port or node. */
    public data object Empty : HitResult()
}
