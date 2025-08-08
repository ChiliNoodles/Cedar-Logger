package io.github.chilinoodles.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val rootElement = document.getElementById("root") ?: document.body!!
    ComposeViewport(rootElement) {
        App()
    }
}