# Canvas DSL & Persistence Guide

This document describes how to programmatically build a canvas state and how to handle its lifecycle via JSON serialization.

## 1. Programmable DSL

The `buildCanvasState` DSL provides a human-readable way to initialize your canvas without worrying about low-level ID wrappers or manual coordinate math.

```kotlin
val state = buildCanvasState {
    // Define a node
    node("n1") {
        at(x = 100f, y = 100f)
        size(width = 200f, height = 100f)
        port("out", PortSide.Right)
    }

    // Define another node relative to the first
    node("n2") {
        rightOf("n1", gap = 100f) // No manual math needed!
        port("in", PortSide.Left)
    }

    // Connect them
    connect("n1", "out") linksTo connect("n2", "in")
}
```

### Key DSL Features:
- **String IDs**: Use simple strings for Node and Port IDs.
- **Relative Positioning**: Use `rightOf(nodeId)` and `below(nodeId)` for architectural layouts.
- **Infix Links**: Use `linksTo` for a natural connection syntax.

---

## 2. JSON Persistence

Compose Block Canvas uses `kotlinx-serialization` for high-performance state persistence.

### High-Performance Parsing (Coroutines)
To keep your UI stutter-free, serialization helpers are `suspend` functions that automatically run on `Dispatchers.Default`.

#### Saving State
```kotlin
val jsonString = state.canvasState.encodeToJson()
// Now save jsonString to a file or database
```

#### Loading State
```kotlin
val savedJson: String = ... // load from disk
val restoredState = savedJson.decodeToCanvasState()
```

### Modularization Note
The persistence logic is bundled within the `core` module but separated into the `com.yasincidem.blockcanvas.core.persistence` package to keep the core models clean.
