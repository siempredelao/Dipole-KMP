package gc.david.dipole.sound

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameSoundTest {

    @Test
    fun samplesLastAsLongAsTheSoundAndStayInRange() {
        GameSound.entries.forEach { sound ->
            val samples = sound.samples()
            assertEquals((sound.seconds * SAMPLE_RATE).toInt(), samples.size)
            assertTrue(samples.any { it != 0.toShort() })
            assertEquals(0, samples.first().toInt()) // fades in, so no click
        }
    }

    @Test
    fun wavHasAValidHeader() {
        val sound = GameSound.Move
        val wav = sound.wavBytes()
        assertEquals("RIFF", wav.decodeToString(0, 4))
        assertEquals("WAVE", wav.decodeToString(8, 12))
        assertEquals("data", wav.decodeToString(36, 40))
        assertEquals(44 + sound.samples().size * 2, wav.size)
    }
}
