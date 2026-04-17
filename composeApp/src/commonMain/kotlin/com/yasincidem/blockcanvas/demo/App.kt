package com.yasincidem.blockcanvas.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                Text("compose-block-canvas demo — v0.1 scaffold")
            }
        }
    }
}
