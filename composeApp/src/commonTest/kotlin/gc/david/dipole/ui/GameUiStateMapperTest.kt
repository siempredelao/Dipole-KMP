package gc.david.dipole.ui

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameUiStateMapperTest {

    private fun map(mode: GameMode, toMove: Player, vararg board: Pair<Square, Stack>, selected: Square? = null) =
        GameUiStateMapper.map(GameUiState(GameSessions.new(mode, initial = GameState(mapOf(*board), toMove)), selected))

    @Test
    fun aNewGameOffersTheStartingStack() {
        val ui = GameUiStateMapper.map(GameUiState(GameSessions.new(GameMode.TwoPlayers)))
        assertEquals(setOf(DipoleRules.WHITE_START), ui.movable)
        assertTrue(ui.targets.isEmpty())
        assertFalse(ui.canUndo)
        assertTrue(ui.canHint)
        assertEquals(GameStatus(), ui.status)
        assertEquals(mapOf(Player.White to 12, Player.Black to 12), ui.checkersOnBoard)
        assertEquals(mapOf(Player.White to 0, Player.Black to 0), ui.removedCheckers)
    }

    @Test
    fun nothingIsMovableWhileTheComputerThinks() {
        val session = GameSessions.new(GameMode.VsComputer, humanSide = Player.Black)
        val ui = GameUiStateMapper.map(GameUiState(session))
        assertTrue(ui.movable.isEmpty())
        assertFalse(ui.canHint)
        assertTrue(ui.status.computerThinking)
    }

    @Test
    fun theSelectedStackShowsItsTargetsAndCaptures() {
        val c3 = Square(2, 2)
        val d4 = Square(3, 3)
        val e5 = Square(4, 4)
        val ui = map(GameMode.TwoPlayers, Player.White, c3 to Stack(Player.White, 4), e5 to Stack(Player.Black, 2), selected = c3)
        assertEquals(Move(c3, Direction.NorthEast, 1), ui.targets[d4])
        assertEquals(Move(c3, Direction.NorthEast, 2), ui.targets[e5])
        assertEquals(setOf(e5), ui.captureTargets)
    }

    @Test
    fun movesOffTheBoardAreListedApart() {
        val g7 = Square(6, 6)
        val ui = map(GameMode.TwoPlayers, Player.White, g7 to Stack(Player.White, 2), Square(7, 0) to Stack(Player.Black, 1), selected = g7)
        assertTrue(ui.bearOffs.isNotEmpty())
        assertTrue(ui.bearOffs.none { it.to.isOnBoard })
        assertTrue(ui.targets.keys.all { it.isOnBoard })
        assertTrue(Square(7, 7) in ui.targets)
    }

    @Test
    fun aFinishedGameReportsTheWinner() {
        val ui = map(GameMode.TwoPlayers, Player.Black, Square(0, 0) to Stack(Player.White, 3))
        assertEquals(Player.White, ui.status.winner)
        assertTrue(ui.movable.isEmpty())
        assertFalse(ui.canHint)
        assertEquals(0, ui.checkersOnBoard[Player.Black])
        assertEquals(12, ui.removedCheckers[Player.Black])
    }

    @Test
    fun aSkippedPlayerIsReported() {
        // Black's lone checker on b2 is boxed in, so after White moves it is White's turn again.
        val start = GameState(
            board = mapOf(
                Square(0, 0) to Stack(Player.White, 2),
                Square(0, 2) to Stack(Player.White, 2),
                Square(1, 7) to Stack(Player.White, 1),
                Square(1, 1) to Stack(Player.Black, 1),
            ),
            toMove = Player.White,
        )
        val session = GameSessions.play(GameSessions.new(GameMode.TwoPlayers, initial = start), Move(Square(1, 7), Direction.NorthWest, 1))
        assertEquals(Player.Black, GameUiStateMapper.map(GameUiState(session)).status.satOut)
        assertNull(GameUiStateMapper.map(GameUiState(GameSessions.new(GameMode.TwoPlayers))).status.satOut)
    }
}
