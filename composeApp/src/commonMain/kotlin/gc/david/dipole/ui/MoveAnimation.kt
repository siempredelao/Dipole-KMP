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

package gc.david.dipole.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Stack

/**
 * A move that was just played, animated from the position [before] it: [mover] holds the checkers
 * that travel, and [isCapture] fades out the stack they land on. A class rather than a data class,
 * so playing the same move again (after an undo) still starts a new animation.
 */
class MoveAnimation(val move: Move, val before: GameState, val mover: Stack, val isCapture: Boolean)

/** The animation for [move] played from [before]. */
fun animationOf(move: Move, before: GameState): MoveAnimation = MoveAnimation(
    move = move,
    before = before,
    mover = Stack(before.board.getValue(move.from).owner, move.count),
    isCapture = DipoleRules.kindOf(before, move) is MoveKind.Capture,
)

/** A [MoveAnimation] in flight, [progress] going from 0 (just left) to 1 (landed). */
class Flight(val animation: MoveAnimation, val progress: Float)

/**
 * Animates each move as it is played, by the human or the computer, and calls [onLanded] once the
 * checkers arrive. Undo, loading and new games jump straight to the new position.
 */
@Composable
fun rememberFlight(session: GameSession, onLanded: (MoveAnimation) -> Unit = {}): Flight? {
    val animation = moveAnimation(session)
    val currentOnLanded by rememberUpdatedState(onLanded)
    val progress = remember(animation) { Animatable(if (animation == null) 1f else 0f) }
    LaunchedEffect(animation) {
        if (animation == null) return@LaunchedEffect
        progress.animateTo(1f, tween(MOVE_MILLIS, easing = FastOutSlowInEasing))
        currentOnLanded(animation)
    }

    return animation?.takeIf { progress.value < 1f }?.let { Flight(it, progress.value) }
}

internal const val MOVE_MILLIS = 300

/** Remembers the previous session so a newly played move can be told apart from undo or load. */
private class SessionTracker(var session: GameSession? = null, var animation: MoveAnimation? = null)

@Composable
private fun moveAnimation(session: GameSession): MoveAnimation? {
    val tracker = remember { SessionTracker() }
    if (tracker.session !== session) {
        tracker.animation = tracker.session?.let { before -> newMove(before, session) }
        tracker.session = session
    }

    return tracker.animation
}

/** The move that turned [before] into [after], if exactly one move was added to the same game. */
private fun newMove(before: GameSession, after: GameSession): MoveAnimation? {
    val move = after.lastMove ?: return null
    val isNextMove = after.mode == before.mode &&
        after.moves.size == before.moves.size + 1 &&
        after.moves.subList(0, before.moves.size) == before.moves

    return if (isNextMove) animationOf(move, before.state) else null
}
