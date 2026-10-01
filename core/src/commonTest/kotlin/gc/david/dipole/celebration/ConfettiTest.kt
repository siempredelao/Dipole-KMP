package gc.david.dipole.celebration

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfettiTest {

    private val confetti = Confetti.burst(Random(42))

    @Test
    fun twoBurstsFromTheBottomCorners() {
        assertEquals(150, confetti.pieces.size)
        assertEquals(75, confetti.pieces.count { it.startX == 0f && it.velocityX > 0 })
        assertEquals(75, confetti.pieces.count { it.startX == 1f && it.velocityX < 0 })
        assertTrue(confetti.pieces.all { it.startY == 1f && it.velocityY < 0 })
    }

    @Test
    fun piecesShootUpThenFallBackDown() {
        val start = confetti.frame(0f)!!
        val peak = confetti.frame(0.8f)!!
        val later = confetti.frame(2.5f)!!
        // Most of the confetti climbs well up the screen...
        assertTrue(peak.count { it.y < 0.6f } > 100)
        // ...and every piece then comes down again.
        peak.indices.forEach { i -> assertTrue(later[i].y > peak[i].y, "piece $i") }
        assertTrue(start.all { it.y == 1f })
    }

    @Test
    fun confettiStaysMostlyOnScreen() {
        val peak = confetti.frame(1f)!!
        assertTrue(peak.all { it.x in -0.05f..1.05f })
        assertTrue(peak.all { it.y > -0.1f })
    }

    @Test
    fun fadesOutAndEnds() {
        assertTrue(confetti.frame(1f)!!.all { it.alpha == 1f })
        val fading = confetti.frame(Confetti.DURATION_SECONDS - 0.4f)!!
        assertTrue(fading.all { it.alpha in 0.4f..0.6f })
        assertNull(confetti.frame(Confetti.DURATION_SECONDS))
    }

    @Test
    fun theSameSeedGivesTheSameConfetti() {
        assertEquals(confetti.pieces, Confetti.burst(Random(42)).pieces)
    }

    @Test
    fun colorsAreWithinThePalette() {
        assertTrue(confetti.pieces.all { it.colorIndex in 0 until Confetti.COLOR_COUNT })
    }
}
