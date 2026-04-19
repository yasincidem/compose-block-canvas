package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Port

/**
 * Rejects a connection when either the source or target port already has
 * [maxPerPort] or more edges attached to it.
 *
 * Default [maxPerPort] = 1 enforces a "one connection per port" rule, which
 * keeps typical node-graph canvases clean. Set higher (or [Int.MAX_VALUE])
 * to allow fan-out/fan-in topologies.
 *
 * ```kotlin
 * val state = rememberBlockCanvasState(
 *     connectionValidator = CompositeConnectionValidator(
 *         DefaultConnectionValidator(),
 *         MaxEdgesPerPortRule(maxPerPort = 1),
 *     )
 * )
 * ```
 */
public class MaxEdgesPerPortRule(public val maxPerPort: Int = 1) : ConnectionValidator {

    override fun validate(
        from: EndPoint,
        to: EndPoint,
        existingEdges: Collection<Edge>,
        portLookup: (EndPoint) -> Port?,
    ): ConnectionError? {
        val fromCount = existingEdges.count { it.from == from || it.to == from }
        if (fromCount >= maxPerPort) {
            return ConnectionError.MaxEdgesExceeded(from, fromCount, maxPerPort)
        }
        val toCount = existingEdges.count { it.from == to || it.to == to }
        if (toCount >= maxPerPort) {
            return ConnectionError.MaxEdgesExceeded(to, toCount, maxPerPort)
        }
        return null
    }
}
