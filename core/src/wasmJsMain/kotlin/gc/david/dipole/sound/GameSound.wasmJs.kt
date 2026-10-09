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

actual fun playSound(sound: GameSound) {
    playTone(sound.startHz, sound.endHz, sound.seconds, sound.volume)
}

// The browser synthesizes the same sound with the Web Audio API.
private fun playTone(startHz: Double, endHz: Double, seconds: Double, volume: Double): Unit = js(
    """{
    try {
        const AudioContextClass = window.AudioContext || window.webkitAudioContext;
        if (!AudioContextClass) return;
        if (!globalThis.dipoleAudioContext) globalThis.dipoleAudioContext = new AudioContextClass();
        const context = globalThis.dipoleAudioContext;
        const start = context.currentTime;
        const oscillator = context.createOscillator();
        const gain = context.createGain();
        oscillator.frequency.setValueAtTime(startHz, start);
        oscillator.frequency.linearRampToValueAtTime(endHz, start + seconds);
        gain.gain.setValueAtTime(volume, start);
        gain.gain.exponentialRampToValueAtTime(0.001, start + seconds);
        oscillator.connect(gain);
        gain.connect(context.destination);
        oscillator.start(start);
        oscillator.stop(start + seconds);
    } catch (e) {}
    }""",
)
