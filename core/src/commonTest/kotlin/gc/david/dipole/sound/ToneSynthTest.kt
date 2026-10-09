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

package gc.david.dipole.sound

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneSynthTest {

    @Test
    fun samplesLastAsLongAsTheSoundAndStayInRange() {
        GameSound.entries.forEach { sound ->
            val samples = ToneSynth.samples(sound)
            assertEquals((sound.seconds * ToneSynth.SAMPLE_RATE).toInt(), samples.size)
            assertTrue(samples.any { it != 0.toShort() })
            assertEquals(0, samples.first().toInt()) // fades in, so no click
        }
    }

    @Test
    fun wavHasAValidHeader() {
        val sound = GameSound.Move
        val wav = ToneSynth.wavBytes(sound)
        assertEquals("RIFF", wav.decodeToString(0, 4))
        assertEquals("WAVE", wav.decodeToString(8, 12))
        assertEquals("data", wav.decodeToString(36, 40))
        assertEquals(44 + ToneSynth.samples(sound).size * 2, wav.size)
    }
}
