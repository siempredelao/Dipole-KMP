package dev.siempredelao.dipole.game

enum class GameMode { VsComputer, TwoPlayers }

/**
 * A game being played: its mode, the moves made so far and every position they led to.
 *
 * Immutable: every action returns a new session. Keeping the moves lets a game be saved and
 * replayed, and keeping the positions makes undo cheap.
 */
class GameSession private constructor(
    val mode: GameMode,
    val moves: List<Move>,
    private val states: List<GameState>,
) {
    val state: GameState get() = states.last()

    val lastMove: Move? get() = moves.lastOrNull()

    val isComputerTurn: Boolean
        get() = mode == GameMode.VsComputer && state.toMove != HUMAN_SIDE && !state.isOver

    /** True when the player who didn't just move had no legal move, so the mover goes again. */
    val opponentSatOut: Boolean
        get() = states.size > 1 && states[states.size - 2].toMove == state.toMove

    val canUndo: Boolean get() = moves.isNotEmpty() && !isComputerTurn

    fun play(move: Move): GameSession = GameSession(mode, moves + move, states + state.play(move))

    /**
     * Takes back the last move. Against the computer it goes back to the last position where the
     * human was to move, so the computer's reply is undone too.
     */
    fun undo(): GameSession {
        if (moves.isEmpty()) return this
        var count = moves.size - 1
        if (mode == GameMode.VsComputer) {
            while (count > 0 && states[count].toMove != HUMAN_SIDE) count--
        }
        return GameSession(mode, moves.take(count), states.take(count + 1))
    }

    companion object {
        /** The side the human plays against the computer. */
        val HUMAN_SIDE = Player.White

        fun new(mode: GameMode, initial: GameState = GameState.initial()): GameSession =
            GameSession(mode, emptyList(), listOf(initial))

        /** Replays [moves] from the starting position, or returns null if any of them is illegal. */
        fun replay(mode: GameMode, moves: List<Move>): GameSession? =
            moves.fold(new(mode)) { session, move ->
                if (!session.state.isLegal(move)) return null
                session.play(move)
            }
    }
}
