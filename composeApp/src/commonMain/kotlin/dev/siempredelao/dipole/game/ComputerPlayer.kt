package dev.siempredelao.dipole.game

import kotlin.random.Random

/** A small alpha-beta searcher that plays Dipole by counting checkers left on the board. */
class ComputerPlayer(
    private val depth: Int = 3,
    private val random: Random = Random.Default,
) {
    fun chooseMove(state: GameState): Move? {
        val me = state.toMove
        val moves = ordered(state, state.legalMoves())
        if (moves.isEmpty()) return null
        var bestScore = Int.MIN_VALUE
        val best = mutableListOf<Move>()
        for (move in moves) {
            val score = search(state.play(move), depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE, me)
            if (score > bestScore) {
                bestScore = score
                best.clear()
            }
            if (score == bestScore) best += move
        }
        return best.random(random)
    }

    /** Scores [state] from [me]'s point of view. The side to move can stay the same after a pass. */
    private fun search(state: GameState, depth: Int, alpha: Int, beta: Int, me: Player): Int {
        state.winner?.let { return if (it == me) WIN + depth else -WIN - depth }
        if (depth == 0) return evaluate(state, me)
        val maximizing = state.toMove == me
        var a = alpha
        var b = beta
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        for (move in ordered(state, state.legalMoves())) {
            val score = search(state.play(move), depth - 1, a, b, me)
            if (maximizing) {
                best = maxOf(best, score)
                a = maxOf(a, score)
            } else {
                best = minOf(best, score)
                b = minOf(b, score)
            }
            if (a >= b) break
        }
        return best
    }

    private fun evaluate(state: GameState, me: Player): Int =
        state.checkersOf(me) - state.checkersOf(me.opponent)

    /** Captures first, biggest captures first, so alpha-beta prunes more. */
    private fun ordered(state: GameState, moves: List<Move>): List<Move> =
        moves.sortedByDescending { (state.kindOf(it) as? MoveKind.Capture)?.captured ?: -1 }

    private companion object {
        const val WIN = 1_000
    }
}
