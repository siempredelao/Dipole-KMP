package gc.david.dipole.celebration

/**
 * Confetti for a win: two bursts from the bottom corners that shoot up, then flutter down and fade.
 *
 * Positions are fractions of the screen (x of its width, y of its height, 0 at the top), so the
 * same confetti fits any screen. Plain data: [ConfettiLauncher] throws it and [ConfettiPhysics]
 * says where each piece is at any moment.
 */
data class Confetti(val pieces: List<ConfettiPiece>)

enum class ConfettiShape { Strip, Dot }

/** One piece of confetti: where and how it is launched, and how it looks. */
data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    /** Screen widths per second. */
    val velocityX: Float,
    /** Screen heights per second; negative is up. */
    val velocityY: Float,
    /** Air drag: how quickly the launch speed is lost. */
    val drag: Float,
    val spinDegreesPerSecond: Float,
    val swayPhase: Float,
    val swaySpeed: Float,
    /** Relative size, around 1. */
    val size: Float,
    /** Index into the theme's confetti colours, below [ConfettiLauncher.COLOR_COUNT]. */
    val colorIndex: Int,
    val shape: ConfettiShape,
)

/** A [piece] at one moment: its position (screen fractions), rotation and opacity. */
data class ConfettiFrame(
    val piece: ConfettiPiece,
    val x: Float,
    val y: Float,
    val rotationDegrees: Float,
    val alpha: Float,
)
