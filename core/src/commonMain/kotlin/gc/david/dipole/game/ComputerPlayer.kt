package gc.david.dipole.game

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

enum class Difficulty { Easy, Medium, Hard }

/** A recommended [move] from one stack, and whether another stack has a clearly better move. */
data class Hint(val move: Move, val betterMoveElsewhere: Boolean)

/**
 * An alpha-beta searcher that plays Dipole.
 *
 * It searches [depth] plies, then keeps searching one ply deeper at a time up to [maxDepth] while
 * [timeBudget] allows, playing the best move of the deepest search it finished. With
 * [randomMoveChance] it sometimes plays a random legal move instead.
 */
class ComputerPlayer(
    private val depth: Int = 3,
    private val maxDepth: Int = depth,
    private val timeBudget: Duration = Duration.INFINITE,
    private val randomMoveChance: Double = 0.0,
    private val countThreats: Boolean = false,
    private val random: Random = Random.Default,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {

    fun chooseMove(state: GameState): Move? {
        val moves = ordered(state, DipoleRules.legalMoves(state))
        if (moves.isEmpty()) return null
        if (randomMoveChance > 0 && random.nextDouble() < randomMoveChance) return moves.random(random)

        val scores = scoreMoves(state, moves)
        val bestScore = scores.values.max()
        return moves.filter { scores.getValue(it) == bestScore }.random(random)
    }

    /**
     * Recommends the best move for the stack on [from], or returns null if it can't move. Never
     * plays a random move, whatever [randomMoveChance] is.
     */
    fun hint(state: GameState, from: Square): Hint? {
        val moves = ordered(state, DipoleRules.legalMoves(state))
        if (moves.none { it.from == from }) return null
        val scores = scoreMoves(state, moves)
        val best = moves.filter { it.from == from }.maxBy { scores.getValue(it) }
        val margin = scores.values.max() - scores.getValue(best)
        return Hint(best, betterMoveElsewhere = margin >= MATERIAL_WEIGHT)
    }

    /**
     * Scores every move in [moves], searching [depth] plies and then deeper while the time budget
     * lasts. Returns the scores of the deepest search that finished.
     */
    private fun scoreMoves(state: GameState, moves: List<Move>): Map<Move, Int> {
        val deadline = timeSource.markNow() + timeBudget
        var scores = scoreMoves(state, moves, depth, deadline = null)
        for (d in depth + 1..maxDepth) {
            if (deadline.hasPassedNow()) break
            scores = try {
                scoreMoves(state, moves, d, deadline)
            } catch (_: OutOfTime) {
                break
            }
        }
        return scores
    }

    private fun scoreMoves(state: GameState, moves: List<Move>, depth: Int, deadline: TimeMark?): Map<Move, Int> {
        val me = state.toMove
        return moves.associateWith { search(DipoleRules.play(state, it), depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE, me, deadline) }
    }

    /** Scores [state] from [me]'s point of view. The side to move can stay the same after a pass. */
    private fun search(
        state: GameState,
        depth: Int,
        alpha: Int,
        beta: Int,
        me: Player,
        deadline: TimeMark?,
    ): Int {
        DipoleRules.winner(state)?.let { return if (it == me) WIN + depth else -WIN - depth }
        if (depth == 0) return evaluate(state, me)
        if (deadline != null && deadline.hasPassedNow()) throw OutOfTime()
        val maximizing = state.toMove == me
        var a = alpha
        var b = beta
        var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE
        for (move in ordered(state, DipoleRules.legalMoves(state))) {
            val score = search(DipoleRules.play(state, move), depth - 1, a, b, me, deadline)
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

    private fun evaluate(state: GameState, me: Player): Int {
        val material = (DipoleRules.checkersOf(state, me) - DipoleRules.checkersOf(state, me.opponent)) * MATERIAL_WEIGHT
        if (!countThreats) return material
        // The side to move can probably take the biggest stack it threatens, so count that.
        val threat = biggestThreatenedStack(state, attacker = state.toMove) * THREAT_WEIGHT
        return if (state.toMove == me) material + threat else material - threat
    }

    /** Size of the biggest enemy stack [attacker] could capture this turn, or 0. */
    private fun biggestThreatenedStack(state: GameState, attacker: Player): Int {
        var biggest = 0
        for ((from, stack) in state.board) {
            if (stack.owner != attacker) continue
            for (direction in Direction.entries) {
                for (count in 1..stack.size) {
                    if (direction.isOrthogonal && count % 2 != 0) continue
                    val target = state.board[Move(from, direction, count).to] ?: continue
                    if (target.owner != attacker && target.size <= count) biggest = maxOf(biggest, target.size)
                }
            }
        }
        return biggest
    }

    /** Captures first, biggest captures first, so alpha-beta prunes more. */
    private fun ordered(state: GameState, moves: List<Move>): List<Move> =
        moves.sortedByDescending { (DipoleRules.kindOf(state, it) as? MoveKind.Capture)?.captured ?: -1 }

    private class OutOfTime : RuntimeException()

    companion object {

        private const val WIN = 100_000
        private const val MATERIAL_WEIGHT = 10
        private const val THREAT_WEIGHT = 8

        fun forDifficulty(difficulty: Difficulty, random: Random = Random.Default): ComputerPlayer = when (difficulty) {
            Difficulty.Easy -> ComputerPlayer(depth = 1, randomMoveChance = 0.35, random = random)
            Difficulty.Medium -> ComputerPlayer(depth = 3, random = random)
            Difficulty.Hard -> ComputerPlayer(
                depth = 3,
                maxDepth = 6,
                timeBudget = 1_500.milliseconds,
                countThreats = true,
                random = random,
            )
        }
    }
}
