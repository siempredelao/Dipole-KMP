package gc.david.dipole.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import gc.david.dipole.celebration.ConfettiLauncher
import gc.david.dipole.celebration.ConfettiPhysics
import gc.david.dipole.celebration.ConfettiFrame
import gc.david.dipole.celebration.ConfettiShape
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Throws confetti over the screen for a win. Each new [celebration] throws it once, as soon as the
 * winning move has landed, if [celebrating] says it hasn't been thrown yet; [onStart] then reports
 * it thrown (and plays the chime). It only draws, so taps go through to the game underneath.
 *
 * [launcher] picks how the pieces fly; [previewSeconds] freezes the confetti at that moment, for
 * previews.
 */
@Composable
fun ConfettiOverlay(
    celebration: Int,
    celebrating: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    launcher: ConfettiLauncher = remember { ConfettiLauncher(Random.Default) },
    previewSeconds: Float? = null,
) {
    val confetti = remember(celebration) { launcher.burst() }
    var seconds by remember(celebration) { mutableFloatStateOf(previewSeconds ?: NOT_THROWN) }
    val currentOnStart by rememberUpdatedState(onStart)
    LaunchedEffect(celebration) {
        if (!celebrating || previewSeconds != null) return@LaunchedEffect
        delay(MOVE_MILLIS.toLong())
        currentOnStart()
        val start = withFrameNanos { it }
        while (seconds < ConfettiPhysics.DURATION_SECONDS) {
            seconds = withFrameNanos { (it - start) / 1_000_000_000f }
        }
    }
    if (seconds == NOT_THROWN) return
    val frame = ConfettiPhysics.frame(confetti, seconds) ?: return
    val colors = LocalAppColors.current.confetti
    Canvas(modifier.fillMaxSize()) {
        frame.forEach { drawPiece(it, colors[it.piece.colorIndex]) }
    }
}

private const val NOT_THROWN = -1f

private fun DrawScope.drawPiece(frame: ConfettiFrame, color: Color) {
    val center = Offset(frame.x * size.width, frame.y * size.height)
    val unit = PIECE_SIZE.toPx() * frame.piece.size
    when (frame.piece.shape) {
        ConfettiShape.Dot -> drawCircle(color, radius = unit / 2.5f, center = center, alpha = frame.alpha)
        ConfettiShape.Strip -> rotate(frame.rotationDegrees, pivot = center) {
            drawRect(
                color = color,
                topLeft = Offset(center.x - unit / 2, center.y - unit / 4),
                size = Size(unit, unit / 2),
                alpha = frame.alpha,
            )
        }
    }
}

private val PIECE_SIZE = 10.dp
