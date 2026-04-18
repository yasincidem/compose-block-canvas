package com.yasincidem.blockcanvas.core.model

/**
 * The side of a node's bounding box on which a port sits.
 *
 * Port positions are derived from the owning node's geometry plus this value,
 * so the model stays purely structural and requires no separate coordinate
 * storage.
 *
 * @since 0.1.0
 */
public enum class PortSide { Top, Right, Bottom, Left }
