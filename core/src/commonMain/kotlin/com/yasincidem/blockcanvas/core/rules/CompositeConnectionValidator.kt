package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Port

/**
 * Chains multiple [ConnectionValidator]s, returning the first error encountered.
 *
 * ```kotlin
 * val validator = CompositeConnectionValidator(
 *     DefaultConnectionValidator(),          // self-loop, duplicates
 *     MaxEdgesPerPortRule(maxPerPort = 1),   // one edge per port
 * )
 * ```
 */
public class CompositeConnectionValidator(
    private vararg val validators: ConnectionValidator,
) : ConnectionValidator {

    override fun validate(
        from: EndPoint,
        to: EndPoint,
        existingEdges: Collection<Edge>,
        portLookup: (EndPoint) -> Port?,
    ): ConnectionError? {
        for (validator in validators) {
            val error = validator.validate(from, to, existingEdges, portLookup)
            if (error != null) return error
        }
        return null
    }
}
