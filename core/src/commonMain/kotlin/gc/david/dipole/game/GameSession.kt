package gc.david.dipole.game

enum class GameMode { VsComputer, TwoPlayers }

/**
 * A game being played: its mode, the computer's [difficulty] and the [humanSide] (both only used in
 * [GameMode.VsComputer]), the moves made so far and every position they led to.
 *
 * Immutable: every action returns a new session. Keeping the moves lets a game be saved and
 * replayed, and keeping the positions makes undo cheap.
 */
class GameSession private constructor(
    val mode: GameMode,
    val difficulty: Difficulty,
    val humanSide: Player,
    val moves: List<Move>,
    private val states: List<GameState>,
) {
    val state: GameState get() = states.last()

    val lastMove: Move? get() = moves.lastOrNull()

    val isComputerTurn: Boolean
        get() = mode == GameMode.VsComputer && state.toMove != humanSide && !DipoleRules.isOver(state)

    /** True when the player who didn't just move had no legal move, so the mover goes again. */
    val opponentSatOut: Boolean
        get() = states.size > 1 && states[states.size - 2].toMove == state.toMove

    val canUndo: Boolean get() = !isComputerTurn && undo() !== this

    fun play(move: Move): GameSession =
        GameSession(mode, difficulty, humanSide, moves + move, states + DipoleRules.play(state, move))

    /**
     * Takes back the last move. Against the computer it goes back to the last position where the
     * human was to move, so the computer's reply is undone too. Returns this session unchanged when
     * there is nothing to undo, for example when only the computer's opening move has been played.
     */
    fun undo(): GameSession {
        if (moves.isEmpty()) return this
        var count = moves.size - 1
        if (mode == GameMode.VsComputer) {
            while (count > 0 && states[count].toMove != humanSide) count--
            if (states[count].toMove != humanSide) return this
        }
        return GameSession(mode, difficulty, humanSide, moves.take(count), states.take(count + 1))
    }

    companion object {
        fun new(
            mode: GameMode,
            difficulty: Difficulty = Difficulty.Medium,
            initial: GameState = DipoleRules.initial(),
            humanSide: Player = Player.White,
        ): GameSession = GameSession(mode, difficulty, humanSide, emptyList(), listOf(initial))

        /** Replays [moves] from the starting position, or returns null if any of them is illegal. */
        fun replay(
            mode: GameMode,
            moves: List<Move>,
            difficulty: Difficulty = Difficulty.Medium,
            humanSide: Player = Player.White,
        ): GameSession? =
            moves.fold(new(mode, difficulty, humanSide = humanSide)) { session, move ->
                if (!DipoleRules.isLegal(session.state, move)) return null
                session.play(move)
            }
    }
}
