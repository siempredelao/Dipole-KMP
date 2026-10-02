package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGameCodec.encode

/**
 * Turns a [SavedGame] into the text kept in storage and back. The text has one field per line:
 * header, mode, difficulty, side, date, name, moves. Older saves are read too.
 */
object SavedGameCodec {

    private const val HEADER = "dipole-save 3"

    /** Saves made before the human could play Black: no side line, played as White. */
    private const val HEADER_V2 = "dipole-save 2"

    /** Saves made before difficulty levels existed: no difficulty line, played at Medium. */
    private const val HEADER_V1 = "dipole-save 1"

    fun encode(game: SavedGame): String = listOf(
        HEADER,
        game.mode.name,
        game.difficulty.name,
        game.humanSide.name,
        game.savedAtEpochMillis.toString(),
        game.name,
        game.moves.joinToString(";") { "${it.from.row},${it.from.col},${it.direction.name},${it.count}" },
    ).joinToString("\n")

    /** Parses what [encode] produced, or returns null if [text] isn't a valid save. */
    fun decode(id: String, text: String): SavedGame? {
        val lines = text.split("\n").toMutableList()
        // Bring older formats up to the current one: header, mode, difficulty, side, date, name, moves.
        when {
            lines.size == 7 && lines[0] == HEADER -> Unit
            lines.size == 6 && lines[0] == HEADER_V2 -> lines.add(3, Player.White.name)
            lines.size == 5 && lines[0] == HEADER_V1 -> lines.addAll(2, listOf(Difficulty.Medium.name, Player.White.name))
            else -> return null
        }

        val mode = GameMode.entries.firstOrNull { it.name == lines[1] } ?: return null
        val difficulty = Difficulty.entries.firstOrNull { it.name == lines[2] } ?: return null
        val humanSide = Player.entries.firstOrNull { it.name == lines[3] } ?: return null
        val savedAt = lines[4].toLongOrNull() ?: return null
        val moves = if (lines[6].isEmpty()) emptyList() else lines[6].split(";").map { decodeMove(it) ?: return null }

        return SavedGame(id, lines[5], savedAt, mode, moves, difficulty, humanSide)
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
}
