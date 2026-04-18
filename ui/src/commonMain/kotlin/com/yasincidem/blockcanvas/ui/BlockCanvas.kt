package com.yasincidem.blockcanvas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.alignment.Axis
import com.yasincidem.blockcanvas.core.alignment.DistanceLabel
import com.yasincidem.blockcanvas.core.hittest.DefaultHitTester
import com.yasincidem.blockcanvas.core.hittest.HitResult
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.Node
import com.yasincidem.blockcanvas.core.model.PortSide
import com.yasincidem.blockcanvas.core.model.computePortPosition
import com.yasincidem.blockcanvas.core.routing.EdgePath
import com.yasincidem.blockcanvas.core.routing.EdgeRouter
import com.yasincidem.blockcanvas.ui.state.AlignmentGuideStyle
import com.yasincidem.blockcanvas.ui.state.BlockCanvasState
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.GridType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import androidx.compose.ui.graphics.StrokeCap
import com.yasincidem.blockcanvas.core.geometry.Offset as CoreOffset

private fun CoreOffset.toCompose() = androidx.compose.ui.geometry.Offset(x, y)
private fun androidx.compose.ui.geometry.Offset.toCore() = CoreOffset(x, y)

/**
 * The main composable for the node-based editor.
 *
 * Gesture dispatch — single outer [pointerInput] hit-tests on every pointer-down
 * and routes to one of three behaviours:
 *  - **Port**: starts a pending connection; draws a live bezier to the pointer;
 *    fires [onConnectionRequest] when released over a different port.
 *  - **Node**: drags the node in world space.
 *  - **Empty**: falls through to [detectTransformGestures] for pan / zoom.
 *
 * @param state                 Hoisted state managing canvas data, selection, and viewport.
 * @param modifier              Modifier for the outer bounds of the canvas.
 * @param onConnectionAttempt   Optional veto: return `false` to reject a connection attempt
 *                              before the validator runs. Default allows all.
 * @param alignmentGuideStyle   Visual style for alignment guide lines and distance labels.
 * @param nodeContent           Slot for rendering a single node. The container is already
 *                              sized; use [Modifier.fillMaxSize] inside.
 */
