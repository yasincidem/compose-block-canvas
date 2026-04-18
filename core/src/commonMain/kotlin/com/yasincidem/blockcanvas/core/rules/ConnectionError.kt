package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint

/**
 * Reasons a candidate connection between two ports can be rejected.
 *
 * @since 0.1.0
 */
public sealed interface ConnectionError {

    /**
     * One of the endpoints does not resolve to a known port.
     */
    public data class PortNotFound(public val endpoint: EndPoint) : ConnectionError

    /**
     * The candidate edge's `from` and `to` endpoints are identical.
     */
    public data class SelfLoop(public val endpoint: EndPoint) : ConnectionError

    /**
     * An edge with the same directed `(from, to)` pair already exists.
     */
    public data class DuplicateEdge(public val existingEdgeId: EdgeId) : ConnectionError
}
