package com.yasincidem.blockcanvas.ui

import com.yasincidem.blockcanvas.core.BlockCanvasVersion
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Canary test verifying the `:ui` `commonTest` source set compiles, that the
 * `:core` dependency is exposed through `api(...)`, and that the test runner
 * executes on every configured Kotlin Multiplatform target.
 */
class InfrastructureTest {

    @Test
    fun core_version_is_accessible_from_ui_module() {
        assertTrue(
            actual = BlockCanvasVersion.CURRENT.isNotBlank(),
            message = "BlockCanvasVersion.CURRENT should be reachable from :ui via api(projects.core)",
        )
    }
}
