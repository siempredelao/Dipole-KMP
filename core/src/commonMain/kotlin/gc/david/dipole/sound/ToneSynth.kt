package gc.david.dipole.sound

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/** Synthesizes the audio for a [GameSound]: a gliding sine tone with a quick decay. */
object ToneSynth {
    const val SAMPLE_RATE = 44_100

    /** [sound] as 16-bit mono samples at [SAMPLE_RATE]. */
    fun samples(sound: GameSound): ShortArray {
        val count = (sound.seconds * SAMPLE_RATE).roundToInt()
        var phase = 0.0
        return ShortArray(count) { i ->
            val t = i.toDouble() / count
            val hz = sound.startHz + (sound.endHz - sound.startHz) * t
            phase += 2 * PI * hz / SAMPLE_RATE
            // Fast decay, plus a few samples of fade-in so the start doesn't click.
            val envelope = exp(-5.0 * t) * minOf(1.0, i / 40.0)
            (sin(phase) * envelope * sound.volume * Short.MAX_VALUE).roundToInt().toShort()
        }
    }

    /** [sound] as the bytes of a WAV file, for platforms that play audio files. */
    fun wavBytes(sound: GameSound): ByteArray {
        val samples = samples(sound)
        val dataSize = samples.size * 2
        val bytes = ByteArray(44 + dataSize)
        fun putString(offset: Int, text: String) = text.forEachIndexed { i, c -> bytes[offset + i] = c.code.toByte() }
        fun putInt(offset: Int, value: Int, size: Int) {
            for (i in 0 until size) bytes[offset + i] = (value shr (8 * i)).toByte()
        }
        putString(0, "RIFF")
        putInt(4, 36 + dataSize, 4)
        putString(8, "WAVE")
        putString(12, "fmt ")
        putInt(16, 16, 4) // format chunk size
        putInt(20, 1, 2) // PCM
        putInt(22, 1, 2) // mono
        putInt(24, SAMPLE_RATE, 4)
        putInt(28, SAMPLE_RATE * 2, 4) // bytes per second
        putInt(32, 2, 2) // bytes per frame
        putInt(34, 16, 2) // bits per sample
        putString(36, "data")
        putInt(40, dataSize, 4)
        samples.forEachIndexed { i, sample -> putInt(44 + i * 2, sample.toInt(), 2) }
        return bytes
    }
}
