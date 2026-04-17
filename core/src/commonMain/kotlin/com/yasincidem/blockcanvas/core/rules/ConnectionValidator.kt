package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Port

/**
 * Decides whether a candidate directed edge between two endpoints is
 * admissible in a canvas.
 *
 * The validator is expressed as an interface so consumers can supply a
 * stricter rule set (e.g. typed ports, DAG-only canvases) without
 * subclassing [DefaultConnectionValidator]. Call [validate] with a pure
 * `portLookup` function — keeping the validator decoupled from any
 * concrete canvas state implementation.
 *
 * @since 0.1.0
 */
public interface ConnectionValidator {

    /**
     * Checks whether `from → to` is admissible given [existingEdges] and
     * the port graph resolved through [portLookup].
     *
     * @return `null` if the candidate edge is valid, otherwise a
     *   [ConnectionError] describing the first violated rule.
     */
    public fun validate(
        from: EndPoint,
        to: EndPoint,
        existingEdges: List<Edge>,
        portLookup: (EndPoint) -> Port?,
    ): ConnectionError?
}
