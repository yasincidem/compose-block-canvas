package com.yasincidem.blockcanvas.core

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Canary test verifying the `commonTest` source set of `:core` compiles and
 * runs on every configured Kotlin Multiplatform target.
 *
 * The test intentionally asserts against a real public symbol
 * ([BlockCanvasVersion.CURRENT]) rather than a tautology so a broken test
 * classpath surfaces immediately, not as a silent pass.
 */
class InfrastructureTest {

    @Test
    fun version_is_semver_shaped() {
        val version = BlockCanvasVersion.CURRENT
        val semverPrefix = Regex("""^\d+\.\d+\.\d+""")
        assertTrue(
            actual = semverPrefix.containsMatchIn(version),
            message = "BlockCanvasVersion.CURRENT='$version' does not start with MAJOR.MINOR.PATCH",
        )
    }
}
