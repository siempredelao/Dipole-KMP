package gc.david.dipole.review

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * When a happy moment may turn into a review request. The stores add their own limits on top
 * (Google about once a month, Apple 3 times a year), so a request doesn't always show the popup.
 */
object ReviewPolicy {

    const val MIN_FINISHED_GAMES = 5
    const val MIN_DAYS_SINCE_FIRST_LAUNCH = 3
    const val MIN_LAUNCHES = 2
    const val MIN_DAYS_BETWEEN_REQUESTS = 30

    fun shouldAsk(stats: ReviewStats, now: Instant): Boolean {
        val firstLaunchAt = stats.firstLaunchAt ?: return false
        val lastRequestAt = stats.lastRequestAt
        return stats.finishedGames >= MIN_FINISHED_GAMES &&
            stats.launches >= MIN_LAUNCHES &&
            now - firstLaunchAt >= MIN_DAYS_SINCE_FIRST_LAUNCH.days &&
            (lastRequestAt == null || now - lastRequestAt >= MIN_DAYS_BETWEEN_REQUESTS.days)
    }
}
