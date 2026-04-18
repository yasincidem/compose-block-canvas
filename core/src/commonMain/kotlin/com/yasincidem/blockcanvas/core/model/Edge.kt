package com.yasincidem.blockcanvas.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.yasincidem.blockcanvas.core.routing.EdgeRouter

/**
 * A directed connection between two ports in the canvas.
 *
 * `Edge` is a pure data holder: it enforces no rules beyond those
 * encoded in its type system. Whether an edge is admissible — e.g. its
 * [from] endpoint must be an `Out` port, the edge must not form a
 * self-loop — is the responsibility of a `ConnectionValidator`, not of
 * `Edge` itself. This separation lets consumers construct edges in
 * intermediate "draft" states (for example, while the user is still
 * dragging a connection) without fighting the type.
 *
 * The edge is directional: [from] and [to] are not interchangeable.
 *
 * @property id Stable canvas-wide identifier.
 * @property from The endpoint the edge leaves (typically an `Out` port).
 * @property to The endpoint the edge enters (typically an `In` port).
 * @since 0.1.0
 */
@Serializable
public data class Edge(
    public val id: EdgeId,
    public val from: EndPoint,
    public val to: EndPoint,
    /** Per-edge router override. `null` = canvas-wide default. Not serialized. */
    @Transient public val router: EdgeRouter? = null,
    /** Decoration at the source end. `null` = canvas-wide default. Not serialized. */
    @Transient public val sourceEnd: EdgeEnd? = null,
    /** Decoration at the target end. `null` = canvas-wide default. Not serialized. */
    @Transient public val targetEnd: EdgeEnd? = null,
    /** Stroke style. `null` = canvas-wide default. Not serialized. */
    @Transient public val stroke: EdgeStroke? = null,
    /** Animation. `null` = canvas-wide default. Not serialized. */
    @Transient public val animation: EdgeAnimation? = null,
)
