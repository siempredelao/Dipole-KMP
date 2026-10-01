package gc.david.dipole.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

private val tracks = mutableMapOf<GameSound, AudioTrack>()

actual fun playSound(sound: GameSound) {
    runCatching {
        val track = tracks.getOrPut(sound) { createTrack(sound) }
        track.stop()
        track.reloadStaticData()
        track.play()
    }
}

private fun createTrack(sound: GameSound): AudioTrack {
    val samples = sound.samples()
    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(samples.size * 2)
        .build()
    track.write(samples, 0, samples.size)
    return track
}
