package com.yasincidem.blockcanvas.core.model

import com.yasincidem.blockcanvas.core.geometry.Offset

/**
 * A draggable block in the canvas.
 *
 * A node owns a list of input / output [Port]s and carries the geometry
 * needed to place it on screen: the top-left [position] plus [width]
 * and [height]. All coordinates are expressed in canvas-space (i.e.
 * world coordinates, not screen pixels).
 *
 * Ports must have distinct [PortId]s within the node; cross-node
 * collisions are legal because every edge references a port through
 * the full `(NodeId, PortId)` pair.
 *
 * **Immutability contract:** callers must pass an immutable [ports]
 * list (for example, one built with `listOf(...)`). `Node` does not
 * defensively copy the list on construction — this avoids allocations
 * on every `Node` creation, which matters for canvases with hundreds
 * of nodes.
 *
 * @property id Stable canvas-wide identifier.
 * @property position Top-left of the node in canvas-space.
 * @property width Horizontal extent; must be non-negative.
 * @property height Vertical extent; must be non-negative.
 * @property ports Immutable list of ports attached to this node.
 * @since 0.1.0
 */
public data class Node(
    public val id: NodeId,
    public val position: Offset,
    public val width: Float,
    public val height: Float,
    public val ports: List<Port>,
) {
    init {
        require(width >= 0f) { "Node width ($width) must be >= 0" }
        require(height >= 0f) { "Node height ($height) must be >= 0" }
        val duplicate = ports.groupingBy { it.id }.eachCount().entries
            .firstOrNull { it.value > 1 }?.key
        require(duplicate == null) {
            "Node '${id.value}' declares duplicate PortId '${duplicate?.value}'"
        }
    }
}
