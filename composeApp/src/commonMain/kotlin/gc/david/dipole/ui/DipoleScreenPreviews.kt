package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenPreview() {
    DipoleTheme {
        DipoleScreen(DipoleUiState(GameSession.new(GameMode.VsComputer)), onAction = {})
    }
}

/** A position part way through a game, so the off-board stacks have checkers in them. */
private val midGame = GameState(
    board = mapOf(
        Square(0, 2) to Stack(Player.White, 5), // c1
        Square(3, 5) to Stack(Player.White, 3), // f4
        Square(7, 3) to Stack(Player.Black, 4), // d8
        Square(4, 4) to Stack(Player.Black, 2), // e5
        Square(5, 1) to Stack(Player.Black, 1), // b6
    ),
    toMove = Player.White,
)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenMidGamePreview() {
    DipoleTheme {
        DipoleScreen(
            DipoleUiState(GameSession.new(GameMode.TwoPlayers, initial = midGame), selected = Square(3, 5)),
            onAction = {},
        )
    }
}

/** Hints switched on with the f4 stack selected, showing its recommended move. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenHintPreview() {
    val selected = Square(3, 5)
    DipoleTheme {
        DipoleScreen(
            DipoleUiState(
                session = GameSession.new(GameMode.VsComputer, initial = midGame),
                selected = selected,
                hintsOn = true,
                hint = ComputerPlayer(depth = 2).hint(midGame, selected),
            ),
            onAction = {},
        )
    }
}

/** Playing Black against the computer: the board is turned round so Black sits at the bottom. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenAsBlackPreview() {
    val session = GameSession.new(GameMode.VsComputer, humanSide = Player.Black)
        .play(Move(GameState.WHITE_START, Direction.NorthEast, 3))
    DipoleTheme {
        DipoleScreen(DipoleUiState(session, selected = GameState.BLACK_START), onAction = {})
    }
}
