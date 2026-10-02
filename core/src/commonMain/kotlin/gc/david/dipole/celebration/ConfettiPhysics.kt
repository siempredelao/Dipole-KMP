package gc.david.dipole.celebration

import gc.david.dipole.celebration.ConfettiPhysics.frame
import kotlin.math.exp
import kotlin.math.sin

/**
 * Where confetti is over time. Each piece follows a closed-form path, which makes [frame] cheap
 * and lets tests ask for any moment directly.
 */
object ConfettiPhysics {

    const val DURATION_SECONDS = 3.2f

    private const val FADE_SECONDS = 0.8f

    /** Screen heights per second squared. */
    private const val GRAVITY = 0.9f

    /** How far pieces sway from side to side, in screen widths. */
    private const val SWAY = 0.015f

    /** Where every piece of [confetti] is [seconds] after the bursts, or null once it is over. */
    fun frame(confetti: Confetti, seconds: Float): List<ConfettiFrame>? {
        if (seconds >= DURATION_SECONDS) return null
        val alpha = ((DURATION_SECONDS - seconds) / FADE_SECONDS).coerceIn(0f, 1f)
        return confetti.pieces.map { at(it, seconds, alpha) }
    }

    /**
     * [piece] [t] seconds after launch. With gravity and drag the speed tends to a slow fall, which
     * keeps the confetti fluttering instead of dropping like stones.
     */
    private fun at(piece: ConfettiPiece, t: Float, alpha: Float): ConfettiFrame {
        val decay = (1 - exp(-piece.drag * t)) / piece.drag
        val fallSpeed = GRAVITY / piece.drag
        val x = piece.startX + piece.velocityX * decay + SWAY * sin(piece.swaySpeed * t + piece.swayPhase) * minOf(1f, t)
        val y = piece.startY + fallSpeed * t + (piece.velocityY - fallSpeed) * decay
        return ConfettiFrame(piece, x, y, piece.spinDegreesPerSecond * t, alpha)
    }
}
