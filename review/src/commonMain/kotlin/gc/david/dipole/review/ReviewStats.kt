package gc.david.dipole.review

import kotlin.time.Instant

/** What the app has counted so far, to decide whether to ask for a review. */
data class ReviewStats(
    val finishedGames: Int = 0,
    val launches: Int = 0,
    val firstLaunchAt: Instant? = null,
    /** When the app last asked the store for its review popup; the store may not have shown it. */
    val lastRequestAt: Instant? = null,
)
