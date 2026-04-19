package com.yasincidem.blockcanvas.demo.nav

sealed interface Destination {
    data object Gallery : Destination
    data object Basics : Destination
    data object CustomNodes : Destination
    data object Workflow : Destination
    data object KnowledgeGraph : Destination
    data object Rules : Destination
    data object Serialization : Destination
    data object Theming : Destination
    data object Performance : Destination
}