@Composable
public fun BlockCanvas(
    state: BlockCanvasState,
    modifier: Modifier = Modifier,
    onConnectionAttempt: (from: EndPoint, to: EndPoint) -> Boolean = { _, _ -> true },
    alignmentGuideStyle: AlignmentGuideStyle = AlignmentGuideStyle.Default,
    nodeContent: @Composable (node: Node, isSelected: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val hitTester = remember { DefaultHitTester() }
    val onConnectionAttemptState by rememberUpdatedState(onConnectionAttempt)
    val textMeasurer = rememberTextMeasurer()
    // Reuse Path to avoid allocations in draw loop
    val sharedPath = remember { Path() }
    val decorationPath = remember { Path() }
    // Tap threshold: pointer moved less than this → treat as click, not drag
    val tapThresholdPx = 8f

    // Single shared time source for all edge animations — 0..1 cycling every second.
    // Captured as State<Float> (not `by` delegation) so .value is read inside draw
    // lambdas only — no recomposition of BlockCanvas on every animation frame.
    val infiniteTransition = rememberInfiniteTransition(label = "edgeAnim")
    val animTimeState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1000, easing = LinearEasing)),
        label = "edgeAnimTime",
    )

    Box(
        modifier = modifier
            // Pan / zoom — outer modifier, processed SECOND in Main pass.
            // detectTransformGestures checks `canceled = event.changes.any { it.isConsumed }` and
            // skips onGesture when the inner handler has already consumed the events.
            .pointerInput(state) {
                detectTransformGestures { centroid, pan, zoomFactor, _ ->
                    val afterZoom = state.viewport.withZoom(
                        newZoom = state.viewport.zoom * zoomFactor,
                        anchor = centroid.toCore(),
                    )
                    state.updateViewport(afterZoom.withPan(afterZoom.pan + pan.toCore()))
                }
            }
            // Connection drag + node drag — inner modifier, processed FIRST in Main pass.
            // Consumes events for Port/Node hits so the outer pan/zoom handler is suppressed.
            .pointerInput(state) {
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    val down = event.changes.first()
                    val isShiftPressed = event.keyboardModifiers.isShiftPressed
                    val worldPos = state.viewport.screenToWorld(down.position.toCore())

                    // 1. Edge Hit Testing (drawn in UI so hit-tested here)
                    val edgeHit = hitTestEdges(worldPos, state)
                    if (edgeHit != null) {
                        down.consume()
                        if (isShiftPressed) {
                            state.toggleSelection(edgeHit)
                        } else {
                            state.selectOnly(edgeHit)
                        }
                        return@awaitEachGesture
                    }

                    // 2. Core Hit Testing for Ports and Nodes
                    when (val hit = hitTester.hitTest(worldPos, state.canvasState.nodes.values)) {
                        is HitResult.Port -> {
                            down.consume()
                            val clickedEndpoint = EndPoint(hit.nodeId, hit.portId)

                            // Click-click mode: if a connection is already pending, this is the second tap
                            val existing = state.pendingConnection
                            if (existing != null && existing.mode == com.yasincidem.blockcanvas.ui.state.ConnectionMode.Click) {
                                state.tryCommitConnection(clickedEndpoint, onConnectionAttemptState)
                                return@awaitEachGesture
                            }

                            // Reconnection: rip off an existing edge from this port
                            val edgeToDetach = state.canvasState.edges.values.find { it.to == clickedEndpoint }
                                ?: state.canvasState.edges.values.find { it.from == clickedEndpoint }

                            val sourceEnd = if (edgeToDetach != null) {
                                state.removeEdge(edgeToDetach.id)
                                if (edgeToDetach.to == clickedEndpoint) edgeToDetach.from else edgeToDetach.to
                            } else {
                                clickedEndpoint
                            }

                            state.startPendingConnection(sourceEnd, worldPos, com.yasincidem.blockcanvas.ui.state.ConnectionMode.Drag)

                            var totalMovePx = 0f
                            var upHit: HitResult = HitResult.Empty
                            while (true) {
                                val ev = awaitPointerEvent()
                                val change = ev.changes.find { it.id == down.id } ?: break
                                change.consume()
                                val curr = state.viewport.screenToWorld(change.position.toCore())
                                state.updatePendingConnection(curr)
                                val d = change.position - change.previousPosition
                                totalMovePx += kotlin.math.sqrt(d.x * d.x + d.y * d.y)
                                if (!change.pressed) {
                                    upHit = hitTester.hitTest(curr, state.canvasState.nodes.values)
                                    break
                                }
                            }

                            when {
                                // Released on a port → commit
                                upHit is HitResult.Port -> {
                                    val to = EndPoint(upHit.nodeId, upHit.portId)
                                    state.tryCommitConnection(to, onConnectionAttemptState)
                                }
                                // Quick tap (little movement) → switch to click-click mode
                                totalMovePx < tapThresholdPx -> {
                                    state.startPendingConnection(
                                        sourceEnd,
                                        state.pendingConnection?.currentPointerWorld ?: worldPos,
                                        com.yasincidem.blockcanvas.ui.state.ConnectionMode.Click,
                                    )
                                }
                                // Dragged but released on empty → cancel
                                else -> state.clearPendingConnection()
                            }
                        }

                        is HitResult.Node -> {
                            down.consume()

                            // Multi-selection handling
                            if (isShiftPressed) {
                                state.toggleSelection(hit.nodeId)
                            } else if (!state.selectionState.isSelected(hit.nodeId)) {
                                state.selectOnly(hit.nodeId)
                            }

                            val startPositions = state.selectionState.selectedNodes.associateWith {
                                state.nodePositions[it] ?: state.canvasState.nodes[it]?.position ?: CoreOffset(0f, 0f)
                            }
                            // Pre-snap base positions so delta is always accumulated from an
                            // on-grid origin. Without this, an off-grid startPos causes a jump.
                            val basePositions = startPositions.mapValues { (_, pos) -> state.snap(pos) }
                            basePositions.forEach { (id, pos) -> state.moveNodeDuringDrag(id, pos) }
                            var totalDelta = CoreOffset(0f, 0f)

                            while (true) {
                                val dragEvent = awaitPointerEvent()
                                val change = dragEvent.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    val finalPlacements = buildMap {
                                        state.selectionState.selectedNodes.forEach { id ->
                                            val pos = state.nodePositions[id]
                                            if (pos != null) put(id, pos)
                                        }
                                    }
                                    state.commitNodePositions(finalPlacements)
                                    state.clearAlignmentResult()
                                    break
                                }
                                change.consume()
                                val delta = (change.position - change.previousPosition).toCore() / state.viewport.zoom
                                totalDelta = CoreOffset(totalDelta.x + delta.x, totalDelta.y + delta.y)

                                state.selectionState.selectedNodes.forEach { selectedId ->
                                    val basePos = basePositions[selectedId] ?: return@forEach
                                    val rawPos = CoreOffset(basePos.x + totalDelta.x, basePos.y + totalDelta.y)
                                    val snapped = state.snap(rawPos)

                                    // Compute alignment guides for the primary dragged node
                                    if (selectedId == hit.nodeId) {
                                        val draggedNode = state.canvasState.nodes[selectedId]
                                        if (draggedNode != null) {
                                            val nodeAtPos = draggedNode.copy(position = snapped)
                                            state.updateAlignmentResult(nodeAtPos)
                                            val alignSnap = state.alignmentResult?.snapOffset ?: CoreOffset.Zero
                                            val aligned = CoreOffset(snapped.x + alignSnap.x, snapped.y + alignSnap.y)
                                            state.moveNodeDuringDrag(selectedId, aligned)
                                            return@forEach
                                        }
                                    }
                                    state.moveNodeDuringDrag(selectedId, snapped)
                                }
                            }
                        }

                        HitResult.Empty -> {
                            // Cancel any active click-click connection
                            if (state.pendingConnection?.mode == com.yasincidem.blockcanvas.ui.state.ConnectionMode.Click) {
                                state.clearPendingConnection()
                                down.consume()
                                return@awaitEachGesture
                            }
                            if (!isShiftPressed) state.clearSelection()
                            /* fall through to pan/zoom */
                        }
                    }
                }
            }
    ) {
        // Background
        Box(Modifier.fillMaxSize().background(state.gridConfig.backgroundColor))

        // Grid layer — world-space: spacing scales with zoom so the grid feels
        // like part of the infinite canvas. Hidden below 4px screen spacing to
        // avoid sub-pixel clutter at very low zoom levels.
        val grid = state.gridConfig
        if (grid.type != GridType.None) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val screenSpacing = grid.spacing * state.viewport.zoom
                if (screenSpacing >= 4f) {
                    val panX = state.viewport.pan.x % screenSpacing
                    val panY = state.viewport.pan.y % screenSpacing

                    if (grid.type == GridType.Lines) {
                        var lx = if (panX > 0) panX - screenSpacing else panX
                        while (lx < size.width) {
                            drawLine(grid.gridColor, androidx.compose.ui.geometry.Offset(lx, 0f), androidx.compose.ui.geometry.Offset(lx, size.height), strokeWidth = 1f)
                            lx += screenSpacing
                        }
                        var ly = if (panY > 0) panY - screenSpacing else panY
                        while (ly < size.height) {
                            drawLine(grid.gridColor, androidx.compose.ui.geometry.Offset(0f, ly), androidx.compose.ui.geometry.Offset(size.width, ly), strokeWidth = 1f)
                            ly += screenSpacing
                        }
                    } else if (grid.type == GridType.Dots) {
                        val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        var dx = if (panX > 0) panX - screenSpacing else panX
                        while (dx < size.width) {
                            var dy = if (panY > 0) panY - screenSpacing else panY
                            while (dy < size.height) {
                                points.add(androidx.compose.ui.geometry.Offset(dx, dy))
                                dy += screenSpacing
                            }
                            dx += screenSpacing
                        }
                        drawPoints(
                            points = points,
                            pointMode = androidx.compose.ui.graphics.PointMode.Points,
                            color = grid.gridColor,
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Edge + pending + port highlights + alignment guides — one Canvas so all
        // dynamic state (nodePositions, pendingConnection, alignmentResult, animTime)
        // is read in the draw phase only. Zero BlockCanvas recompositions during drag.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val animTime = animTimeState.value  // draw-phase read — no recomposition

            // ── Edges ────────────────────────────────────────────────────────
            state.canvasState.edges.values.forEach { edge ->
                val fromNode = state.canvasState.nodes[edge.from.node] ?: return@forEach
                val toNode   = state.canvasState.nodes[edge.to.node]   ?: return@forEach
                val fromPort = fromNode.ports.find { it.id == edge.from.port } ?: return@forEach
                val toPort   = toNode.ports.find   { it.id == edge.to.port }   ?: return@forEach

                val fromPos   = state.nodePositions[fromNode.id] ?: fromNode.position
                val toPos     = state.nodePositions[toNode.id]   ?: toNode.position
                val fromWorld = computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
                val toWorld   = computePortPosition(toPos,   toNode.width,   toNode.height,   toPort.side)

                val router     = edge.router    ?: state.defaultEdgeRouter
                val edgePath   = router.route(fromWorld, toWorld, fromPort.side, toPort.side)
                val srcEnd     = edge.sourceEnd ?: state.defaultSourceEnd
                val tgtEnd     = edge.targetEnd ?: state.defaultTargetEnd
                val edgeStroke = edge.stroke    ?: state.defaultEdgeStroke
                val edgeAnim   = edge.animation ?: state.defaultEdgeAnimation
                val isSelected = state.selectionState.isSelected(edge.id)
                drawEdgePath(
                    sharedPath, decorationPath, edgePath, state.viewport,
                    srcEnd, tgtEnd, edgeStroke, edgeAnim,
                    animTime, density.density,
                    selected = isSelected,
                )
            }

            // ── Pending connection bezier ─────────────────────────────────────
            state.pendingConnection?.let { pending ->
                val fromNode  = state.canvasState.nodes[pending.from.node] ?: return@let
                val fromPort  = fromNode.ports.find { it.id == pending.from.port } ?: return@let
                val fromPos   = state.nodePositions[fromNode.id] ?: fromNode.position
                val fromWorld = computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
                val pendingPath = EdgeRouter.Bezier.route(
                    fromWorld, pending.currentPointerWorld, fromPort.side, PortSide.Left,
                )
                drawEdgePath(sharedPath, decorationPath, pendingPath, state.viewport, EdgeEnd.None, EdgeEnd.None, isPending = true)

                // ── Port highlight overlay ────────────────────────────────────
                val portLookup: (EndPoint) -> com.yasincidem.blockcanvas.core.model.Port? = { ep ->
                    state.canvasState.nodes[ep.node]?.ports?.find { it.id == ep.port }
                }
                state.canvasState.nodes.values.forEach { node ->
                    val nodePos = state.nodePositions[node.id] ?: node.position
                    node.ports.forEach { port ->
                        val ep = EndPoint(node.id, port.id)
                        if (ep == pending.from) return@forEach
                        val portWorld  = computePortPosition(nodePos, node.width, node.height, port.side)
                        val portScreen = state.viewport.worldToScreen(portWorld).toCompose()
                        val error = state.connectionValidator.validate(
                            pending.from, ep,
                            state.canvasState.edges.values, // Avoid .toList()
                            portLookup,
                        )
                        if (error == null) {
                            drawCircle(Color(0xFF5B8DEF).copy(alpha = 0.6f), 10f, portScreen, style = Stroke(width = 2f))
                            drawCircle(Color(0xFF5B8DEF).copy(alpha = 0.15f), 14f, portScreen)
                        } else {
                            drawCircle(Color.Black.copy(alpha = 0.4f), 10f, portScreen)
                        }
                    }
                }
            }

            // ── Alignment guides ─────────────────────────────────────────────
            val alignment = state.alignmentResult
            if (alignment != null && (alignment.guides.isNotEmpty() || alignment.labels.isNotEmpty())) {
                drawAlignmentGuides(
                    guides = alignment.guides,
                    labels = alignment.labels,
                    viewport = state.viewport,
                    style = alignmentGuideStyle,
                    textMeasurer = textMeasurer,
                    canvasSize = size,
                )
            }
        }

        // Node layer — viewport applied via graphicsLayer (no per-node gesture handling needed).
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = state.viewport.pan.x
                translationY = state.viewport.pan.y
                scaleX = state.viewport.zoom
                scaleY = state.viewport.zoom
                transformOrigin = TransformOrigin(0f, 0f)
            }
        ) {
            state.canvasState.nodes.values.forEach { node ->
                Box(
                    modifier = Modifier
                        .offset {
                            // Read from nodePositions (layout phase) so drag updates
                            // only re-layout this node, not recompose BlockCanvas.
                            val pos = state.nodePositions[node.id] ?: node.position
                            IntOffset(
                                x = pos.x.roundToInt(),
                                y = pos.y.roundToInt(),
                            )
                        }
                        // Size the container in dp so it matches the pixel world-space
                        // dimensions: visual px = node.width, so dp = px / density.
                        .size(
                            width  = with(density) { node.width.toDp() },
                            height = with(density) { node.height.toDp() },
                        )
                ) {
                    val isSelected = state.selectionState.isSelected(node.id)
                    nodeContent(node, isSelected)
                }
            }
        }
    }
}

