package com.yasincidem.blockcanvas.core.alignment

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.NodeId
import kotlin.math.abs

public enum class Axis { Horizontal, Vertical }

public enum class EdgeAlignment { Start, Center, End }

public data class AlignmentGuide(
    val axis: Axis,
    val position: Float,
    val fromNode: NodeId,
    val edge: EdgeAlignment,
)

public data class DistanceLabel(
    val between: Pair<NodeId, NodeId>,
    val distance: Float,
    val midpoint: Offset,
)

public data class AlignmentResult(
    val guides: List<AlignmentGuide>,
    val labels: List<DistanceLabel>,
    val snapOffset: Offset,
)

public object AlignmentDetector {

    public fun detect(
        dragged: Node,
        others: Collection<Node>,
        threshold: Float = 6f,
    ): AlignmentResult {
        val guides = mutableListOf<AlignmentGuide>()
        val labels = mutableListOf<DistanceLabel>()
        var snapDx = Float.MAX_VALUE
        var snapDy = Float.MAX_VALUE

        // 3 x-anchors on dragged node for vertical guides (constant x lines)
        val dXStart = dragged.position.x
        val dXCenter = dragged.position.x + dragged.width / 2f
        val dXEnd = dragged.position.x + dragged.width

        // 3 y-anchors on dragged node for horizontal guides (constant y lines)
        val dYStart = dragged.position.y
        val dYCenter = dragged.position.y + dragged.height / 2f
        val dYEnd = dragged.position.y + dragged.height

        for (other in others) {
            if (other.id == dragged.id) continue

            val oXStart = other.position.x
            val oXCenter = other.position.x + other.width / 2f
            val oXEnd = other.position.x + other.width
            val oYStart = other.position.y
            val oYCenter = other.position.y + other.height / 2f
            val oYEnd = other.position.y + other.height

            // --- Vertical guides (x alignment) ---
            checkVertical(dXStart, oXStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXStart, oXCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXStart, oXEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXCenter, oXStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXCenter, oXCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXCenter, oXEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXEnd, oXStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXEnd, oXCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }
            checkVertical(dXEnd, oXEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDx)) snapDx = it }

            // --- Horizontal guides (y alignment) ---
            checkHorizontal(dYStart, oYStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYStart, oYCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYStart, oYEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYCenter, oYStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYCenter, oYCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYCenter, oYEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYEnd, oYStart,  EdgeAlignment.Start,  other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYEnd, oYCenter, EdgeAlignment.Center, other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }
            checkHorizontal(dYEnd, oYEnd,    EdgeAlignment.End,    other.id, threshold, guides)?.let { if (abs(it) < abs(snapDy)) snapDy = it }

            // --- Distance labels ---
            var hasVertical = false
            for (g in guides) if (g.fromNode == other.id && g.axis == Axis.Vertical) { hasVertical = true; break }
            if (hasVertical) {
                val gapH = dragged.position.x - (other.position.x + other.width)
                val gapH2 = other.position.x - (dragged.position.x + dragged.width)
                val gap = if (gapH > 0) gapH else if (gapH2 > 0) gapH2 else null
                if (gap != null && gap > 0) {
                    val midX = if (gapH > 0) other.position.x + other.width + gap / 2f else dragged.position.x + dragged.width + gap / 2f
                    val midY = (dragged.position.y + dragged.height / 2f + other.position.y + other.height / 2f) / 2f
                    labels.add(DistanceLabel(other.id to dragged.id, gap, Offset(midX, midY)))
                }
            }

            var hasHorizontal = false
            for (g in guides) if (g.fromNode == other.id && g.axis == Axis.Horizontal) { hasHorizontal = true; break }
            if (hasHorizontal) {
                val gapV = dragged.position.y - (other.position.y + other.height)
                val gapV2 = other.position.y - (dragged.position.y + dragged.height)
                val gap = if (gapV > 0) gapV else if (gapV2 > 0) gapV2 else null
                if (gap != null && gap > 0) {
                    val midY = if (gapV > 0) other.position.y + other.height + gap / 2f else dragged.position.y + dragged.height + gap / 2f
                    val midX = (dragged.position.x + dragged.width / 2f + other.position.x + other.width / 2f) / 2f
                    labels.add(DistanceLabel(other.id to dragged.id, gap, Offset(midX, midY)))
                }
            }
        }

        return AlignmentResult(
            guides = guides.distinctBy { it.axis to it.position },
            labels = labels,
            snapOffset = Offset(if (snapDx == Float.MAX_VALUE) 0f else snapDx, if (snapDy == Float.MAX_VALUE) 0f else snapDy),
        )
    }

    private fun checkVertical(d: Float, o: Float, edge: EdgeAlignment, id: NodeId, threshold: Float, list: MutableList<AlignmentGuide>): Float? {
        val diff = abs(d - o)
        if (diff <= threshold) {
            list.add(AlignmentGuide(Axis.Vertical, o, id, edge))
            return o - d
        }
        return null
    }

    private fun checkHorizontal(d: Float, o: Float, edge: EdgeAlignment, id: NodeId, threshold: Float, list: MutableList<AlignmentGuide>): Float? {
        val diff = abs(d - o)
        if (diff <= threshold) {
            list.add(AlignmentGuide(Axis.Horizontal, o, id, edge))
            return o - d
        }
        return null
    }
}
