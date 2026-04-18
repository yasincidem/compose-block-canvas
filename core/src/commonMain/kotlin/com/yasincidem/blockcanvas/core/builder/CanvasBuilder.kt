package com.yasincidem.blockcanvas.core.builder

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.routing.EdgeRouter
import com.yasincidem.blockcanvas.core.state.CanvasState

/**
 * Entry point for the Block Canvas DSL.
 *
 * ```kotlin
 * val canvas = buildCanvasState {
 *     node("input") {
 *         at(x = 100f, y = 200f)
 *         size(150f, 80f)
 *         port("out", PortSide.Right)
 *     }
 *     node("output") {
 *         rightOf("input", gap = 80f)
 *         size(150f, 80f)
 *         port("in", PortSide.Left)
 *     }
 *     connect("input", "out") linksTo connect("output", "in") style {
 *         stroke = EdgeStroke.Dashed()
 *         targetEnd = EdgeEnd.Arrow()
 *         animation = EdgeAnimation.MarchingAnts()
 *     }
 * }
 * ```
 */
public fun buildCanvasState(block: CanvasBuilder.() -> Unit): CanvasState {
    return CanvasBuilder().apply(block).build()
}

public class CanvasBuilder {
    private val nodes = mutableMapOf<NodeId, Node>()
    private val edges = mutableMapOf<EdgeId, Edge>()

    public fun node(id: String, block: NodeBuilder.() -> Unit = {}): NodeId {
        val nodeId = NodeId(id)
        require(!nodes.containsKey(nodeId)) { "Node with ID '$id' already exists." }
        val node = NodeBuilder(nodeId, nodes).apply(block).build()
        nodes[nodeId] = node
        return nodeId
    }

    /**
     * Creates an edge between [from] and [to] with optional styling via [block].
     *
     * ```kotlin
     * edge(from = EndPoint(nodeA, portOut), to = EndPoint(nodeB, portIn)) {
     *     stroke = EdgeStroke.Dashed()
     *     targetEnd = EdgeEnd.Arrow()
     * }
     * ```
     */
    public fun edge(
        from: EndPoint,
        to: EndPoint,
        id: String? = null,
        block: EdgeBuilder.() -> Unit = {},
    ) {
        val edgeId = id?.let { EdgeId(it) } ?: EdgeId("e_${edges.size}")
        edges[edgeId] = EdgeBuilder(edgeId, from, to).apply(block).build()
    }

    /** Returns a (nodeId, portId) pair for use with [linksTo]. */
    public fun connect(nodeId: String, portId: String): Pair<String, String> = nodeId to portId

    /**
     * Connects two ports with optional decoration.
     *
     * ```kotlin
     * connect("a", "out") linksTo connect("b", "in") style {
     *     stroke = EdgeStroke.Dotted()
     * }
     * ```
     */
    public infix fun Pair<String, String>.linksTo(other: Pair<String, String>): EdgeBuilder {
        val edgeId = EdgeId("e_${edges.size}")
        val from = EndPoint(NodeId(this.first), PortId(this.second))
        val to = EndPoint(NodeId(other.first), PortId(other.second))
        val builder = EdgeBuilder(edgeId, from, to)
        // Register a live reference — style{} will call builder.apply() then rebuild.
        edges[edgeId] = builder.build()
        builder.onChanged = { edges[edgeId] = it }
        return builder
    }

    internal fun build(): CanvasState = CanvasState(nodes = nodes.toMap(), edges = edges.toMap())
}

/** Fluent builder for edge appearance. Used directly in [edge] or chained via [style]. */
public class EdgeBuilder internal constructor(
    private val id: EdgeId,
    private val from: EndPoint,
    private val to: EndPoint,
) {
    public var router: EdgeRouter? = null
    public var sourceEnd: EdgeEnd? = null
    public var targetEnd: EdgeEnd? = null
    public var stroke: EdgeStroke? = null
    public var animation: EdgeAnimation? = null

    internal var onChanged: ((Edge) -> Unit)? = null

    internal fun build(): Edge = Edge(
        id = id,
        from = from,
        to = to,
        router = router,
        sourceEnd = sourceEnd,
        targetEnd = targetEnd,
        stroke = stroke,
        animation = animation,
    )
}

/**
 * Applies edge decoration inline after [linksTo].
 *
 * ```kotlin
 * connect("a", "out") linksTo connect("b", "in") style {
 *     stroke = EdgeStroke.Dashed()
 *     targetEnd = EdgeEnd.Arrow()
 * }
 * ```
 */
public infix fun EdgeBuilder.style(block: EdgeBuilder.() -> Unit): EdgeBuilder {
    apply(block)
    onChanged?.invoke(build())
    return this
}

public class NodeBuilder(
    private val id: NodeId,
    private val existingNodes: Map<NodeId, Node>,
) {
    private var position = Offset.Zero
    private var width = 150f
    private var height = 80f
    private val ports = mutableListOf<Port>()

    public fun at(x: Float, y: Float) {
        position = Offset(x, y)
    }

    public fun size(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    public fun port(id: String, side: PortSide) {
        ports.add(Port(PortId(id), side))
    }

    public fun rightOf(nodeId: String, gap: Float = 40f) {
        val other = existingNodes[NodeId(nodeId)]
            ?: error("Node '$nodeId' not found for relative positioning.")
        position = Offset(other.position.x + other.width + gap, other.position.y)
    }

    public fun below(nodeId: String, gap: Float = 40f) {
        val other = existingNodes[NodeId(nodeId)]
            ?: error("Node '$nodeId' not found for relative positioning.")
        position = Offset(other.position.x, other.position.y + other.height + gap)
    }

    internal fun build(): Node = Node(
        id = id,
        position = position,
        width = width,
        height = height,
        ports = ports.toList(),
    )
}
