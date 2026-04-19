package com.yasincidem.blockcanvas.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val accentColor: Color,
)

private val entries = listOf(
    ShowcaseEntry(Destination.Basics,         "Basics",          "Default nodes, edges, pan & zoom",                   "⬡",  Color(0xFF5B8DEF)),
    ShowcaseEntry(Destination.CustomNodes,    "Custom Nodes",    "Image, form, long-text, rich-card node types",       "🎨", Color(0xFFE25B8D)),
    ShowcaseEntry(Destination.Workflow,       "Workflow",        "AI pipeline with typed ports & marching-ant edges",  "⚡", Color(0xFF8DE25B)),
    ShowcaseEntry(Destination.KnowledgeGraph, "Knowledge Graph", "Concept map with pill nodes",                        "🔗", Color(0xFFE2C45B)),
    ShowcaseEntry(Destination.Rules,          "Connection Rules","Live MaxEdgesPerPort & acyclic validators",          "🛡", Color(0xFFBB5BE2)),
    ShowcaseEntry(Destination.Serialization,  "Serialization",   "Export & import canvas state as JSON",               "💾", Color(0xFF5BE2D8)),
    ShowcaseEntry(Destination.Theming,        "Theming",         "Dark, light and neon/synthwave themes",              "🌈", Color(0xFFE2815B)),
    ShowcaseEntry(Destination.Performance,    "Performance",     "100 / 500 / 1000 node stress test with FPS meter",   "🚀", Color(0xFF5BDEF5)),
)

@Composable
fun GalleryScreen(onNavigate: (Destination) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        DemoTopBar(title = "Block Canvas — Showcase")
        Text(
            text = "Tap a card to explore a feature.",
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, entry.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = entry.icon,
                fontSize = 32.sp,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = entry.accentColor,
            )
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
