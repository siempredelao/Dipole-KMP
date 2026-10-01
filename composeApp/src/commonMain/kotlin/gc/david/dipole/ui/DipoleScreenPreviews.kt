package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
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
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(DipoleUiState(GameSession.new(GameMode.VsComputer)))
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
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(
            DipoleUiState(GameSession.new(GameMode.TwoPlayers, initial = midGame), selected = Square(3, 5))
        )
    }
}

/** Hints switched on with the f4 stack selected, showing its recommended move. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenHintPreview() {
    val selected = Square(3, 5)
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(
            DipoleUiState(
                session = GameSession.new(GameMode.VsComputer, initial = midGame),
                selected = selected,
                hintsOn = true,
                hint = ComputerPlayer(depth = 2).hint(midGame, selected),
            )
        )
    }
}

/** Playing Black against the computer: the board is turned round so Black sits at the bottom. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenAsBlackPreview() {
    val session = GameSession.new(GameMode.VsComputer, humanSide = Player.Black)
        .play(Move(GameState.WHITE_START, Direction.NorthEast, 3))
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(DipoleUiState(session, selected = GameState.BLACK_START))
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenLightPreview() = MidGameWith(AppearanceMode.Light, BoardTheme.Wood)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenTournamentPreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.Tournament)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenSlatePreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.Slate)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenMarblePreview() = MidGameWith(AppearanceMode.Light, BoardTheme.Marble)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenHighContrastPreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.HighContrast)

/**
 * The mid-game position plus a White stack on c3 that is selected, so plain targets and a capture
 * (of e5) show in [board]'s colours.
 */
@Composable
private fun MidGameWith(mode: AppearanceMode, board: BoardTheme) {
    val position = midGame.copy(board = midGame.board + (Square(2, 2) to Stack(Player.White, 4)))
    DipoleTheme(mode, board) {
        PreviewScreen(
            DipoleUiState(
                session = GameSession.new(GameMode.TwoPlayers, initial = position),
                selected = Square(2, 2),
            )
        )
    }
}

/** The game screen with default settings and no actions, for previews. */
@Composable
internal fun PreviewScreen(uiState: DipoleUiState) {
    DipoleScreen(uiState, SettingsUiState(), onAction = {}, onSettingsAction = {}, onRulesClick = {})
}
