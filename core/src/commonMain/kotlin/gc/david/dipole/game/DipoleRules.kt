/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Dipole, the game and its rules, was designed by Mark Steere
 * (Copyright (c) May 2007 by Mark Steere, https://www.marksteeregames.com).
 * This code, an implementation of the game, was developed by David Aguiar Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
object DipoleRules {

    const val STARTING_STACK = 12

    // As in Mark Steere's rules sheet, confirmed by him on 11 October 2026.
    val WHITE_START = Square(0, 4) // e1
    val BLACK_START = Square(7, 3) // d8

    /** Where White started in versions before 1.1, kept so games saved then still load. */
    val OLD_WHITE_START = Square(0, 2) // c1

    /** The starting position; [whiteStart] differs only for games saved with [OLD_WHITE_START]. */
    fun initial(whiteStart: Square = WHITE_START): GameState = GameState(
        board = mapOf(
            whiteStart to Stack(Player.White, STARTING_STACK),
            BLACK_START to Stack(Player.Black, STARTING_STACK),
        ),
        toMove = Player.White,
    )

    fun checkersOf(state: GameState, player: Player): Int =
        state.board.values.filter { it.owner == player }.sumOf { it.size }

    /** Checkers of [player] taken out of play, whether captured or moved off the board. */
    fun removedCheckersOf(state: GameState, player: Player): Int = STARTING_STACK - checkersOf(state, player)

    fun winner(state: GameState): Player? = when {
        checkersOf(state, Player.White) == 0 -> Player.Black
        checkersOf(state, Player.Black) == 0 -> Player.White
        else -> null
    }

    fun isOver(state: GameState): Boolean = winner(state) != null

    fun kindOf(state: GameState, move: Move): MoveKind? {
        val toMove = state.toMove
        val stack = state.board[move.from] ?: return null
        if (stack.owner != toMove || move.count !in 1..stack.size) return null
        // Straight moves of odd length would end on a light square (or leave the board from one).
        if (move.direction.isOrthogonal && move.count % 2 != 0) return null
        val forward = move.direction.isForwardFor(toMove)
        val to = move.to
        if (!to.isOnBoard) return if (forward) MoveKind.BearOff else null
        val target = state.board[to]
        return when {
            target == null -> if (forward) MoveKind.Step else null
            target.owner == toMove -> if (forward) MoveKind.Merge else null
            target.size <= move.count -> MoveKind.Capture(target.size)
            else -> null
        }
    }

    fun isLegal(state: GameState, move: Move): Boolean = kindOf(state, move) != null

    fun legalMoves(state: GameState): List<Move> {
        if (isOver(state)) return emptyList()

        return state.board.filterValues { it.owner == state.toMove }
            .flatMap { (square, stack) -> legalMovesFrom(state, square, stack) }
    }

    fun legalMovesFrom(state: GameState, square: Square): List<Move> {
        val stack = state.board[square] ?: return emptyList()

        if (stack.owner != state.toMove || isOver(state)) return emptyList()

        return legalMovesFrom(state, square, stack)
    }

    private fun legalMovesFrom(state: GameState, square: Square, stack: Stack): List<Move> =
        (1..stack.size).flatMap { count ->
            Direction.entries.map { Move(square, it, count) }.filter { isLegal(state, it) }
        }

    /**
     * Plays [move] and returns the resulting position. If the next player has no legal move they
     * sit out, so the turn passes straight back to the mover.
     */
    fun play(state: GameState, move: Move): GameState {
        val kind = requireNotNull(kindOf(state, move)) { "Illegal move $move" }
        val toMove = state.toMove
        val mover = state.board.getValue(move.from)
        val newBoard = state.board.toMutableMap()
        val remaining = mover.size - move.count
        if (remaining == 0) newBoard.remove(move.from) else newBoard[move.from] = mover.copy(size = remaining)
        when (kind) {
            MoveKind.Step, is MoveKind.Capture -> newBoard[move.to] = Stack(toMove, move.count)
            MoveKind.Merge -> newBoard[move.to] = Stack(toMove, newBoard.getValue(move.to).size + move.count)
            MoveKind.BearOff -> Unit
        }
        val next = GameState(newBoard, toMove.opponent)
        return if (!isOver(next) && legalMoves(next).isEmpty()) next.copy(toMove = toMove) else next
    }
}
