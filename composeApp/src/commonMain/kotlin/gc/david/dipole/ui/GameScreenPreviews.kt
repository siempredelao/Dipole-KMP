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
private fun GameScreenPreview() {
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(GameUiState(GameSession.new(GameMode.VsComputer)))
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
private fun GameScreenMidGamePreview() {
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(
            GameUiState(GameSession.new(GameMode.TwoPlayers, initial = midGame), selected = Square(3, 5))
        )
    }
}

/** Hints switched on with the f4 stack selected, showing its recommended move. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenHintPreview() {
    val selected = Square(3, 5)
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(
            GameUiState(
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
private fun GameScreenAsBlackPreview() {
    val session = GameSession.new(GameMode.VsComputer, humanSide = Player.Black)
        .play(Move(GameState.WHITE_START, Direction.NorthEast, 3))
    DipoleTheme(mode = AppearanceMode.Dark) {
        PreviewScreen(GameUiState(session, selected = GameState.BLACK_START))
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenLightPreview() = MidGameWith(AppearanceMode.Light, BoardTheme.Wood)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenTournamentPreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.Tournament)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenSlatePreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.Slate)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenMarblePreview() = MidGameWith(AppearanceMode.Light, BoardTheme.Marble)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun GameScreenHighContrastPreview() = MidGameWith(AppearanceMode.Dark, BoardTheme.HighContrast)

/**
 * The mid-game position plus a White stack on c3 that is selected, so plain targets and a capture
 * (of e5) show in [board]'s colours.
 */
@Composable
private fun MidGameWith(mode: AppearanceMode, board: BoardTheme) {
    val position = midGame.copy(board = midGame.board + (Square(2, 2) to Stack(Player.White, 4)))
    DipoleTheme(mode, board) {
        PreviewScreen(
            GameUiState(
                session = GameSession.new(GameMode.TwoPlayers, initial = position),
                selected = Square(2, 2),
            )
        )
    }
}

/** The game screen with default settings and no actions, for previews. */
@Composable
internal fun PreviewScreen(uiState: GameUiState) {
    GameScreen(uiState, SettingsUiState(), onAction = {}, onSettingsAction = {}, onRulesClick = {})
}
