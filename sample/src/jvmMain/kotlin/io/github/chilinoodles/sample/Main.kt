package io.github.chilinoodles.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Cedar Logger JVM") {
        App()
    }
}