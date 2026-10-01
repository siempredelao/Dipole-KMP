package dev.siempredelao.dipole.saves

import dev.siempredelao.dipole.game.Difficulty
import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameSession
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.game.Square
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
) {
    /** Rebuilds the game, or returns null if the stored moves aren't a legal game. */
    fun toSession(): GameSession? = GameSession.replay(mode, moves, difficulty)

    fun encode(): String = listOf(
        HEADER,
        mode.name,
        difficulty.name,
        savedAtEpochMillis.toString(),
        name,
        moves.joinToString(";") { "${it.from.row},${it.from.col},${it.direction.name},${it.count}" },
    ).joinToString("\n")

    companion object {
        private const val HEADER = "dipole-save 2"

        /** Saves made before difficulty levels existed: no difficulty line, played at Medium. */
        private const val HEADER_V1 = "dipole-save 1"

        fun of(session: GameSession, name: String, savedAt: Instant): SavedGame = SavedGame(
            id = "${savedAt.toEpochMilliseconds()}-${Random.nextInt(1_000_000)}",
            name = name.lines().joinToString(" ").trim().ifEmpty { defaultName(savedAt) },
            savedAtEpochMillis = savedAt.toEpochMilliseconds(),
            mode = session.mode,
            moves = session.moves,
            difficulty = session.difficulty,
        )

        /** Parses what [encode] produced, or returns null if [text] isn't a valid save. */
        fun decode(id: String, text: String): SavedGame? {
            val lines = text.split("\n").toMutableList()
            when {
                lines.size == 6 && lines[0] == HEADER -> Unit
                lines.size == 5 && lines[0] == HEADER_V1 -> lines.add(2, Difficulty.Medium.name)
                else -> return null
            }
            val mode = GameMode.entries.firstOrNull { it.name == lines[1] } ?: return null
            val difficulty = Difficulty.entries.firstOrNull { it.name == lines[2] } ?: return null
            val savedAt = lines[3].toLongOrNull() ?: return null
            val moves = if (lines[5].isEmpty()) emptyList() else lines[5].split(";").map { decodeMove(it) ?: return null }
            return SavedGame(id, lines[4], savedAt, mode, moves, difficulty)
        }

        private fun decodeMove(text: String): Move? {
            val parts = text.split(",")
            if (parts.size != 4) return null
            val row = parts[0].toIntOrNull() ?: return null
            val col = parts[1].toIntOrNull() ?: return null
            val direction = Direction.entries.firstOrNull { it.name == parts[2] } ?: return null
            val count = parts[3].toIntOrNull() ?: return null
            return Move(Square(row, col), direction, count)
        }

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
