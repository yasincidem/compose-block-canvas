> WIP: Compose Block Canvas is currently under active development and the API is still evolving.

# Compose Block Canvas

Compose Block Canvas is a Kotlin Multiplatform toolkit for building node-based editors with Jetpack Compose and Compose Multiplatform.

The project is focused on the primitives behind editors such as workflow builders, knowledge graphs, automation canvases, and visual programming tools: draggable nodes, connectable ports, zoomable canvases, edge rendering, and customizable node content.

## Demo

Demo video will live here once it is added to the repository.

```md
[demo.mp4](./docs/media/demo.mp4)
```

If you want to embed it later, replace this section with the final asset link or a GitHub-hosted video URL.

## Status

This project is under active development.

- The API is still evolving and may change before `1.0`.
- The library is not published yet.
- The demo app in this repository is currently the main reference for usage and design direction.

Right now the implementation is centered on Android and Desktop (JVM). Broader multiplatform coverage, including iOS, is planned but not finished yet.

## Current scope

- Pure Kotlin core models and immutable canvas state
- Compose-based rendering and interaction layer
- Pan and zoom
- Node dragging
- Port-to-port connections
- Edge styling and animation
- Selection, deletion, edge reconnection, and undo/redo
- Grid rendering and snap-to-grid
- JSON serialization for canvas state

## Modules

- `:core`  
  Pure Kotlin module with models, geometry, canvas state, rules, builders, and serialization.

- `:ui`  
  Compose Multiplatform UI module that renders nodes and edges, handles gestures, and works with hoisted state from `:core`.

- `:composeApp`  
  Demo/showcase application used to validate the API and interaction model.

## Running the demo

To run the desktop demo:

```bash
./gradlew :composeApp:desktopRun
```

For Android, open the project in Android Studio and run the `composeApp` target.

## Example: build a canvas

The core module provides a small DSL for building `CanvasState` instances:

```kotlin
import com.yasincidem.blockcanvas.core.builder.buildCanvasState
import com.yasincidem.blockcanvas.core.builder.linksTo
import com.yasincidem.blockcanvas.core.builder.style
import com.yasincidem.blockcanvas.core.model.EdgeAnimation
import com.yasincidem.blockcanvas.core.model.EdgeEnd
import com.yasincidem.blockcanvas.core.model.EdgeStroke
import com.yasincidem.blockcanvas.core.model.PortSide

val canvas = buildCanvasState {
    node("input") {
        at(x = 120f, y = 180f)
        size(width = 160f, height = 88f)
        port("out", PortSide.Right)
    }

    node("output") {
        rightOf("input", gap = 96f)
        size(width = 160f, height = 88f)
        port("in", PortSide.Left)
    }

    connect("input", "out") linksTo connect("output", "in") style {
        stroke = EdgeStroke.Dashed(width = 2f, dashLength = 10f, gapLength = 6f)
        targetEnd = EdgeEnd.Arrow(size = 10f)
        animation = EdgeAnimation.MarchingAnts(speedDpPerSecond = 60f)
    }
}
```

## Example: render it with Compose

The UI module keeps state hoisted and leaves node visuals to your own composables:

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.ui.BlockCanvas
import com.yasincidem.blockcanvas.ui.state.GridConfig
import com.yasincidem.blockcanvas.ui.state.rememberBlockCanvasState

@Composable
fun Editor(canvas: CanvasState) {
    val state = rememberBlockCanvasState(
        initialCanvasState = canvas,
        gridConfig = GridConfig(snapToGrid = true),
    )

    BlockCanvas(
        state = state,
        modifier = Modifier.fillMaxSize(),
        nodeContent = { node, isSelected, _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isSelected) Color(0xFF2A2A3E) else Color(0xFF1B1B2B),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF5B8DEF),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = node.id.value, color = Color.White)
            }
        },
    )
}
```

## Example: serialize a canvas

Canvas state can be exported and restored as JSON:

```kotlin
import com.yasincidem.blockcanvas.core.state.CanvasState
import com.yasincidem.blockcanvas.core.persistence.decodeToCanvasState
import com.yasincidem.blockcanvas.core.persistence.encodeToJson

suspend fun roundTrip(canvas: CanvasState) {
    val json = canvas.encodeToJson()
    val restored = json.decodeToCanvasState()

    println(restored.nodes.size)
}
```

## Design direction

The project is being built with a few constraints in mind:

- library-first rather than app-first
- clear separation between core logic and UI
- extensible node rendering through slots instead of fixed templates
- deterministic interactions that can be tested
- API ergonomics over feature count

If you are exploring the project today, the best place to start is the demo app and the examples under [`composeApp`](/Users/yasincidem/AndroidStudioProjects/compose-block-canvas/composeApp).
