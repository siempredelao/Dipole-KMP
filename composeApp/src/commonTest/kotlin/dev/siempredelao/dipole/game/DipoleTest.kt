package dev.siempredelao.dipole.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DipoleTest {

    private fun sq(name: String) = Square(row = name[1] - '1', col = name[0] - 'a')

    private fun state(toMove: Player = Player.White, vararg stacks: Pair<String, Stack>) =
        GameState(stacks.associate { (square, stack) -> sq(square) to stack }, toMove)

    private fun white(size: Int) = Stack(Player.White, size)
    private fun black(size: Int) = Stack(Player.Black, size)

    @Test
    fun initialPositionHasTwelveCheckersEachOnDarkSquares() {
        val initial = GameState.initial()
        assertEquals(12, initial.checkersOf(Player.White))
        assertEquals(12, initial.checkersOf(Player.Black))
        assertEquals(Player.White, initial.toMove)
        assertTrue(initial.board.keys.all { it.isDark })
    }

    @Test
    fun everyLegalMoveEndsOnADarkSquareOrOffTheBoard() {
        val moves = GameState.initial().legalMoves()
        assertTrue(moves.isNotEmpty())
        assertTrue(moves.all { !it.to.isOnBoard || it.to.isDark })
    }

    @Test
    fun movedCheckersTravelExactlyTheirCount() {
        val s = state(Player.White, "c1" to white(12), "h8" to black(1))
        val after = s.play(Move(sq("c1"), Direction.NorthEast, 3))
        assertEquals(white(9), after.board[sq("c1")])
        assertEquals(white(3), after.board[sq("f4")])
        assertEquals(Player.Black, after.toMove)
    }

    @Test
    fun straightMovesNeedAnEvenCount() {
        val s = state(Player.White, "c1" to white(12), "h8" to black(1))
        assertFalse(s.isLegal(Move(sq("c1"), Direction.North, 3)))
        assertTrue(s.isLegal(Move(sq("c1"), Direction.North, 4)))
    }

    @Test
    fun nonCapturingMovesOnlyGoForward() {
        val s = state(Player.White, "d4" to white(4), "h8" to black(1))
        assertFalse(s.isLegal(Move(sq("d4"), Direction.SouthEast, 1)))
        assertFalse(s.isLegal(Move(sq("d4"), Direction.East, 2)))
        assertFalse(s.isLegal(Move(sq("d4"), Direction.South, 2)))
        assertTrue(s.isLegal(Move(sq("d4"), Direction.NorthWest, 1)))
    }

    @Test
    fun blackMovesForwardTowardsRowOne() {
        val s = state(Player.Black, "d8" to black(12), "a1" to white(1))
        assertTrue(s.isLegal(Move(sq("d8"), Direction.SouthWest, 2)))
        assertFalse(s.isLegal(Move(sq("d8"), Direction.NorthEast, 1)))
    }

    @Test
    fun capturesWorkInAnyDirectionAgainstEqualOrSmallerStacks() {
        val s = state(Player.White, "e5" to white(3), "b2" to black(3), "e3" to black(1), "g7" to black(3))
        assertEquals(MoveKind.Capture(3), s.kindOf(Move(sq("e5"), Direction.SouthWest, 3)))
        // Two backwards moves to e3, but moving 2 checkers is enough to take a stack of one.
        assertEquals(MoveKind.Capture(1), s.kindOf(Move(sq("e5"), Direction.South, 2)))
        // g7 is two squares away, so only two checkers arrive: not enough for a stack of three.
        assertNull(s.kindOf(Move(sq("e5"), Direction.NorthEast, 2)))
    }

    @Test
    fun captureReplacesTheEnemyStack() {
        val s = state(Player.White, "e5" to white(3), "b2" to black(3), "h8" to black(1))
        val after = s.play(Move(sq("e5"), Direction.SouthWest, 3))
        assertEquals(white(3), after.board[sq("b2")])
        assertNull(after.board[sq("e5")])
        assertEquals(1, after.checkersOf(Player.Black))
    }

    @Test
    fun movementIsNeverObstructed() {
        val s = state(Player.White, "a1" to white(3), "b2" to black(5), "c3" to white(2), "h8" to black(1))
        assertEquals(MoveKind.Step, s.kindOf(Move(sq("a1"), Direction.NorthEast, 3)))
    }

    @Test
    fun mergingOntoAFriendlyStack() {
        val s = state(Player.White, "a1" to white(2), "c3" to white(4), "h8" to black(1))
        assertEquals(MoveKind.Merge, s.kindOf(Move(sq("a1"), Direction.NorthEast, 2)))
        assertEquals(white(6), s.play(Move(sq("a1"), Direction.NorthEast, 2)).board[sq("c3")])
        // Merging is not a capture, so it can't go backwards.
        assertNull(s.kindOf(Move(sq("c3"), Direction.SouthWest, 2)))
    }

    @Test
    fun stacksMovedOutOfBoundsAreRemoved() {
        val s = state(Player.White, "g7" to white(3), "a1" to white(1), "b8" to black(1))
        assertEquals(MoveKind.BearOff, s.kindOf(Move(sq("g7"), Direction.North, 2)))
        assertEquals(MoveKind.BearOff, s.kindOf(Move(sq("g7"), Direction.NorthEast, 3)))
        val after = s.play(Move(sq("g7"), Direction.NorthEast, 3))
        assertEquals(1, after.checkersOf(Player.White))
        // Backwards off the board is not allowed.
        assertNull(s.kindOf(Move(sq("a1"), Direction.SouthWest, 1)))
    }

    @Test
    fun capturingTheLastEnemyStackWins() {
        val s = state(Player.White, "c3" to white(2), "e5" to black(2))
        val after = s.play(Move(sq("c3"), Direction.NorthEast, 2))
        assertEquals(Player.White, after.winner)
        assertTrue(after.legalMoves().isEmpty())
    }

    @Test
    fun bearingOffYourLastCheckerLoses() {
        val s = state(Player.White, "h8" to white(1), "a1" to black(1))
        assertEquals(Player.Black, s.play(Move(sq("h8"), Direction.NorthWest, 1)).winner)
    }

    @Test
    fun playerWithoutMovesSitsOut() {
        // Black's single checker on b2 is boxed in: a1 and c1 hold bigger white stacks and moving
        // one checker straight is impossible. It can't capture either, so White moves again.
        val s = state(Player.White, "a1" to white(2), "c1" to white(2), "h2" to white(1), "b2" to black(1))
        assertTrue(GameState(s.board, Player.Black).legalMoves().isEmpty())
        val after = s.play(Move(sq("h2"), Direction.NorthWest, 1))
        assertEquals(Player.White, after.toMove)
    }

    @Test
    fun illegalMovesAreRejected() {
        val s = GameState.initial()
        assertFailsWith<IllegalArgumentException> { s.play(Move(GameState.BLACK_START, Direction.South, 2)) }
        assertFailsWith<IllegalArgumentException> { s.play(Move(GameState.WHITE_START, Direction.North, 13)) }
    }
}
