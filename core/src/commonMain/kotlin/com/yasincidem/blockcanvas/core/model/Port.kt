package com.yasincidem.blockcanvas.core.model

/**
 * A connection point attached to a [Node].
 *
 * A port carries only its identity and direction — it is intentionally
 * stateless. Its on-screen position is computed by the rendering layer
 * from the owning node's geometry and the port's index within
 * [Node.ports].
 *
 * @property id Stable identifier, unique within the owning node.
 * @property direction Data-flow direction; see [PortDirection].
 * @since 0.1.0
 */
public data class Port(
    public val id: PortId,
    public val direction: PortDirection,
)
