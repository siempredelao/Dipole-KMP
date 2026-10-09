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

/**
 * The game's sound effects. They are synthesized rather than recorded, so there are no audio files
 * to ship: [startHz] glides to [endHz] while the volume decays over [seconds]. [ToneSynth] turns
 * these settings into audio.
 */
enum class GameSound(val startHz: Double, val endHz: Double, val seconds: Double, val volume: Double) {

    /** A short wooden tick when checkers land. */
    Move(startHz = 900.0, endHz = 600.0, seconds = 0.06, volume = 0.5),

    /** A deeper, longer knock when a stack is captured. */
    Capture(startHz = 320.0, endHz = 110.0, seconds = 0.16, volume = 0.8),

    /** A bright rising chime with the confetti when a human wins. */
    Win(startHz = 520.0, endHz = 1_040.0, seconds = 0.45, volume = 0.55),
}

/** Plays [sound] without waiting for it to finish. Never throws: a device without audio stays silent. */
expect fun playSound(sound: GameSound)
