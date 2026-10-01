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
            AVAudioPlayer(data = sound.wavBytes().toNSData(), error = null).apply { prepareToPlay() }
        }
        player.currentTime = 0.0
        player.play()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}
