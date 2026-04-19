# AGENTS.md

## Project Overview

**Project Name:** Compose Block Canvas
**Type:** Kotlin Multiplatform Library
**Purpose:**
A node-based editor toolkit for Jetpack Compose and Compose Multiplatform.
Enables developers to build interactive workflows, knowledge graphs, and visual editors using draggable blocks, connectable ports, and a zoomable canvas.

---

## Core Principles

* Library-first, not app-first
* UI and logic must be decoupled
* Multiplatform compatibility is required
* Performance and scalability are first-class concerns
* API ergonomics is more important than feature count

---

## Architecture

The project is structured into modular layers:

### 1. `compose-blocks-core`

* Pure Kotlin (no Compose dependency)
* Contains:

    * Node / Edge / Port models
    * Canvas state
    * Selection logic
    * Geometry utilities
    * Connection rules
    * Serialization models

### 2. `compose-blocks-ui`

* Compose Multiplatform module
* Responsible for:

    * Rendering nodes and edges
    * Gesture handling (drag, zoom, pan)
    * Hit testing
    * Visual feedback (selection, hover, connection)

### 3. `demo`

* Showcase application
* Demonstrates usage of the library
* Not part of the public API

---

## Design Guidelines

### State Management

* State must be hoisted
* Prefer immutable models
* Use unidirectional data flow
* Avoid hidden internal state when possible

### API Design

* Prefer composable slots over rigid APIs
* Users must be able to fully customize node UI
* Provide both:

    * low-level primitives
    * high-level convenience components

### Separation of Concerns

* Core module must not depend on UI
* UI module must not contain business logic
* Rendering and interaction logic should be separated

---

## Interaction Model

The canvas must support:

* Pan and zoom
* Node dragging
* Port-based connections
* Edge rendering
* Node selection (single & multi)
* Hit testing (node vs port vs canvas)

All interactions must be deterministic and testable.

---

## Performance Requirements

* Must handle 100+ nodes without noticeable lag
* Avoid unnecessary recompositions
* Use efficient drawing (Canvas where needed)
* Minimize allocation during gestures

---

## Multiplatform Targets

Must support:

* Android
* Desktop (JVM)
* iOS (via Compose Multiplatform)

Web support is optional (future scope)

---

## Naming Conventions

* Public APIs must be explicit and descriptive
* Internal classes should be clearly marked
* Avoid abbreviations unless widely accepted

---

## Contribution Guidelines

* Do not introduce breaking API changes without discussion
* Keep functions small and focused
* Add documentation for all public APIs
* Prefer clarity over cleverness

---

## Roadmap (High-Level)

### v0.1

* Basic canvas
* Node rendering
* Drag support
* Edge drawing

### v0.2

* Multi-selection
* Edge reconnection
* Snap-to-grid

### v0.3

* Serialization (import/export)
* Undo/redo

### v1.0

* Stable API
* Documentation
* Maven Central release

---

## Non-Goals (for now)

* AI workflow execution engine
* Complex runtime graph evaluation
* Domain-specific node types
* Visual scripting language

---

## Philosophy

This project is not just a UI library.

It is a foundational toolkit for building visual editors in Compose.

Focus on primitives, extensibility, and developer experience.


<claude-mem-context>
# Memory Context

# [compose-block-canvas] recent context, 2026-04-19 6:29pm GMT+3

No previous sessions found.
</claude-mem-context>