package com.yasincidem.blockcanvas.demo.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yasincidem.blockcanvas.demo.nav.DemoTopBar

@Composable
fun ShowcaseScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    controls: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
        DemoTopBar(title = title, onBack = onBack)
        if (description.isNotBlank()) {
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            content = controls,
        )
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}
