package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.PortDirection

/**
 * Reasons a candidate connection between two ports can be rejected.
 *
 * `ConnectionError` is sealed so consumers can exhaustively match on it in
 * `when` expressions. New error variants may be introduced in minor
 * releases without breaking binary compatibility of the sealed hierarchy,
 * but they _will_ cause previously exhaustive `when` blocks to no longer
 * be exhaustive — callers that want to stay forward-compatible should
 * include a defensive `else` branch.
 *
 * @since 0.1.0
 */
public sealed interface ConnectionError {

    /**
     * One of the endpoints does not resolve to a known [com.yasincidem.blockcanvas.core.model.Port].
     *
     * This typically means the referenced node or port id has been
     * removed, or the candidate endpoint was constructed against a stale
     * canvas snapshot.
     */
    public data class PortNotFound(public val endpoint: EndPoint) : ConnectionError

    /**
     * The candidate ports have incompatible directions.
     *
     * An admissible edge flows from an `Out` port to an `In` port; any
     * other combination (`In→Out`, `In→In`, `Out→Out`) is rejected.
     */
    public data class DirectionMismatch(
        public val fromDirection: PortDirection,
        public val toDirection: PortDirection,
    ) : ConnectionError

    /**
     * The candidate edge's `from` and `to` endpoints are identical —
     * a port cannot connect to itself.
     *
     * Note this is _strictly_ the same [EndPoint]; edges that connect
     * two different ports of the same node are allowed, since feedback
     * connections within a node are a legitimate use case.
     */
    public data class SelfLoop(public val endpoint: EndPoint) : ConnectionError

    /**
     * An edge with the same directed `(from, to)` pair already exists.
     *
     * Parallel edges in the _reverse_ direction are not considered
     * duplicates because edges are directional.
     */
    public data class DuplicateEdge(public val existingEdgeId: EdgeId) : ConnectionError
}
