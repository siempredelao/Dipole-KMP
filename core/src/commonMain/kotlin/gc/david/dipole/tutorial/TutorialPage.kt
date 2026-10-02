package gc.david.dipole.tutorial

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack

/**
 * The pages of the tutorial, in order. Each shows [position] with the stack on [selected] picked out
 * and its [examples] drawn as arrows; [hinted] is shown as a hint would be.
 */
enum class TutorialPage(
    val position: GameState,
    val selected: Square? = null,
    val examples: List<Move> = emptyList(),
    val hinted: Square? = null,
) {

    /** The board, the starting stacks and the goal. */
    Goal(DipoleRules.initial()),

    /** Moving part of a stack: the distance is the number of checkers moved. */
    Moving(
        position = position(C3 to Stack(Player.White, 4), E7 to Stack(Player.Black, 2)),
        selected = C3,
        examples = (1..4).map { Move(C3, Direction.NorthEast, it) },
    ),

    /** Plain moves forward, a merge onto a friendly stack and a straight move with an even count. */
    PlainMoves(
        position = position(C3 to Stack(Player.White, 4), E5 to Stack(Player.White, 1), H8 to Stack(Player.Black, 2)),
        selected = C3,
        examples = listOf(
            Move(C3, Direction.NorthWest, 1),
            Move(C3, Direction.NorthEast, 2),
            Move(C3, Direction.North, 2),
        ),
    ),

    /** Captures go any direction, even backwards, onto enemy stacks no bigger than the mover. */
    Captures(
        position = position(
            C3 to Stack(Player.White, 2),
            E5 to Stack(Player.Black, 2),
            A1 to Stack(Player.Black, 1),
            C5 to Stack(Player.Black, 3),
        ),
        selected = C3,
        examples = listOf(Move(C3, Direction.NorthEast, 2), Move(C3, Direction.SouthWest, 2)),
    ),

    /** A plain move past the edge takes those checkers out of play. */
    OffBoard(
        position = position(F6 to Stack(Player.White, 3), B8 to Stack(Player.Black, 2)),
        selected = F6,
        examples = listOf(Move(F6, Direction.NorthEast, 3), Move(F6, Direction.North, 2)),
    ),

    /** Black's lone checker on b2 has no legal move, so Black sits out. */
    SittingOut(
        position = GameState(
            board = mapOf(
                A1 to Stack(Player.White, 2),
                C1 to Stack(Player.White, 2),
                H2 to Stack(Player.White, 1),
                B2 to Stack(Player.Black, 1),
            ),
            toMove = Player.Black,
        ),
        selected = B2,
    ),

    /** Hints: the recommended move blinks on the board. */
    Tips(
        position = position(C3 to Stack(Player.White, 2), E5 to Stack(Player.Black, 2), G7 to Stack(Player.Black, 1)),
        selected = C3,
        hinted = E5,
    ),
    ;
}

private fun position(vararg stacks: Pair<Square, Stack>) = GameState(mapOf(*stacks), Player.White)

private val A1 = Square(0, 0)
private val C1 = Square(0, 2)
private val B2 = Square(1, 1)
private val H2 = Square(1, 7)
private val C3 = Square(2, 2)
private val C5 = Square(4, 2)
private val E5 = Square(4, 4)
private val F6 = Square(5, 5)
private val E7 = Square(6, 4)
private val G7 = Square(6, 6)
private val B8 = Square(7, 1)
private val H8 = Square(7, 7)
