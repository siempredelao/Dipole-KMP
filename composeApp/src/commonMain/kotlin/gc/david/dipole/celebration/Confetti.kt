package gc.david.dipole.celebration

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti for a win: two bursts from the bottom corners that shoot up, then flutter down and fade.
 *
 * Positions are fractions of the screen (x of its width, y of its height, 0 at the top), so the
 * same confetti fits any screen. Each piece follows a closed-form path, which makes [frame] cheap
 * and lets tests ask for any moment directly.
 */
class Confetti(val pieces: List<ConfettiPiece>) {

    /** Where every piece is [seconds] after the bursts, or null once the confetti is over. */
    fun frame(seconds: Float): List<ConfettiFrame>? {
        if (seconds >= DURATION_SECONDS) return null
        val alpha = ((DURATION_SECONDS - seconds) / FADE_SECONDS).coerceIn(0f, 1f)
        return pieces.map { it.at(seconds, alpha) }
    }

    companion object {
        const val DURATION_SECONDS = 3.2f
        private const val FADE_SECONDS = 0.8f

        /** Two bursts of [perBurst] pieces each, randomised by [random]. */
        fun burst(random: Random = Random.Default, perBurst: Int = 75): Confetti {
            val fromLeft = List(perBurst) { piece(random, startX = 0f, towardsRight = true) }
            val fromRight = List(perBurst) { piece(random, startX = 1f, towardsRight = false) }
            return Confetti(fromLeft + fromRight)
        }

        private fun piece(random: Random, startX: Float, towardsRight: Boolean): ConfettiPiece {
            // Aim up and inwards, between 55 and 80 degrees above the horizontal.
            val angle = (55 + random.nextFloat() * 25) * PI.toFloat() / 180
            val speed = 1.6f + random.nextFloat() * 0.8f
            val inwards = if (towardsRight) 1f else -1f
            return ConfettiPiece(
                startX = startX,
                startY = 1f,
                velocityX = inwards * cos(angle) * speed * 0.6f,
                velocityY = -sin(angle) * speed,
                drag = 1.6f + random.nextFloat() * 0.8f,
                spinDegreesPerSecond = (random.nextFloat() - 0.5f) * 720,
                swayPhase = random.nextFloat() * 2 * PI.toFloat(),
                swaySpeed = 4f + random.nextFloat() * 4f,
                size = 0.7f + random.nextFloat() * 0.6f,
                colorIndex = random.nextInt(COLOR_COUNT),
                shape = if (random.nextFloat() < 0.7f) ConfettiShape.Strip else ConfettiShape.Dot,
            )
        }

        /** How many confetti colours the theme provides; [ConfettiPiece.colorIndex] is below this. */
        const val COLOR_COUNT = 6
    }
}

enum class ConfettiShape { Strip, Dot }

/** One piece of confetti: where and how it is launched, and how it looks. */
data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    /** Screen widths per second. */
    val velocityX: Float,
    /** Screen heights per second; negative is up. */
    val velocityY: Float,
    /** Air drag: how quickly the launch speed is lost. */
    val drag: Float,
    val spinDegreesPerSecond: Float,
    val swayPhase: Float,
    val swaySpeed: Float,
    /** Relative size, around 1. */
    val size: Float,
    val colorIndex: Int,
    val shape: ConfettiShape,
) {
    /**
     * The piece [t] seconds after launch. With gravity and drag the speed tends to a slow fall,
     * which keeps the confetti fluttering instead of dropping like stones.
     */
    fun at(t: Float, alpha: Float): ConfettiFrame {
        val decay = (1 - exp(-drag * t)) / drag
        val fallSpeed = GRAVITY / drag
        val x = startX + velocityX * decay + SWAY * sin(swaySpeed * t + swayPhase) * minOf(1f, t)
        val y = startY + fallSpeed * t + (velocityY - fallSpeed) * decay
        return ConfettiFrame(this, x, y, spinDegreesPerSecond * t, alpha)
    }

    private companion object {
        /** Screen heights per second squared. */
        const val GRAVITY = 0.9f

        /** How far pieces sway from side to side, in screen widths. */
        const val SWAY = 0.015f
    }
}

/** A [piece] at one moment: its position (screen fractions), rotation and opacity. */
data class ConfettiFrame(
    val piece: ConfettiPiece,
    val x: Float,
    val y: Float,
    val rotationDegrees: Float,
    val alpha: Float,
)
