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
