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
public fun computePortPosition(node: Node, port: Port): Offset =
    computePortPosition(node.position, node.width, node.height, port.side)

public fun computePortPosition(
    position: Offset,
    width: Float,
    height: Float,
    side: PortSide
): Offset = when (side) {
    PortSide.Top    -> Offset(position.x + width  / 2f, position.y)
    PortSide.Right  -> Offset(position.x + width,       position.y + height / 2f)
    PortSide.Bottom -> Offset(position.x + width  / 2f, position.y + height)
    PortSide.Left   -> Offset(position.x,                position.y + height / 2f)
}
