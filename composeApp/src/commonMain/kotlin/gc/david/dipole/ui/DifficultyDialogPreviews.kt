package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Player

@Preview
@Composable
private fun DifficultyDialogPreview() {
    DipoleTheme {
        DifficultyDialog(
            suggested = Difficulty.Medium,
            side = Player.White,
            onSideChosen = {},
            onDifficultyChosen = {},
            onBack = {},
            onDismiss = {},
        )
    }
}
