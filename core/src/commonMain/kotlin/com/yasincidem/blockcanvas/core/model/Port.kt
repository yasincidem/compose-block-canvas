package com.yasincidem.blockcanvas.core.model

import kotlinx.serialization.Serializable

/**
 * A connection point attached to a [Node].
 *
 * A port carries only its identity and which side of the node it lives on.
 * Its world-space position is derived from the owning node's geometry via
 * [computePortPosition] — no coordinate storage is needed here.
 *
 * @property id   Stable identifier, unique within the owning node.
 * @property side Which edge of the node's bounding box this port sits on.
 * @since 0.1.0
 */
@Serializable
public data class Port(
    public val id: PortId,
    public val side: PortSide,
)
