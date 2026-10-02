package gc.david.dipole.celebration

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Throws [Confetti]: two bursts from the bottom corners, each piece randomised by [random]. */
class ConfettiLauncher(private val random: Random) {

    /** Two bursts of [perBurst] pieces each. */
    fun burst(perBurst: Int = 75): Confetti {
        val fromLeft = List(perBurst) { piece(startX = 0f, towardsRight = true) }
        val fromRight = List(perBurst) { piece(startX = 1f, towardsRight = false) }
        return Confetti(fromLeft + fromRight)
    }

    private fun piece(startX: Float, towardsRight: Boolean): ConfettiPiece {
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

    companion object {
        /** How many confetti colours the theme provides; [ConfettiPiece.colorIndex] is below this. */
        const val COLOR_COUNT = 6
    }
}