private fun DrawScope.drawEdgePath(
    path: Path,
    decorationPath: Path,
    edgePath: EdgePath,
    viewport: com.yasincidem.blockcanvas.core.geometry.Viewport,
    sourceEnd: EdgeEnd = EdgeEnd.None,
    targetEnd: EdgeEnd = EdgeEnd.Arrow(),
    edgeStroke: EdgeStroke = EdgeStroke.Solid(),
    edgeAnimation: EdgeAnimation = EdgeAnimation.None,
    animTime: Float = 0f,        // shared 0..1 cycling every second
    screenDensity: Float = 1f,
    isPending: Boolean = false,
    selected: Boolean = false,
) {
    val color = if (isPending) Color(0xFF_5B8DEF).copy(alpha = 0.55f) else Color(0xFF_5B8DEF)

    path.reset()
    when (edgePath) {
        is EdgePath.Bezier -> {
            val s  = viewport.worldToScreen(edgePath.start).toCompose()
            val c1 = viewport.worldToScreen(edgePath.control1).toCompose()
            val c2 = viewport.worldToScreen(edgePath.control2).toCompose()
            val e  = viewport.worldToScreen(edgePath.end).toCompose()

            // Tangent at t=0: direction from start → control1 (or start → end if degenerate)
            val srcTanX = c1.x - s.x; val srcTanY = c1.y - s.y
            val srcLen = sqrt(srcTanX * srcTanX + srcTanY * srcTanY).coerceAtLeast(0.001f)
            val srcAngle = atan2(srcTanY / srcLen, srcTanX / srcLen)

            // Tangent at t=1: direction end → control2 reversed = control2 → end
            val tgtTanX = e.x - c2.x; val tgtTanY = e.y - c2.y
            val tgtLen = sqrt(tgtTanX * tgtTanX + tgtTanY * tgtTanY).coerceAtLeast(0.001f)
            val tgtAngle = atan2(tgtTanY / tgtLen, tgtTanX / tgtLen)

            // Inset endpoints so stroke ends at the base of the decoration
            val srcInset = decorationInset(sourceEnd)
            val tgtInset = decorationInset(targetEnd)
            val sInset = androidx.compose.ui.geometry.Offset(
                s.x + cos(srcAngle) * srcInset, s.y + sin(srcAngle) * srcInset,
            )
            val eInset = androidx.compose.ui.geometry.Offset(
                e.x - cos(tgtAngle) * tgtInset, e.y - sin(tgtAngle) * tgtInset,
            )

            path.moveTo(sInset.x, sInset.y)
            path.cubicTo(c1.x, c1.y, c2.x, c2.y, eInset.x, eInset.y)

            if (selected) drawPath(path, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 8f))

            val stroke = if (isPending) {
                Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
            } else {
                edgeStrokeToCompose(edgeStroke)
            }
            drawPath(path, color = color, style = stroke)

            if (!isPending) {
                drawEdgeDecoration(decorationPath, sourceEnd, s, srcAngle, color)
                drawEdgeDecoration(decorationPath, targetEnd, e, tgtAngle, color)

                // Animation overlay
                when (edgeAnimation) {
                    is EdgeAnimation.None -> Unit

                    is EdgeAnimation.MarchingAnts -> {
                        // Only meaningful on dashed/dotted strokes
                        if (edgeStroke is EdgeStroke.Dashed || edgeStroke is EdgeStroke.Dotted) {
                            val intervalLen = when (edgeStroke) {
                                is EdgeStroke.Dashed -> edgeStroke.dashLength + edgeStroke.gapLength
                                is EdgeStroke.Dotted -> edgeStroke.width + edgeStroke.gapLength
                                else -> 12f
                            }
                            val speedPx = edgeAnimation.speedDpPerSecond * screenDensity
                            val phase = (animTime * speedPx) % intervalLen
                            val animatedPhase = if (edgeAnimation.reverse) phase else -phase

                            val intervals = when (edgeStroke) {
                                is EdgeStroke.Dashed -> floatArrayOf(edgeStroke.dashLength, edgeStroke.gapLength)
                                is EdgeStroke.Dotted -> floatArrayOf(edgeStroke.width, edgeStroke.gapLength)
                                else -> floatArrayOf(8f, 4f)
                            }
                            val animStroke = Stroke(
                                width = edgeStroke.width,
                                cap = if (edgeStroke is EdgeStroke.Dotted) StrokeCap.Round else StrokeCap.Butt,
                                pathEffect = PathEffect.dashPathEffect(intervals, animatedPhase),
                            )
                            drawPath(path, color = color, style = animStroke)
                        }
                    }

                    is EdgeAnimation.Pulse -> {
                        val count = edgeAnimation.count.coerceAtLeast(1)
                        repeat(count) { i ->
                            // Offset each dot evenly across the [0,1] range
                            val t = ((animTime + i.toFloat() / count) % 1f)
                            val dotPos = evalBezier(s, c1, c2, e, t)
                            drawCircle(color = color, radius = edgeAnimation.dotRadius * screenDensity, center = dotPos)
                        }
                    }
                }
            }
        }
    }
}

