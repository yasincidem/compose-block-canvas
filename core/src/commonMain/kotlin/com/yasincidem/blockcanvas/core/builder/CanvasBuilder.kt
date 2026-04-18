package com.yasincidem.blockcanvas.core.builder

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.state.CanvasState

/**
 * Entry point for the Block Canvas DSL.
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
        val builder = NodeBuilder(nodeId, nodes).apply(block)
        val node = builder.build()
        nodes[nodeId] = node
        return nodeId
    }

    public fun edge(from: EndPoint, to: EndPoint, id: String? = null) {
        val edgeId = id?.let { EdgeId(it) } ?: EdgeId("e_${edges.size}")
        edges[edgeId] = Edge(edgeId, from, to)
    }

    public infix fun Pair<String, String>.linksTo(other: Pair<String, String>) {
        val fromNode = NodeId(this.first)
        val fromPort = PortId(this.second)
        val toNode = NodeId(other.first)
        val toPort = PortId(other.second)
        
        edge(EndPoint(fromNode, fromPort), EndPoint(toNode, toPort))
    }

    public fun connect(nodeId: String, portId: String): Pair<String, String> = nodeId to portId

    internal fun build(): CanvasState {
        return CanvasState(nodes = nodes.toMap(), edges = edges.toMap())
    }
}

public class NodeBuilder(
    private val id: NodeId,
    private val existingNodes: Map<NodeId, Node>
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
        val other = existingNodes[NodeId(nodeId)] ?: error("Node '$nodeId' not found for relative positioning.")
        position = Offset(other.position.x + other.width + gap, other.position.y)
    }

    public fun below(nodeId: String, gap: Float = 40f) {
        val other = existingNodes[NodeId(nodeId)] ?: error("Node '$nodeId' not found for relative positioning.")
        position = Offset(other.position.x, other.position.y + other.height + gap)
    }

    internal fun build(): Node {
        return Node(
            id = id,
            position = position,
            width = width,
            height = height,
            ports = ports.toList()
        )
    }
}
