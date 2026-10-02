package gc.david.dipole.saves

import gc.david.dipole.game.GameSession
import kotlin.random.Random
import kotlin.time.Clock

/** Creates a [SavedGame] from the game being played, with a new id and the time it was saved. */
class SavedGameFactory(
    private val random: Random,
    private val clock: Clock,
) {
    /** Saves [session] as [name], which is kept on one line and must not be blank. */
    fun create(session: GameSession, name: String): SavedGame {
        val oneLineName = name.lines().joinToString(" ").trim()
        require(oneLineName.isNotEmpty()) { "A saved game needs a name" }
        val savedAt = clock.now().toEpochMilliseconds()
        return SavedGame(
            id = "$savedAt-${random.nextInt(1_000_000)}",
            name = oneLineName,
            savedAtEpochMillis = savedAt,
            mode = session.mode,
            moves = session.moves,
            difficulty = session.difficulty,
            humanSide = session.humanSide,
        )
    }
}
