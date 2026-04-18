package com.yasincidem.blockcanvas.core.persistence

import com.yasincidem.blockcanvas.core.state.CanvasState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val canvasJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Serializes the current [CanvasState] to a JSON string.
 * Performs the operation on [Dispatchers.Default] to keep the caller thread responsive.
 */
public suspend fun CanvasState.encodeToJson(): String = withContext(Dispatchers.Default) {
    canvasJson.encodeToString(this@encodeToJson)
}

/**
 * Deserializes a [CanvasState] from the given JSON string.
 * Performs the operation on [Dispatchers.Default].
 */
public suspend fun String.decodeToCanvasState(): CanvasState = withContext(Dispatchers.Default) {
    canvasJson.decodeFromString<CanvasState>(this@decodeToCanvasState)
}
