package com.yasincidem.blockcanvas.core.model

import kotlin.jvm.JvmInline

/**
 * Stable, type-safe identifier for a port attached to a [Node].
 *
 * Uniqueness of a `PortId` is scoped to its owning node: two different
 * nodes may legitimately use the same `PortId.value`. Edges always
 * reference a port through the pair `(NodeId, PortId)` to disambiguate.
 *
 * Zero-cost wrapper around a [String] — see [NodeId] for the rationale.
 *
 * @property value The raw identifier. Must be non-blank.
 * @since 0.1.0
 */
@JvmInline
public value class PortId(public val value: String) {
    init {
        require(value.isNotBlank()) { "PortId value must not be blank" }
    }
}
