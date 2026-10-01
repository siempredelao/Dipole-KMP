package gc.david.dipole

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import gc.david.dipole.ui.DipoleApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport { DipoleApp() }
}
