package dev.siempredelao.dipole

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.siempredelao.dipole.ui.DipoleApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport { DipoleApp() }
}