/** De Casteljau evaluation of a cubic bezier at parameter [t]. */
private fun evalBezier(
    p0: androidx.compose.ui.geometry.Offset,
    p1: androidx.compose.ui.geometry.Offset,
    p2: androidx.compose.ui.geometry.Offset,
    p3: androidx.compose.ui.geometry.Offset,
    t: Float,
): androidx.compose.ui.geometry.Offset {
    val u = 1f - t
    val u2 = u * u; val u3 = u2 * u
    val t2 = t * t; val t3 = t2 * t
    return androidx.compose.ui.geometry.Offset(
        x = u3 * p0.x + 3 * u2 * t * p1.x + 3 * u * t2 * p2.x + t3 * p3.x,
        y = u3 * p0.y + 3 * u2 * t * p1.y + 3 * u * t2 * p2.y + t3 * p3.y,
    )
}

/**
 * Maps [EdgeStroke] to a Compose [Stroke]. Intervals are in screen space so
 * dash/dot patterns stay visually consistent at any zoom level.
 */
private fun edgeStrokeToCompose(s: EdgeStroke): Stroke = when (s) {
    is EdgeStroke.Solid  -> Stroke(width = s.width)
    is EdgeStroke.Dashed -> Stroke(
        width = s.width,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(s.dashLength, s.gapLength), 0f),
    )
    is EdgeStroke.Dotted -> Stroke(
        width = s.width,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(s.width, s.gapLength), 0f),
    )
}

