package com.yasincidem.blockcanvas.core.model

import kotlin.jvm.JvmInline

/**
 * Stable, type-safe identifier for a [Node] inside a canvas.
 *
 * Wraps a caller-owned [String] — consumers decide the naming scheme
 * (UUID, slug, numeric counter, etc.). The `@JvmInline value class`
 * representation means a `NodeId` costs the same as the underlying
 * `String` at runtime, and the type system prevents accidentally mixing
 * it with [PortId], [EdgeId], or a raw `String`.
 *
 * Because the class is a value class wrapping a primitive-like type,
 * the Compose compiler treats it as stable and safe to use as a
 * composable key without triggering recomposition.
 *
 * @property value The raw identifier. Must be non-blank.
 * @since 0.1.0
 */
@JvmInline
public value class NodeId(public val value: String) {
    init {
        require(value.isNotBlank()) { "NodeId value must not be blank" }
    }
}
