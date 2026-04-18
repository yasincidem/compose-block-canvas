package com.yasincidem.blockcanvas.ui.state

/**
 * Semantic zoom level derived from the current canvas scale.
 *
 * Use [LodLevel.of] inside your `nodeContent` slot to hide expensive
 * composables (labels, icons, buttons) when the canvas is zoomed out.
 *
 * Example:
 * ```kotlin
 * nodeContent = { node, isSelected, scale ->
 *     val lod = LodLevel.of(scale)
 *     if (lod >= LodLevel.MEDIUM) {
 *         Text(node.label)
 *     }
 * }
 * ```
 */
public enum class LodLevel {
    /** scale < 0.15 — render only a minimal placeholder (dot / tiny rect). */
    MINIMAL,

    /** 0.15 ≤ scale < 0.4 — solid colored block, no text. */
    LOW,

    /** 0.4 ≤ scale < 0.8 — title / ports visible, details hidden. */
    MEDIUM,

    /** scale ≥ 0.8 — all details visible. */
    FULL;

    public companion object {
        public fun of(scale: Float): LodLevel = when {
            scale >= 0.8f -> FULL
            scale >= 0.4f -> MEDIUM
            scale >= 0.15f -> LOW
            else -> MINIMAL
        }
    }
}
