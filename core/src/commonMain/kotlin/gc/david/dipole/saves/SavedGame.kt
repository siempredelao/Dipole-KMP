package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import kotlin.random.Random
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A game saved by the player. Only the moves are stored: loading replays them from the starting
 * position, which also restores the undo history and rejects a corrupted save.
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
) {
    /** Rebuilds the game, or returns null if the stored moves aren't a legal game. */
    fun toSession(): GameSession? = GameSessions.replay(mode, moves, difficulty, humanSide)

    companion object {
        fun of(session: GameSession, name: String, savedAt: Instant): SavedGame = SavedGame(
            id = "${savedAt.toEpochMilliseconds()}-${Random.nextInt(1_000_000)}",
            name = name.lines().joinToString(" ").trim().ifEmpty { defaultName(savedAt) },
            savedAtEpochMillis = savedAt.toEpochMilliseconds(),
            mode = session.mode,
            moves = session.moves,
            difficulty = session.difficulty,
            humanSide = session.humanSide,
        )

        /** Default name for a save, like "30 Sep 2026 21:42", in the device's time zone. */
        fun defaultName(savedAt: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
            val t = savedAt.toLocalDateTime(timeZone)
            val month = MONTHS[t.month.ordinal]
            return "${t.day} $month ${t.year} ${t.hour.twoDigits()}:${t.minute.twoDigits()}"
        }

        private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        private fun Int.twoDigits() = toString().padStart(2, '0')
    }
}
