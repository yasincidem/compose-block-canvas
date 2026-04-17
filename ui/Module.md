# Module block-canvas-ui

Compose Multiplatform UI for **compose-block-canvas**.

Renders a pannable, zoomable canvas of slot-based nodes, draws bezier edges
between ports, and handles drag / connection gestures on Android and Desktop
(JVM). Pure rendering and interaction only — state and rules live in
[`block-canvas-core`][core].

## Theming

Colors, shapes, and typography are exposed through `BlockCanvasTheme` and
`CompositionLocal` tokens so consumers can apply their product's theme
without forking the library.

## Stability

Symbols marked with `@ExperimentalBlockCanvasApi` (declared in `:core`) may
change without notice before v1.0. All other public symbols are binary-
compatible within a minor version.

[core]: ../core/Module.md
