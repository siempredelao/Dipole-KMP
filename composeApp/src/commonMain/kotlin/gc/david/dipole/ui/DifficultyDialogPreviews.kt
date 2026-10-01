package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.game.Difficulty

@Preview
@Composable
private fun DifficultyDialogPreview() {
    DipoleTheme {
        DifficultyDialog(suggested = Difficulty.Medium, onDifficultyChosen = {}, onBack = {}, onDismiss = {})
    }
}
