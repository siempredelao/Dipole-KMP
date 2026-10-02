package gc.david.dipole.screenshots

import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
import gc.david.dipole.ui.GameDialog
import gc.david.dipole.ui.GameUiState

/** The games shown in the Play Store screenshots, kept apart from the drawing so a test can check them. */
internal object StoreScreenshotStates {

    /**
     * A game against the computer with White to move. White has just played c1 to f4 and the
     * computer answered c7 to b6, so Undo is on and the last move is marked.
     */
    val midGame: GameSession = play(
        GameState(
            board = mapOf(
                Square(0, 2) to Stack(Player.White, 8), // c1
                Square(2, 2) to Stack(Player.White, 4), // c3
                Square(4, 4) to Stack(Player.Black, 2), // e5
                Square(6, 2) to Stack(Player.Black, 1), // c7
                Square(7, 3) to Stack(Player.Black, 4), // d8
            ),
            toMove = Player.White,
        ),
        Move(Square(0, 2), Direction.NorthEast, 3),
        Move(Square(6, 2), Direction.SouthWest, 1),
    )

    /** The c3 stack picked, showing where it can go, including a capture of e5. */
    val game = GameUiState(midGame, selected = Square(2, 2))

    /** Hints on with the c3 stack picked: its best move, taking e5, is highlighted. Searching takes a moment. */
    fun hint(): GameUiState {
        val selected = Square(2, 2)
        return GameUiState(midGame, selected = selected, hintsOn = true, hint = ComputerPlayer().hint(midGame.state, selected))
    }

    /** New game against the computer: picking a colour and a difficulty. */
    val newGame = GameUiState(
        GameSessions.new(GameMode.VsComputer),
        dialog = GameDialog.ChooseDifficulty(suggested = Difficulty.Medium, side = Player.White),
    )

    /** White has just taken Black's last stack, capturing e5 from e3. */
    val won = GameUiState(
        play(
            GameState(
                board = mapOf(
                    Square(1, 1) to Stack(Player.White, 2), // b2
                    Square(2, 4) to Stack(Player.White, 3), // e3
                    Square(5, 3) to Stack(Player.White, 2), // d6
                    Square(4, 4) to Stack(Player.Black, 2), // e5
                ),
                toMove = Player.White,
            ),
            Move(Square(2, 4), Direction.North, 2),
        ),
    )

    /** The appearance dialog over the game. */
    val appearance = GameUiState(midGame, dialog = GameDialog.Appearance)

    /** A game against the computer from [start], with [moves] played and each one checked. */
    private fun play(start: GameState, vararg moves: Move): GameSession =
        moves.fold(GameSessions.new(GameMode.VsComputer, initial = start)) { session, move ->
            require(DipoleRules.isLegal(session.state, move)) { "$move is not legal in ${session.state}" }
            GameSessions.play(session, move)
        }
}
