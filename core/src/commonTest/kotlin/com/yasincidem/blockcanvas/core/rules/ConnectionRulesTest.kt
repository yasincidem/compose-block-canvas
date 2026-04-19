package com.yasincidem.blockcanvas.core.rules

import com.yasincidem.blockcanvas.core.model.Edge
import com.yasincidem.blockcanvas.core.model.EdgeId
import com.yasincidem.blockcanvas.core.model.EndPoint
import com.yasincidem.blockcanvas.core.model.NodeId
import com.yasincidem.blockcanvas.core.model.Port
import com.yasincidem.blockcanvas.core.model.PortId
import com.yasincidem.blockcanvas.core.model.PortSide
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class ConnectionRulesTest {

    private fun ep(node: String, port: String) = EndPoint(NodeId(node), PortId(port))
    private fun edge(id: String, from: EndPoint, to: EndPoint) =
        Edge(EdgeId(id), from, to)
    private val noLookup: (EndPoint) -> Port? = { Port(it.port, PortSide.Right) }
    private val noEdges = emptyList<Edge>()

    // ── MaxEdgesPerPort ───────────────────────────────────────────────────────

    @Test
    fun `MaxEdgesPerPort allows edge when count is below limit`() {
        val rule = MaxEdgesPerPortRule(maxPerPort = 2)
        val from = ep("a", "out")
        val to   = ep("b", "in")
        val existing = listOf(edge("e1", from, ep("c", "x")))

        assertNull(rule.validate(from, to, existing, noLookup))
    }

    @Test
    fun `MaxEdgesPerPort blocks when from-port is at limit`() {
        val rule = MaxEdgesPerPortRule(maxPerPort = 1)
        val from = ep("a", "out")
        val to   = ep("b", "in")
        val existing = listOf(edge("e1", from, ep("c", "x")))

        assertIs<ConnectionError.MaxEdgesExceeded>(
            rule.validate(from, to, existing, noLookup)
        )
    }

    @Test
    fun `MaxEdgesPerPort blocks when to-port is at limit`() {
        val rule = MaxEdgesPerPortRule(maxPerPort = 1)
        val from = ep("a", "out")
        val to   = ep("b", "in")
        val existing = listOf(edge("e1", ep("c", "x"), to))

        assertIs<ConnectionError.MaxEdgesExceeded>(
            rule.validate(from, to, existing, noLookup)
        )
    }

    @Test
    fun `MaxEdgesPerPort allows when no existing edges`() {
        val rule = MaxEdgesPerPortRule(maxPerPort = 1)
        val from = ep("a", "out")
        val to   = ep("b", "in")

        assertNull(rule.validate(from, to, noEdges, noLookup))
    }

    // ── CompositeConnectionValidator ──────────────────────────────────────────

    @Test
    fun `CompositeConnectionValidator passes when all rules pass`() {
        val validator = CompositeConnectionValidator(
            DefaultConnectionValidator(),
            MaxEdgesPerPortRule(maxPerPort = 3),
        )
        val from = ep("a", "out")
        val to   = ep("b", "in")

        assertNull(validator.validate(from, to, noEdges, noLookup))
    }

    @Test
    fun `CompositeConnectionValidator returns first error`() {
        val validator = CompositeConnectionValidator(
            DefaultConnectionValidator(),
            MaxEdgesPerPortRule(maxPerPort = 0),
        )
        val from = ep("a", "out")
        val to   = ep("b", "in")

        assertIs<ConnectionError.MaxEdgesExceeded>(
            validator.validate(from, to, noEdges, noLookup)
        )
    }

    @Test
    fun `CompositeConnectionValidator catches self-loop before MaxEdges`() {
        val validator = CompositeConnectionValidator(
            DefaultConnectionValidator(),
            MaxEdgesPerPortRule(maxPerPort = 0),
        )
        val same = ep("a", "out")

        assertIs<ConnectionError.SelfLoop>(
            validator.validate(same, same, noEdges, noLookup)
        )
    }
}