/** Returns how many screen px the stroke end should be inset to sit flush with the decoration. */
private fun decorationInset(end: EdgeEnd): Float = when (end) {
    is EdgeEnd.None   -> 0f
    is EdgeEnd.Arrow  -> end.size
    is EdgeEnd.Circle -> end.radius
    is EdgeEnd.Diamond -> end.size
}

private fun DrawScope.drawEdgeDecoration(
    path: Path,
    end: EdgeEnd,
    tip: androidx.compose.ui.geometry.Offset,
    angleRad: Float,
    color: Color,
) {
    path.reset()
    when (end) {
        is EdgeEnd.None -> Unit

        is EdgeEnd.Arrow -> {
            val size = end.size
            // Arrowhead: tip at `tip`, two wing points behind
            val wingAngle = 0.45f  // ~25°
            val lx = tip.x - cos(angleRad - wingAngle) * size
            val ly = tip.y - sin(angleRad - wingAngle) * size
            val rx = tip.x - cos(angleRad + wingAngle) * size
            val ry = tip.y - sin(angleRad + wingAngle) * size
            
            path.moveTo(tip.x, tip.y)
            path.lineTo(lx, ly)
            if (end.filled) path.lineTo(rx, ry) else { path.moveTo(tip.x, tip.y); path.lineTo(rx, ry) }
            path.close()
            
            if (end.filled) drawPath(path, color = color)
            else drawPath(path, color = color, style = Stroke(width = 2f))
        }

        is EdgeEnd.Circle -> {
            val center = androidx.compose.ui.geometry.Offset(
                tip.x - cos(angleRad) * end.radius,
                tip.y - sin(angleRad) * end.radius,
            )
            if (end.filled) drawCircle(color, end.radius, center)
            else drawCircle(color, end.radius, center, style = Stroke(width = 2f))
        }

        is EdgeEnd.Diamond -> {
            val s = end.size
            // Diamond: tip forward, two sides, tail back
            val tailX = tip.x - cos(angleRad) * s * 2
            val tailY = tip.y - sin(angleRad) * s * 2
            val perpAngle = angleRad + (Math.PI / 2).toFloat()
            val lx = tip.x - cos(angleRad) * s + cos(perpAngle) * s * 0.6f
            val ly = tip.y - sin(angleRad) * s + sin(perpAngle) * s * 0.6f
            val rx = tip.x - cos(angleRad) * s - cos(perpAngle) * s * 0.6f
            val ry = tip.y - sin(angleRad) * s - sin(perpAngle) * s * 0.6f
            
            path.moveTo(tip.x, tip.y); path.lineTo(lx, ly); path.lineTo(tailX, tailY); path.lineTo(rx, ry); path.close()
            
            if (end.filled) drawPath(path, color = color)
            else drawPath(path, color = color, style = Stroke(width = 2f))
        }
    }
}

