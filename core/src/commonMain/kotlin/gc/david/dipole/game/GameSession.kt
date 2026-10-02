package gc.david.dipole.game

enum class GameMode { VsComputer, TwoPlayers }

/**
 * A game being played: its mode, the computer's [difficulty] and the [humanSide] (both only used in
 * [GameMode.VsComputer]), the moves made so far and every position they led to, starting with the
 * position before the first move.
 *
 * Plain data. [GameSessions] starts, plays, undoes and replays sessions; the constructor is internal
 * so [positions] always match [moves]. Keeping the moves lets a game be saved and replayed, and
 * keeping the positions makes undo cheap.
 */
@ConsistentCopyVisibility
data class GameSession internal constructor(
    val mode: GameMode,
    val difficulty: Difficulty,
    val humanSide: Player,
    val moves: List<Move>,
    val positions: List<GameState>,
) {

    /** The current position. */
    val state: GameState get() = positions.last()

    val lastMove: Move? get() = moves.lastOrNull()
}
