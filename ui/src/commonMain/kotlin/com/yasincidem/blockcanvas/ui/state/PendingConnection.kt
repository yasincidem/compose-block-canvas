package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.EndPoint

/**
 * Transient state for a connection being drawn — from a source port to the
 * current pointer position in world space. Cleared when the drag ends.
 */
public data class PendingConnection(
    val from: EndPoint,
    val currentPointerWorld: Offset,
)