private fun DrawScope.drawAlignmentGuides(
    guides: List<com.yasincidem.blockcanvas.core.alignment.AlignmentGuide>,
    labels: List<DistanceLabel>,
    viewport: com.yasincidem.blockcanvas.core.geometry.Viewport,
    style: AlignmentGuideStyle,
    textMeasurer: TextMeasurer,
    canvasSize: androidx.compose.ui.geometry.Size,
) {
    val stroke = Stroke(width = style.lineWidth)

    for (guide in guides) {
        when (guide.axis) {
            Axis.Vertical -> {
                val screenX = viewport.worldToScreen(CoreOffset(guide.position, 0f)).x
                drawLine(
                    color = style.lineColor,
                    start = androidx.compose.ui.geometry.Offset(screenX, 0f),
                    end = androidx.compose.ui.geometry.Offset(screenX, canvasSize.height),
                    strokeWidth = style.lineWidth,
                )
            }
            Axis.Horizontal -> {
                val screenY = viewport.worldToScreen(CoreOffset(0f, guide.position)).y
                drawLine(
                    color = style.lineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, screenY),
                    end = androidx.compose.ui.geometry.Offset(canvasSize.width, screenY),
                    strokeWidth = style.lineWidth,
                )
            }
        }
    }

    for (label in labels) {
        val screenMid = viewport.worldToScreen(label.midpoint)
        val text = "${label.distance.toInt()}"
        val measured = textMeasurer.measure(text, style.labelTextStyle)
        val pad = style.labelPaddingPx
        val bgW = measured.size.width + pad * 2
        val bgH = measured.size.height + pad * 2
        val bgLeft = screenMid.x - bgW / 2f
        val bgTop = screenMid.y - bgH / 2f
        drawRoundRect(
            color = style.labelBackground,
            topLeft = androidx.compose.ui.geometry.Offset(bgLeft, bgTop),
            size = androidx.compose.ui.geometry.Size(bgW, bgH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f),
        )
        drawText(
            textLayoutResult = measured,
            topLeft = androidx.compose.ui.geometry.Offset(bgLeft + pad, bgTop + pad),
        )
    }
}

