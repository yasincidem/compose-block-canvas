package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortDirection

/**
 * Default [ConnectionValidator] enforcing the v0.1 connection rules:
 *
 *  1. Self-loop rejection — an edge whose `from` and `to` are the same
 *     [EndPoint] is invalid.
 *  2. Port existence — both endpoints must resolve through `portLookup`.
 *  3. Direction — `from` must be an `Out` port and `to` must be an `In`
 *     port.
 *  4. Duplicate rejection — no existing edge may share the same
 *     directed `(from, to)` pair.
 *
 * Rules are evaluated in that order and validation short-circuits on the
 * first violation; the cheapest checks run first so the common path
 * through a dense canvas stays allocation-free.
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

        val fromPort = portLookup(from) ?: return ConnectionError.PortNotFound(from)
        val toPort = portLookup(to) ?: return ConnectionError.PortNotFound(to)

        if (fromPort.direction != PortDirection.Out || toPort.direction != PortDirection.In) {
            return ConnectionError.DirectionMismatch(fromPort.direction, toPort.direction)
        }

        val duplicate = existingEdges.firstOrNull { it.from == from && it.to == to }
        if (duplicate != null) return ConnectionError.DuplicateEdge(duplicate.id)

        return null
    }
}
