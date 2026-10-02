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
