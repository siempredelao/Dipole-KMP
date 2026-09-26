package dev.siempredelao.dipole

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.siempredelao.dipole.ui.DipoleApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Dipole",
        state = rememberWindowState(width = 640.dp, height = 900.dp),
    ) {
        DipoleApp()
    }
}
