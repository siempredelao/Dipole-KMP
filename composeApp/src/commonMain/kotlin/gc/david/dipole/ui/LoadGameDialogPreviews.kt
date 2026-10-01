package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.saves.SavedGame

@Preview
@Composable
private fun LoadGameDialogPreview() {
    val opening = Move(GameState.WHITE_START, Direction.NorthEast, 3)
    DipoleTheme {
        LoadGameDialog(
            savedGames = listOf(
                SavedGame("1", "Close game", 1_790_000_000_000, GameMode.VsComputer, listOf(opening)),
                SavedGame("2", "With Ana", 1_789_000_000_000, GameMode.TwoPlayers, emptyList()),
            ),
            onLoad = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}
