package com.yasincidem.blockcanvas.core.state

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId

/**
 * Immutable state of the block canvas.
 *
 * @property nodes A map of all current nodes by their stable IDs.
 * @property edges A map of all current edges by their stable IDs.
 */
public data class CanvasState(
    public val nodes: Map<NodeId, Node> = emptyMap(),
    public val edges: Map<EdgeId, Edge> = emptyMap(),
) {
    /**
     * Adds or updates a node in the state.
     */
    public fun addNode(node: Node): CanvasState {
        return copy(nodes = nodes + (node.id to node))
    }

    /**
     * Removes a node by ID.
     * Also automatically removes any edges attached to this node.
     */
    public fun removeNode(id: NodeId): CanvasState {
        if (!nodes.containsKey(id)) return this
        
        val newNodes = nodes - id
        val edgesToRemove = edges.values.filter { 
            it.from.node == id || it.to.node == id 
        }.map { it.id }.toSet()
        
        return copy(
            nodes = newNodes,
            edges = edges.filterKeys { it !in edgesToRemove }
        )
    }

    /**
     * Adds an edge to the state.
     * Throws an [IllegalArgumentException] if the source or target nodes do not exist.
     */
    public fun addEdge(edge: Edge): CanvasState {
        require(nodes.containsKey(edge.from.node)) {
            "Cannot add edge: source node ${edge.from.node} does not exist."
        }
        require(nodes.containsKey(edge.to.node)) {
            "Cannot add edge: target node ${edge.to.node} does not exist."
        }
        return copy(edges = edges + (edge.id to edge))
    }

    /**
     * Removes an edge from the state by ID.
     */
    public fun removeEdge(id: EdgeId): CanvasState {
        return copy(edges = edges - id)
    }
}
