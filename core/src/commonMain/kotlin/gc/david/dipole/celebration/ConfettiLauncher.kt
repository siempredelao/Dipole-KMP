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
