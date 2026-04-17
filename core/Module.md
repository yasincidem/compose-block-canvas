# Module block-canvas-core

Pure Kotlin core of **compose-block-canvas**: models, state, geometry, and
connection rules for a node-based editor. Contains no Compose dependency and
can be consumed from any Kotlin Multiplatform source set.

## Contents

- **Models** — `Node`, `Port`, `Edge` and their typed identifiers.
- **State** — immutable `CanvasState` with intent-style transitions.
- **Geometry** — platform-independent `Offset`, `Rect`, and viewport math.
- **Rules** — pluggable `ConnectionValidator` enforcing direction, self-loop,
  and duplicate-edge checks for v0.1.
- **Hit testing** — deterministic `HitTester` returning sealed `HitResult`.

## Stability

Symbols marked with `@ExperimentalBlockCanvasApi` may change without following
the project's semantic-versioning contract. All other public symbols are
binary-compatible within a minor version.
