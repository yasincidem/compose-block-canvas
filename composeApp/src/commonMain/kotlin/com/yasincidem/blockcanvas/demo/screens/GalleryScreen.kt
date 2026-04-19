package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yasincidem.blockcanvas.demo.nav.Destination
import com.yasincidem.blockcanvas.demo.nav.DemoTopBar

private data class ShowcaseEntry(
    val destination: Destination,
    val title: String,
    val description: String,
    val icon: String,
)

private val entries = listOf(
    ShowcaseEntry(Destination.Basics,         "Basics",             "Default nodes, edges, pan & zoom",                  "⬡"),
    ShowcaseEntry(Destination.CustomNodes,    "Custom Nodes",       "Image, form, long-text, rich-card node types",      "🎨"),
    ShowcaseEntry(Destination.Workflow,       "Workflow",           "AI pipeline with typed ports & marching-ant edges", "⚡"),
    ShowcaseEntry(Destination.KnowledgeGraph, "Knowledge Graph",    "Concept map with pill nodes",                       "🔗"),
    ShowcaseEntry(Destination.Rules,          "Connection Rules",   "Live MaxEdgesPerPort & acyclic validators",         "🛡"),
    ShowcaseEntry(Destination.Serialization,  "Serialization",      "Export & import canvas state as JSON",              "💾"),
    ShowcaseEntry(Destination.Theming,        "Theming",            "Dark, light and neon/synthwave themes",             "🌈"),
    ShowcaseEntry(Destination.Performance,    "Performance",        "100 / 500 / 1000 node stress test with FPS meter",  "🚀"),
)

@Composable
fun GalleryScreen(onNavigate: (Destination) -> Unit) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
        DemoTopBar(title = "Block Canvas")
        Text(
            text = "Tap a card to explore a feature.",
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(entries) { entry ->
                ShowcaseCard(entry = entry, onClick = { onNavigate(entry.destination) })
            }
        }
    }
}

@Composable
private fun ShowcaseCard(entry: ShowcaseEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.icon,
                fontSize = 28.sp,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