private fun hitTestEdges(
    worldPos: CoreOffset,
    state: BlockCanvasState,
    tolerancePx: Float = 10f
): EdgeId? {
    val scaledTolerance = tolerancePx / state.viewport.zoom
    val squaredTolerance = scaledTolerance * scaledTolerance

    for (edge in state.canvasState.edges.values.reversed()) {
        val fromNode = state.canvasState.nodes[edge.from.node] ?: continue
        val toNode = state.canvasState.nodes[edge.to.node] ?: continue
        val fromPort = fromNode.ports.find { it.id == edge.from.port } ?: continue
        val toPort = toNode.ports.find { it.id == edge.to.port } ?: continue

        val fromPos = state.nodePositions[fromNode.id] ?: fromNode.position
        val toPos   = state.nodePositions[toNode.id]   ?: toNode.position
        val p0 = computePortPosition(fromPos, fromNode.width, fromNode.height, fromPort.side)
        val p3 = computePortPosition(toPos,   toNode.width,   toNode.height,   toPort.side)

        val router = edge.router ?: state.defaultEdgeRouter
        val edgePath = router.route(p0, p3, fromPort.side, toPort.side)

        // Sample 21 points along the path and check squared distance
        when (edgePath) {
            is EdgePath.Bezier -> {
                val p1 = edgePath.control1
                val p2 = edgePath.control2
                for (i in 0..20) {
                    val t = i / 20f
                    val u = 1 - t
                    val u2 = u * u; val u3 = u2 * u
                    val t2 = t * t; val t3 = t2 * t
                    val x = u3 * p0.x + 3 * u2 * t * p1.x + 3 * u * t2 * p2.x + t3 * p3.x
                    val y = u3 * p0.y + 3 * u2 * t * p1.y + 3 * u * t2 * p2.y + t3 * p3.y
                    val dx = x - worldPos.x; val dy = y - worldPos.y
                    if (dx * dx + dy * dy <= squaredTolerance) return edge.id
                }
            }
        }
    }
    return null
}
