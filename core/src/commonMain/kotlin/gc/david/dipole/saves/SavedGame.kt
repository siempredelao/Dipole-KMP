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
