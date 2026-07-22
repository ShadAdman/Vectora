package com.vectora.doc

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.getElementById("compose-receiver") ?: return
    ComposeViewport(body) {
        App()
    }
}
