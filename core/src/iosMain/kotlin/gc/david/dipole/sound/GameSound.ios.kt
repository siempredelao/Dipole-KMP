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

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.Foundation.NSData
import platform.Foundation.create

private val players = mutableMapOf<GameSound, AVAudioPlayer>()
private var sessionReady = false

@OptIn(ExperimentalForeignApi::class)
actual fun playSound(sound: GameSound) {
    runCatching {
        if (!sessionReady) {
            // Ambient: mixes with other audio and respects the silent switch.
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
            sessionReady = true
        }
        val player = players.getOrPut(sound) {
            AVAudioPlayer(data = ToneSynth.wavBytes(sound).toNSData(), error = null).apply { prepareToPlay() }
        }
        player.currentTime = 0.0
        player.play()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}
