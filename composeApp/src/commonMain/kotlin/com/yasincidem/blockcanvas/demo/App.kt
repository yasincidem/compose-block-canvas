package com.yasincidem.blockcanvas.demo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
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
            val backStack = remember { mutableStateListOf<Destination>(Destination.Gallery) }
            val current = backStack.last()
            var isNavigatingBack by remember { mutableStateOf(false) }

            val onBack: () -> Unit = {
                if (backStack.size > 1) {
                    isNavigatingBack = true
                    backStack.removeAt(backStack.lastIndex)
                }
            }
            val onNavigate: (Destination) -> Unit = {
                isNavigatingBack = false
                backStack.add(it)
            }

            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    if (isNavigatingBack) {
                        // Back: incoming (Gallery) from left, outgoing slides to right
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    } else {
                        // Forward: incoming from right, outgoing slides to left
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    }
                },
                label = "nav",
            ) { destination ->
                when (destination) {
                    Destination.Gallery        -> GalleryScreen(onNavigate = onNavigate)
                    Destination.Basics         -> BasicsScreen(onBack = onBack)
                    Destination.CustomNodes    -> CustomNodesScreen(onBack = onBack)
                    Destination.Workflow       -> WorkflowScreen(onBack = onBack)
                    Destination.KnowledgeGraph -> KnowledgeGraphScreen(onBack = onBack)
                    Destination.Rules          -> RulesScreen(onBack = onBack)
                    Destination.Serialization  -> SerializationScreen(onBack = onBack)
                    Destination.Theming        -> ThemingScreen(onBack = onBack)
                    Destination.Performance    -> PerformanceScreen(onBack = onBack)
                }
            }
        }
    }
}
