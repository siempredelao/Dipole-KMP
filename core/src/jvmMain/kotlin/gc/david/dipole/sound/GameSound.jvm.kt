package gc.david.dipole.sound

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

private val clips = mutableMapOf<GameSound, Clip>()

actual fun playSound(sound: GameSound) {
    runCatching {
        val clip = clips.getOrPut(sound) {
            AudioSystem.getClip().apply {
                open(AudioSystem.getAudioInputStream(ByteArrayInputStream(sound.wavBytes())))
            }
        }
        clip.stop()
        clip.framePosition = 0
        clip.start()
    }
}
