package gc.david.dipole.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Stack

/** A move that was just played, animated from the position [before] it. */
class MoveAnimation(val move: Move, val before: GameState) {
    /** The checkers that travel: [Move.count] of them, from the moving stack. */
    val mover: Stack = Stack(before.board.getValue(move.from).owner, move.count)

    val isCapture: Boolean = before.kindOf(move) is MoveKind.Capture
}

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

private const val MOVE_MILLIS = 300

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
    return if (isNextMove) MoveAnimation(move, before.state) else null
}
