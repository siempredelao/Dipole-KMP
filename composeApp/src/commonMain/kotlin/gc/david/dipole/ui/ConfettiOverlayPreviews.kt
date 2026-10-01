package gc.david.dipole.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession

/** The game screen with the confetti frozen [seconds] into a celebration. */
@Composable
private fun CelebrationPreview(mode: AppearanceMode, seconds: Float) {
    DipoleTheme(mode = mode) {
        Box(Modifier.fillMaxSize()) {
            PreviewScreen(DipoleUiState(GameSession.new(GameMode.VsComputer)))
            ConfettiOverlay(celebration = 1, celebrating = false, onStart = {}, previewSeconds = seconds)
        }
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiRisingPreview() = CelebrationPreview(AppearanceMode.Dark, seconds = 0.4f)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiFallingPreview() = CelebrationPreview(AppearanceMode.Dark, seconds = 1.6f)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiLightPreview() = CelebrationPreview(AppearanceMode.Light, seconds = 1f)
