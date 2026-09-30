package dev.siempredelao.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.saves.SavedGame

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
