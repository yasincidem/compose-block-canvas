package com.yasincidem.blockcanvas.demo

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.yasincidem.blockcanvas.demo.nav.Destination
import com.yasincidem.blockcanvas.demo.screens.BasicsScreen
import com.yasincidem.blockcanvas.demo.screens.CustomNodesScreen
import com.yasincidem.blockcanvas.demo.screens.GalleryScreen
import com.yasincidem.blockcanvas.demo.screens.KnowledgeGraphScreen
import com.yasincidem.blockcanvas.demo.screens.PerformanceScreen
import com.yasincidem.blockcanvas.demo.screens.RulesScreen
import com.yasincidem.blockcanvas.demo.screens.SerializationScreen
import com.yasincidem.blockcanvas.demo.screens.ThemingScreen
import com.yasincidem.blockcanvas.demo.screens.WorkflowScreen
import com.yasincidem.blockcanvas.demo.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNav()
        }
    }
}

@Composable
fun AppNav() {
    val backStack = remember { mutableStateListOf<Any>(Destination.Gallery) }

    val onBack: () -> Unit = {
        backStack.removeLastOrNull()
    }

    val onNavigate: (Destination) -> Unit = {
        backStack.add(it)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            // Forward: enterTransition + exitTransition
            (slideInHorizontally(tween(500)) { it } + fadeIn(tween(500))) togetherWith
                (slideOutHorizontally(tween(500)) { -it } + fadeOut(tween(500)))
        },
        popTransitionSpec = {
            // Back (Pop): popEnterTransition + popExitTransition
            (slideInHorizontally(tween(500)) { -it } + fadeIn(tween(500))) togetherWith
                (slideOutHorizontally(tween(500)) { it } + fadeOut(tween(500)))
        },
        entryProvider = { key ->

            when (key) {
                is Destination.Gallery  -> NavEntry(key) {
                    GalleryScreen(onNavigate)
                }
                is Destination.Basics   -> NavEntry(key) {
                    BasicsScreen(onBack)
                }
                is Destination.CustomNodes -> NavEntry(key) {
                    CustomNodesScreen(onBack)
                }
                is Destination.Workflow -> NavEntry(key) {
                    WorkflowScreen(onBack)
                }
                is Destination.KnowledgeGraph -> NavEntry(key) {
                    KnowledgeGraphScreen(onBack)
                }
                is Destination.Rules -> NavEntry(key) {
                    RulesScreen(onBack)
                }
                is Destination.Serialization -> NavEntry(key) {
                    SerializationScreen(onBack)
                }
                is Destination.Theming -> NavEntry(key) {
                    ThemingScreen(onBack)
                }
                is Destination.Performance -> NavEntry(key) {
                    PerformanceScreen(onBack)
                }
                else -> NavEntry(Unit) { Text("Unknown route") }
            }
        }
    )
}
