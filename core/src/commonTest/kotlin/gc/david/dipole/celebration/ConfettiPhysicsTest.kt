package gc.david.dipole.celebration

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfettiPhysicsTest {

    private val confetti = ConfettiLauncher(Random(42)).burst()

    @Test
    fun twoBurstsFromTheBottomCorners() {
        assertEquals(150, confetti.pieces.size)
        assertEquals(75, confetti.pieces.count { it.startX == 0f && it.velocityX > 0 })
        assertEquals(75, confetti.pieces.count { it.startX == 1f && it.velocityX < 0 })
        assertTrue(confetti.pieces.all { it.startY == 1f && it.velocityY < 0 })
    }

    @Test
    fun piecesShootUpThenFallBackDown() {
        val start = ConfettiPhysics.frame(confetti, 0f)!!
        val peak = ConfettiPhysics.frame(confetti, 0.8f)!!
        val later = ConfettiPhysics.frame(confetti, 2.5f)!!
        // Most of the confetti climbs well up the screen...
        assertTrue(peak.count { it.y < 0.6f } > 100)
        // ...and every piece then comes down again.
        peak.indices.forEach { i -> assertTrue(later[i].y > peak[i].y, "piece $i") }
        assertTrue(start.all { it.y == 1f })
    }

    @Test
    fun confettiStaysMostlyOnScreen() {
        val peak = ConfettiPhysics.frame(confetti, 1f)!!
        assertTrue(peak.all { it.x in -0.05f..1.05f })
        assertTrue(peak.all { it.y > -0.1f })
    }

    @Test
    fun fadesOutAndEnds() {
        assertTrue(ConfettiPhysics.frame(confetti, 1f)!!.all { it.alpha == 1f })
        val fading = ConfettiPhysics.frame(confetti, ConfettiPhysics.DURATION_SECONDS - 0.4f)!!
        assertTrue(fading.all { it.alpha in 0.4f..0.6f })
        assertNull(ConfettiPhysics.frame(confetti, ConfettiPhysics.DURATION_SECONDS))
    }

    @Test
    fun theSameSeedGivesTheSameConfetti() {
        assertEquals(confetti, ConfettiLauncher(Random(42)).burst())
    }

    @Test
    fun colorsAreWithinThePalette() {
        assertTrue(confetti.pieces.all { it.colorIndex in 0 until ConfettiLauncher.COLOR_COUNT })
    }
}
