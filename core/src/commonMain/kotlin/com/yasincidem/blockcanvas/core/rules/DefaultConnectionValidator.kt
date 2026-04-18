package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Port

/**
 * Default [ConnectionValidator] enforcing three rules:
 *
 *  1. Self-loop — `from` and `to` must be different [EndPoint]s.
 *  2. Port existence — both endpoints must resolve via `portLookup`.
 *  3. Duplicate — no existing edge may share the same directed `(from, to)` pair.
 *
 * Rules are evaluated in order and short-circuit on the first violation.
 * Any port can connect to any other port on a different node — there is
 * no direction constraint at this layer.
 *
 * @since 0.1.0
 */
public class DefaultConnectionValidator : ConnectionValidator {

    override fun validate(
        from: EndPoint,
        to: EndPoint,
        existingEdges: List<Edge>,
        portLookup: (EndPoint) -> Port?,
    ): ConnectionError? {
        if (from == to) return ConnectionError.SelfLoop(from)

        portLookup(from) ?: return ConnectionError.PortNotFound(from)
        portLookup(to)   ?: return ConnectionError.PortNotFound(to)

        val duplicate = existingEdges.firstOrNull { it.from == from && it.to == to }
        if (duplicate != null) return ConnectionError.DuplicateEdge(duplicate.id)

        return null
    }
}
