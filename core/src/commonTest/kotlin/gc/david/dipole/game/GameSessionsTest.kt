package gc.david.dipole.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GameSessionsTest {

    private val whiteOpening = Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)
    private val blackReply = Move(DipoleRules.BLACK_START, Direction.SouthWest, 2)
    private val whiteSecond = Move(DipoleRules.WHITE_START, Direction.North, 2)

    @Test
    fun newSessionStartsFromTheInitialPosition() {
        val session = GameSessions.new(GameMode.TwoPlayers)
        assertEquals(DipoleRules.initial(), session.state)
        assertTrue(session.moves.isEmpty())
        assertNull(session.lastMove)
        assertFalse(GameSessions.canUndo(session))
    }

    @Test
    fun playingRecordsTheMoveAndThePosition() {
        val session = GameSessions.play(GameSessions.new(GameMode.TwoPlayers), whiteOpening)
        assertEquals(listOf(whiteOpening), session.moves)
        assertEquals(whiteOpening, session.lastMove)
        assertEquals(DipoleRules.play(DipoleRules.initial(), whiteOpening), session.state)
    }

    @Test
    fun undoInTwoPlayerModeTakesBackOneMove() {
        val session = played(GameSessions.new(GameMode.TwoPlayers), whiteOpening, blackReply)
        val undone = GameSessions.undo(session)
        assertEquals(listOf(whiteOpening), undone.moves)
        assertEquals(Player.Black, undone.state.toMove)
    }

    @Test
    fun undoAgainstTheComputerAlsoTakesBackItsReply() {
        val session = played(GameSessions.new(GameMode.VsComputer), whiteOpening, blackReply)
        val undone = GameSessions.undo(session)
        assertTrue(undone.moves.isEmpty())
        assertEquals(DipoleRules.initial(), undone.state)
    }

    @Test
    fun undoWithNoMovesDoesNothing() {
        val session = GameSessions.new(GameMode.TwoPlayers)
        assertSame(session, GameSessions.undo(session))
    }

    @Test
    fun computerTurnOnlyInComputerMode() {
        assertTrue(GameSessions.isComputerTurn(played(GameSessions.new(GameMode.VsComputer), whiteOpening)))
        assertFalse(GameSessions.isComputerTurn(played(GameSessions.new(GameMode.TwoPlayers), whiteOpening)))
        assertFalse(GameSessions.canUndo(played(GameSessions.new(GameMode.VsComputer), whiteOpening)))
    }

    @Test
    fun replayRebuildsTheSameGame() {
        val moves = listOf(whiteOpening, blackReply, whiteSecond)
        val oneByOne = played(GameSessions.new(GameMode.TwoPlayers), *moves.toTypedArray())
        val replayed = GameSessions.replay(GameMode.TwoPlayers, moves)
        assertEquals(oneByOne.state, replayed?.state)
        assertEquals(moves, replayed?.moves)
    }

    @Test
    fun difficultyIsKeptThroughPlayUndoAndReplay() {
        val session = played(GameSessions.new(GameMode.VsComputer, Difficulty.Hard), whiteOpening)
        assertEquals(Difficulty.Hard, session.difficulty)
        assertEquals(Difficulty.Hard, GameSessions.undo(session).difficulty)
        assertEquals(Difficulty.Hard, GameSessions.replay(GameMode.VsComputer, session.moves, Difficulty.Hard)?.difficulty)
        assertEquals(Difficulty.Medium, GameSessions.new(GameMode.VsComputer).difficulty)
    }

    @Test
    fun computerMovesFirstWhenTheHumanPlaysBlack() {
        val session = GameSessions.new(GameMode.VsComputer, humanSide = Player.Black)
        assertTrue(GameSessions.isComputerTurn(session))
        val afterComputer = GameSessions.play(session, whiteOpening)
        assertFalse(GameSessions.isComputerTurn(afterComputer))
        // Only the computer has moved, so there is nothing of the human's to take back.
        assertFalse(GameSessions.canUndo(afterComputer))
        assertSame(afterComputer, GameSessions.undo(afterComputer))
    }

    @Test
    fun undoAsBlackGoesBackToBlacksLastTurn() {
        val session = played(
            GameSessions.new(GameMode.VsComputer, humanSide = Player.Black),
            whiteOpening,
            blackReply,
            whiteSecond,
        )
        assertTrue(GameSessions.canUndo(session))
        val undone = GameSessions.undo(session)
        assertEquals(listOf(whiteOpening), undone.moves)
        assertEquals(Player.Black, undone.state.toMove)
        assertEquals(Player.Black, undone.humanSide)
    }

    @Test
    fun replayRejectsIllegalMoves() {
        assertNull(GameSessions.replay(GameMode.TwoPlayers, listOf(blackReply)))
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
        val session = GameSessions.play(GameSessions.new(GameMode.TwoPlayers, initial = start), Move(Square(1, 7), Direction.NorthWest, 1))
        assertTrue(GameSessions.opponentSatOut(session))
    }

    private fun played(session: GameSession, vararg moves: Move): GameSession = moves.fold(session, GameSessions::play)
}
