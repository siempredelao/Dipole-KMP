package dev.siempredelao.dipole.game

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

class ComputerPlayerTest {

    private val freeCapture = GameState(
        board = mapOf(
            Square(2, 2) to Stack(Player.White, 2), // c3
            Square(4, 4) to Stack(Player.Black, 1), // e5
            Square(6, 0) to Stack(Player.Black, 4), // a7
        ),
        toMove = Player.White,
    )

    @Test
    fun everyDifficultyPlaysLegalMoves() {
        Difficulty.entries.forEach { difficulty ->
            val computer = ComputerPlayer.forDifficulty(difficulty, Random(3))
            var state = GameState.initial()
            repeat(4) {
                val move = computer.chooseMove(state)!!
                assertTrue(state.isLegal(move), "$difficulty played illegal $move")
                state = state.play(move)
            }
        }
    }

    @Test
    fun mediumAndHardTakeAFreeCapture() {
        listOf(Difficulty.Medium, Difficulty.Hard).forEach {
            assertEquals(Square(4, 4), ComputerPlayer.forDifficulty(it, Random(1)).chooseMove(freeCapture)?.to)
        }
    }

    @Test
    fun alwaysRandomPlayerStillPlaysLegalMoves() {
        val computer = ComputerPlayer(depth = 1, randomMoveChance = 1.0, random = Random(5))
        val moves = (1..20).map { computer.chooseMove(GameState.initial())!! }
        assertTrue(moves.all { GameState.initial().isLegal(it) })
        assertTrue(moves.toSet().size > 1, "Expected different random moves")
    }

    @Test
    fun mediumBeatsEasy() {
        val games = 6
        val mediumWins = (0 until games).count { game ->
            val medium = ComputerPlayer.forDifficulty(Difficulty.Medium, Random(game))
            val easy = ComputerPlayer.forDifficulty(Difficulty.Easy, Random(game + 100))
            val mediumIsWhite = game % 2 == 0
            var state = GameState.initial()
            while (!state.isOver) {
                val whiteToMove = state.toMove == Player.White
                state = state.play((if (whiteToMove == mediumIsWhite) medium else easy).chooseMove(state)!!)
            }
            (state.winner == Player.White) == mediumIsWhite
        }
        assertTrue(mediumWins >= games - 1, "Medium won only $mediumWins of $games")
    }

    @Test
    fun hintRecommendsTheCaptureFromTheTappedStack() {
        val hint = ComputerPlayer.forDifficulty(Difficulty.Hard).hint(freeCapture, Square(2, 2))
        assertEquals(Move(Square(2, 2), Direction.NorthEast, 2), hint?.move)
        assertEquals(false, hint?.betterMoveElsewhere)
    }

    @Test
    fun hintSaysWhenAnotherStackHasABetterMove() {
        // The lone checker on a1 can't capture anything, while the stack on c3 can take e5.
        val state = GameState(freeCapture.board + (Square(0, 0) to Stack(Player.White, 1)), Player.White)
        val hint = ComputerPlayer(depth = 1).hint(state, Square(0, 0))
        assertEquals(Square(0, 0), hint?.move?.from)
        assertEquals(true, hint?.betterMoveElsewhere)
    }

    @Test
    fun noHintForAStackThatCannotMove() {
        val computer = ComputerPlayer.forDifficulty(Difficulty.Hard)
        assertEquals(null, computer.hint(freeCapture, Square(4, 4))) // Black's stack, White to move
        assertEquals(null, computer.hint(freeCapture, Square(1, 1))) // empty square
    }

    @Test
    fun hintsIgnoreEasyRandomness() {
        val easy = ComputerPlayer(depth = 2, randomMoveChance = 1.0, random = Random(4))
        assertEquals(Square(4, 4), easy.hint(freeCapture, Square(2, 2))?.move?.to)
    }

    @Test
    fun deeperSearchStopsWhenTimeRunsOut() {
        // With no time at all it still finishes the minimum depth and returns a move.
        val computer = ComputerPlayer(depth = 1, maxDepth = 20, timeBudget = Duration.ZERO, random = Random(2))
        val move = computer.chooseMove(GameState.initial())
        assertTrue(move != null && GameState.initial().isLegal(move))
    }
}
