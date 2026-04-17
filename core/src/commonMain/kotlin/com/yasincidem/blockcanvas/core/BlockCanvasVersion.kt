package com.yasincidem.blockcanvas.core

/**
 * Runtime version identifier for compose-block-canvas.
 *
 * Useful for diagnostics, crash reports, or conditional behaviour when an
 * application bundles multiple library versions. The value follows Semantic
 * Versioning 2.0 (`MAJOR.MINOR.PATCH`, optionally suffixed with a pre-release
 * identifier such as `-alpha01`).
 *
 * ```
 * println("Running compose-block-canvas ${BlockCanvasVersion.CURRENT}")
 * ```
 *
 * @since 0.1.0
 */
public object BlockCanvasVersion {
    /**
     * The version of this compose-block-canvas artifact.
     *
     * Kept in sync with the Gradle publication version; update both together.
     */
    public const val CURRENT: String = "0.1.0-SNAPSHOT"
}
