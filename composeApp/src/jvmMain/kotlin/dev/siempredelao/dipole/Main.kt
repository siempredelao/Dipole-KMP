package dev.siempredelao.dipole

import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.siempredelao.dipole.ui.DipoleApp
import javax.imageio.ImageIO

private val appIcon = object {}.javaClass.getResourceAsStream("/icon.png")?.use { ImageIO.read(it) }?.toPainter()

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Dipole",
        icon = appIcon,
        state = rememberWindowState(width = 640.dp, height = 900.dp),
    ) {
        DipoleApp()
    }
}
