package com.yasincidem.blockcanvas.core.model

import com.yasincidem.blockcanvas.core.geometry.Offset

/**
 * Computes the world-space position of [port] on [node].
 *
 * Each side maps to the center of that edge of the node's bounding box:
 * - [PortSide.Top]    → top-center
 * - [PortSide.Right]  → right-center
 * - [PortSide.Bottom] → bottom-center
 * - [PortSide.Left]   → left-center
 */
public fun computePortPosition(node: Node, port: Port): Offset = when (port.side) {
    PortSide.Top    -> Offset(node.position.x + node.width  / 2f, node.position.y)
    PortSide.Right  -> Offset(node.position.x + node.width,       node.position.y + node.height / 2f)
    PortSide.Bottom -> Offset(node.position.x + node.width  / 2f, node.position.y + node.height)
    PortSide.Left   -> Offset(node.position.x,                    node.position.y + node.height / 2f)
}
