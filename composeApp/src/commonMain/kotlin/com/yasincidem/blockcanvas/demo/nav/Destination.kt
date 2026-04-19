package com.yasincidem.blockcanvas.demo.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable data object Gallery : Destination
    @Serializable data object Basics : Destination
    @Serializable data object CustomNodes : Destination
    @Serializable data object Workflow : Destination
    @Serializable data object KnowledgeGraph : Destination
    @Serializable data object Rules : Destination
    @Serializable data object Serialization : Destination
    @Serializable data object Theming : Destination
    @Serializable data object Performance : Destination
}
