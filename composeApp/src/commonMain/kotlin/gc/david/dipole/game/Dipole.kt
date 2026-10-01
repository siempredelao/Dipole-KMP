package gc.david.dipole.game

/**
 * Rules of Dipole, by Mark Steere (https://www.marksteeregames.com/Dipole_rules.pdf).
 *
 * - 8x8 checkerboard, only dark squares are used (a1 is dark).
 * - Each player starts with one stack of 12 checkers. White moves first.
 * - A stack, or part of it taken from the top, moves exactly as many squares as it has checkers.
 *   Movement is never obstructed by intervening stacks.
 * - Non-capturing moves (to an empty square, merging onto a friendly stack, or out of bounds)
 *   go forward or diagonally forward only. Moving straight (orthogonally) is only possible with
 *   an even number of checkers, as odd distances end on a light square.
 * - Captures can go in any of the eight directions and take an entire enemy stack of equal or
 *   smaller size.
 * - A stack moved out of bounds (forward or diagonally forward) is removed from play.
 * - Moving is mandatory; a player without moves sits out until a move becomes available.
 * - A player wins when all of the opponent's checkers have been removed.
 */
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

data class GameState(
    val board: Map<Square, Stack>,
    val toMove: Player,
) {
    fun checkersOf(player: Player): Int = board.values.filter { it.owner == player }.sumOf { it.size }

    /** Checkers of [player] taken out of play, whether captured or moved off the board. */
    fun removedCheckersOf(player: Player): Int = STARTING_STACK - checkersOf(player)

    val winner: Player?
        get() = when {
            checkersOf(Player.White) == 0 -> Player.Black
            checkersOf(Player.Black) == 0 -> Player.White
            else -> null
        }

    val isOver: Boolean get() = winner != null

    fun kindOf(move: Move): MoveKind? {
        val stack = board[move.from] ?: return null
        if (stack.owner != toMove || move.count !in 1..stack.size) return null
        // Straight moves of odd length would end on a light square (or leave the board from one).
        if (move.direction.isOrthogonal && move.count % 2 != 0) return null
        val forward = move.direction.isForwardFor(toMove)
        val to = move.to
        if (!to.isOnBoard) return if (forward) MoveKind.BearOff else null
        val target = board[to]
        return when {
            target == null -> if (forward) MoveKind.Step else null
            target.owner == toMove -> if (forward) MoveKind.Merge else null
            target.size <= move.count -> MoveKind.Capture(target.size)
            else -> null
        }
    }

    fun isLegal(move: Move): Boolean = kindOf(move) != null

    fun legalMoves(): List<Move> {
        if (isOver) return emptyList()
        return board.filterValues { it.owner == toMove }.flatMap { (square, stack) -> legalMovesFrom(square, stack) }
    }

    fun legalMovesFrom(square: Square): List<Move> {
        val stack = board[square] ?: return emptyList()
        if (stack.owner != toMove || isOver) return emptyList()
        return legalMovesFrom(square, stack)
    }

    private fun legalMovesFrom(square: Square, stack: Stack): List<Move> =
        (1..stack.size).flatMap { count ->
            Direction.entries.map { Move(square, it, count) }.filter(::isLegal)
        }

    /**
     * Plays [move] and returns the resulting state. If the next player has no legal move they sit
     * out, so the turn passes straight back to the mover.
     */
    fun play(move: Move): GameState {
        val kind = requireNotNull(kindOf(move)) { "Illegal move $move" }
        val mover = board.getValue(move.from)
        val newBoard = board.toMutableMap()
        val remaining = mover.size - move.count
        if (remaining == 0) newBoard.remove(move.from) else newBoard[move.from] = mover.copy(size = remaining)
        when (kind) {
            MoveKind.Step, is MoveKind.Capture -> newBoard[move.to] = Stack(toMove, move.count)
            MoveKind.Merge -> newBoard[move.to] = Stack(toMove, newBoard.getValue(move.to).size + move.count)
            MoveKind.BearOff -> Unit
        }
        val next = GameState(newBoard, toMove.opponent)
        return if (!next.isOver && next.legalMoves().isEmpty()) next.copy(toMove = toMove) else next
    }

    companion object {
        const val STARTING_STACK = 12

        // Both stacks sit just left of centre from White's point of view (per the official rules).
        val WHITE_START = Square(0, 2) // c1
        val BLACK_START = Square(7, 3) // d8

        fun initial(): GameState = GameState(
            board = mapOf(
                WHITE_START to Stack(Player.White, STARTING_STACK),
                BLACK_START to Stack(Player.Black, STARTING_STACK),
            ),
            toMove = Player.White,
        )
    }
}

const val BOARD_SIZE = 8
