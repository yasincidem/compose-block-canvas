package com.yasincidem.blockcanvas.core.model

/**
 * The direction in which data flows through a port.
 *
 * v0.1 enforces only direction-based connection rules: an edge must leave
 * an [Out] port and enter an [In] port. Richer constraints (type
 * compatibility, cardinality limits) are planned for later versions and
 * will be layered on top of this enum without breaking existing code.
 *
 * @since 0.1.0
 */
public enum class PortDirection {
    /** The port consumes values produced elsewhere in the graph. */
    In,

    /** The port produces values consumed elsewhere in the graph. */
    Out,
}
