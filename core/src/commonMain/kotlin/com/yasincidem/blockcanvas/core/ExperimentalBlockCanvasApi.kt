package com.yasincidem.blockcanvas.core

/**
 * Marks declarations that are **experimental** in compose-block-canvas.
 *
 * APIs annotated with this marker are subject to change — including signature,
 * behaviour, or removal — in any release prior to v1.0 and do not follow the
 * project's binary-compatibility guarantees.
 *
 * Consumers must opt in explicitly, either at the usage site with
 * `@OptIn(ExperimentalBlockCanvasApi::class)` or at the module level via the
 * Kotlin compiler argument `-opt-in=com.yasincidem.blockcanvas.core.ExperimentalBlockCanvasApi`.
 *
 * @since 0.1.0
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental compose-block-canvas API. " +
        "It may change or be removed without notice before 1.0.",
)
public annotation class ExperimentalBlockCanvasApi
