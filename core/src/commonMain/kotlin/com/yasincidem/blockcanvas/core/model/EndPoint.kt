package com.yasincidem.blockcanvas.core.model

/**
 * One end of an [Edge]: a reference to a specific port on a specific node.
 *
 * Because [PortId] is unique only within its owning node, edges always
 * identify their endpoints through the full `(NodeId, PortId)` pair to
 * avoid ambiguity.
 *
 * @property node The node owning the referenced port.
 * @property port The port on that node.
 * @since 0.1.0
 */
public data class EndPoint(
    public val node: NodeId,
    public val port: PortId,
)
