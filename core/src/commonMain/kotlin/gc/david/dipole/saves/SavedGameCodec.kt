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

package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGameCodec.encode

/**
 * Turns a [SavedGame] into the text kept in storage and back. The text has one field per line:
 * header, mode, difficulty, side, White's starting square, date, name, moves. Older saves are read too.
 */
object SavedGameCodec {

    private const val HEADER = "dipole-save 4"

    /** Saves made before White's start moved from c1 to e1: no start line. */
    private const val HEADER_V3 = "dipole-save 3"

    /** Saves made before the human could play Black: no side line, played as White. */
    private const val HEADER_V2 = "dipole-save 2"

    /** Saves made before difficulty levels existed: no difficulty line, played at Medium. */
    private const val HEADER_V1 = "dipole-save 1"

    fun encode(game: SavedGame): String = listOf(
        HEADER,
        game.mode.name,
        game.difficulty.name,
        game.humanSide.name,
        "${game.whiteStart.row},${game.whiteStart.col}",
        game.savedAtEpochMillis.toString(),
        game.name,
        game.moves.joinToString(";") { "${it.from.row},${it.from.col},${it.direction.name},${it.count}" },
    ).joinToString("\n")

    /** Parses what [encode] produced, or returns null if [text] isn't a valid save. */
    fun decode(id: String, text: String): SavedGame? {
        val lines = text.split("\n").toMutableList()
        // Bring older formats up to the current one: header, mode, difficulty, side, start, date, name, moves.
        val oldStart = DipoleRules.OLD_WHITE_START.let { "${it.row},${it.col}" }
        when {
            lines.size == 8 && lines[0] == HEADER -> Unit
            lines.size == 7 && lines[0] == HEADER_V3 -> lines.add(4, oldStart)
            lines.size == 6 && lines[0] == HEADER_V2 -> lines.addAll(3, listOf(Player.White.name, oldStart))
            lines.size == 5 && lines[0] == HEADER_V1 ->
                lines.addAll(2, listOf(Difficulty.Medium.name, Player.White.name, oldStart))
            else -> return null
        }

        val mode = GameMode.entries.firstOrNull { it.name == lines[1] } ?: return null
        val difficulty = Difficulty.entries.firstOrNull { it.name == lines[2] } ?: return null
        val humanSide = Player.entries.firstOrNull { it.name == lines[3] } ?: return null
        val whiteStart = decodeSquare(lines[4]) ?: return null
        val savedAt = lines[5].toLongOrNull() ?: return null
        val moves = if (lines[7].isEmpty()) emptyList() else lines[7].split(";").map { decodeMove(it) ?: return null }

        return SavedGame(id, lines[6], savedAt, mode, moves, difficulty, humanSide, whiteStart)
    }

    private fun decodeSquare(text: String): Square? {
        val parts = text.split(",")
        if (parts.size != 2) return null
        val square = Square(parts[0].toIntOrNull() ?: return null, parts[1].toIntOrNull() ?: return null)
        return square.takeIf { it.isOnBoard }
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
