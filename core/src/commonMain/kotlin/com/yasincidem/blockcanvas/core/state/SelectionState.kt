package com.yasincidem.blockcanvas.core.state

import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.NodeId

/**
 * Immutable state managing selection of nodes and edges in the canvas.
 *
 * Provides a unidirectional data flow approach to selection: operations 
 * return a new mutated copy rather than modifying state in place.
 * By tracking them with stable IDs, the selection state remains detached
 * from the actual domain objects, preventing deep recompositions.
 *
 * @property selectedNodes A set of currently selected [NodeId]s.
 * @property selectedEdges A set of currently selected [EdgeId]s.
 */
public data class SelectionState(
    public val selectedNodes: Set<NodeId> = emptySet(),
    public val selectedEdges: Set<EdgeId> = emptySet(),
) {
    /** Checks whether a specific node is selected. */
    public fun isSelected(id: NodeId): Boolean = selectedNodes.contains(id)

    /** Checks whether a specific edge is selected. */
    public fun isSelected(id: EdgeId): Boolean = selectedEdges.contains(id)

    /** 
     * Adds a node to the selection.
     */
    public fun add(id: NodeId): SelectionState =
        copy(selectedNodes = selectedNodes + id)

    /** 
     * Adds an edge to the selection.
     */
    public fun add(id: EdgeId): SelectionState =
        copy(selectedEdges = selectedEdges + id)

    /** 
     * Removes a node from the selection.
     */
    public fun remove(id: NodeId): SelectionState =
        copy(selectedNodes = selectedNodes - id)

    /** 
     * Removes an edge from the selection.
     */
    public fun remove(id: EdgeId): SelectionState =
        copy(selectedEdges = selectedEdges - id)

    /** 
     * Selects only the given node, clearing all other selected nodes and edges.
     */
    public fun selectOnly(id: NodeId): SelectionState =
        SelectionState(selectedNodes = setOf(id))

    /** 
     * Selects only the given edge, clearing all other selected nodes and edges.
     */
    public fun selectOnly(id: EdgeId): SelectionState =
        SelectionState(selectedEdges = setOf(id))

    /**
     * Toggles the selection state for a node.
     */
    public fun toggle(id: NodeId): SelectionState =
        if (isSelected(id)) remove(id) else add(id)

    /**
     * Toggles the selection state for an edge.
     */
    public fun toggle(id: EdgeId): SelectionState =
        if (isSelected(id)) remove(id) else add(id)

    /**
     * Clears all selections.
     */
    public fun clear(): SelectionState = SelectionState()
}
