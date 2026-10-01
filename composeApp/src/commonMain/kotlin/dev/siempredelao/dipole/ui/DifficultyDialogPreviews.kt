package dev.siempredelao.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.siempredelao.dipole.game.Difficulty

@Preview
@Composable
private fun DifficultyDialogPreview() {
    DipoleTheme {
        DifficultyDialog(suggested = Difficulty.Medium, onDifficultyChosen = {}, onBack = {}, onDismiss = {})
    }
}
