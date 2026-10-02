package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player

/**
 * A game saved by the player. Only the moves are stored: loading replays them from the starting
 * position, which also restores the undo history and rejects a corrupted save.
 *
 * Plain data: [SavedGameFactory] creates saves and [SavedGameCodec] turns them into stored text.
 */
data class SavedGame(
    val id: String,
    val name: String,
    val savedAtEpochMillis: Long,
    val mode: GameMode,
    val moves: List<Move>,
    /** The computer's difficulty; only meaningful in [GameMode.VsComputer]. */
    val difficulty: Difficulty = Difficulty.Medium,
    /** The side the human plays; only meaningful in [GameMode.VsComputer]. */
    val humanSide: Player = Player.White,
)
