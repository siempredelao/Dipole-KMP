package gc.david.dipole.game

// The pieces of a Dipole game as plain values. What a move may do is decided by DipoleRules.

enum class Player {
    White,
    Black,
    ;

    val opponent: Player get() = if (this == White) Black else White

    /** Row direction that counts as "forward" for this player. White starts at row 0. */
    val forward: Int get() = if (this == White) 1 else -1
}

data class Square(val row: Int, val col: Int) {

    val isOnBoard: Boolean get() = row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE

    val isDark: Boolean get() = (row + col) % 2 == 0

    override fun toString(): String = "${'a' + col}${row + 1}"
}

data class Stack(val owner: Player, val size: Int) {

    init {
        require(size > 0) { "A stack needs at least one checker" }
    }
}

/** One of the eight directions a stack can move in, expressed from White's point of view. */
enum class Direction(val dRow: Int, val dCol: Int) {
    North(1, 0),
    NorthEast(1, 1),
    East(0, 1),
    SouthEast(-1, 1),
    South(-1, 0),
    SouthWest(-1, -1),
    West(0, -1),
    NorthWest(1, -1),
    ;

    val isOrthogonal: Boolean get() = dRow == 0 || dCol == 0

    fun isForwardFor(player: Player): Boolean = dRow == player.forward
}

sealed interface MoveKind {

    data object Step : MoveKind

    data object Merge : MoveKind

    data class Capture(val captured: Int) : MoveKind

    data object BearOff : MoveKind
}

/** Moves [count] checkers from the top of the stack on [from] in [direction]. */
data class Move(val from: Square, val direction: Direction, val count: Int) {

    val to: Square get() = Square(from.row + direction.dRow * count, from.col + direction.dCol * count)
}

/** A position: the stacks on the board and whose turn it is. */
data class GameState(
    val board: Map<Square, Stack>,
    val toMove: Player,
)

const val BOARD_SIZE = 8
