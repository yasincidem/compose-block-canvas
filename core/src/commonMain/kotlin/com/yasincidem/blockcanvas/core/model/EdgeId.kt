package com.yasincidem.blockcanvas.core.model

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Stable, type-safe identifier for an [Edge] connecting two ports.
 *
 * Zero-cost wrapper around a [String] — see [NodeId] for the rationale.
 *
 * @property value The raw identifier. Must be non-blank.
 * @since 0.1.0
 */
@Serializable
@JvmInline
public value class EdgeId(public val value: String) {
    init {
        require(value.isNotBlank()) { "EdgeId value must not be blank" }
    }
}
