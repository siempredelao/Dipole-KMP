package dev.siempredelao.dipole.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GameSessionTest {

    private val whiteOpening = Move(GameState.WHITE_START, Direction.NorthEast, 3)
    private val blackReply = Move(GameState.BLACK_START, Direction.SouthWest, 2)
    private val whiteSecond = Move(GameState.WHITE_START, Direction.North, 2)

    @Test
    fun newSessionStartsFromTheInitialPosition() {
        val session = GameSession.new(GameMode.TwoPlayers)
        assertEquals(GameState.initial(), session.state)
        assertTrue(session.moves.isEmpty())
        assertNull(session.lastMove)
        assertFalse(session.canUndo)
    }

    @Test
    fun playingRecordsTheMoveAndThePosition() {
        val session = GameSession.new(GameMode.TwoPlayers).play(whiteOpening)
        assertEquals(listOf(whiteOpening), session.moves)
        assertEquals(whiteOpening, session.lastMove)
        assertEquals(GameState.initial().play(whiteOpening), session.state)
    }

    @Test
    fun undoInTwoPlayerModeTakesBackOneMove() {
        val session = GameSession.new(GameMode.TwoPlayers).play(whiteOpening).play(blackReply)
        val undone = session.undo()
        assertEquals(listOf(whiteOpening), undone.moves)
        assertEquals(Player.Black, undone.state.toMove)
    }

    @Test
    fun undoAgainstTheComputerAlsoTakesBackItsReply() {
        val session = GameSession.new(GameMode.VsComputer).play(whiteOpening).play(blackReply)
        val undone = session.undo()
        assertTrue(undone.moves.isEmpty())
        assertEquals(GameState.initial(), undone.state)
    }

    @Test
    fun undoWithNoMovesDoesNothing() {
        val session = GameSession.new(GameMode.TwoPlayers)
        assertSame(session, session.undo())
    }

    @Test
    fun computerTurnOnlyInComputerMode() {
        assertTrue(GameSession.new(GameMode.VsComputer).play(whiteOpening).isComputerTurn)
        assertFalse(GameSession.new(GameMode.TwoPlayers).play(whiteOpening).isComputerTurn)
        assertFalse(GameSession.new(GameMode.VsComputer).play(whiteOpening).canUndo)
    }

    @Test
    fun replayRebuildsTheSameGame() {
        val moves = listOf(whiteOpening, blackReply, whiteSecond)
        val played = moves.fold(GameSession.new(GameMode.TwoPlayers)) { s, m -> s.play(m) }
        val replayed = GameSession.replay(GameMode.TwoPlayers, moves)
        assertEquals(played.state, replayed?.state)
        assertEquals(moves, replayed?.moves)
    }

    @Test
    fun difficultyIsKeptThroughPlayUndoAndReplay() {
        val session = GameSession.new(GameMode.VsComputer, Difficulty.Hard).play(whiteOpening)
        assertEquals(Difficulty.Hard, session.difficulty)
        assertEquals(Difficulty.Hard, session.undo().difficulty)
        assertEquals(Difficulty.Hard, GameSession.replay(GameMode.VsComputer, session.moves, Difficulty.Hard)?.difficulty)
        assertEquals(Difficulty.Medium, GameSession.new(GameMode.VsComputer).difficulty)
    }

    @Test
    fun replayRejectsIllegalMoves() {
        assertNull(GameSession.replay(GameMode.TwoPlayers, listOf(blackReply)))
    }

    @Test
    fun opponentSatOutWhenTheMoverGoesAgain() {
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
        val session = GameSession.new(GameMode.TwoPlayers, initial = start).play(Move(Square(1, 7), Direction.NorthWest, 1))
        assertTrue(session.opponentSatOut)
    }
}
