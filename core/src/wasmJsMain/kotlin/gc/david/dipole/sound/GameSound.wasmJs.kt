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
