package com.yasincidem.blockcanvas.ui.state

import com.yasincidem.blockcanvas.core.geometry.Offset
import com.yasincidem.blockcanvas.core.model.EndPoint

public enum class ConnectionMode {
    /** Pointer held down while drawing — cancelled on release with no target. */
    Drag,
    /** Quick tap on a port; connection stays alive until a second port is tapped. */
    Click,
}

/**
 * Transient state for a connection being drawn — from a source port to the
 * current pointer position in world space. Cleared when the connection is
 * committed or cancelled.
 */
public data class PendingConnection(
    val from: EndPoint,
    val currentPointerWorld: Offset,
    val mode: ConnectionMode = ConnectionMode.Drag,
)
