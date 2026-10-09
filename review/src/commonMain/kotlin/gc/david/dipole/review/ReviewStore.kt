package gc.david.dipole.review

import com.russhwolf.settings.Settings
import kotlin.time.Instant

/** Keeps the [ReviewStats] between launches. */
interface ReviewStore {

    val stats: ReviewStats

    fun update(change: (ReviewStats) -> ReviewStats)
}

/** Stores the stats in [settings] under keys starting with `review_`, so it can share the app's store. */
class SettingsReviewStore(
    private val settings: Settings,
) : ReviewStore {

    override val stats: ReviewStats
        get() = ReviewStats(
            finishedGames = settings.getInt(FINISHED_GAMES_KEY, 0),
            launches = settings.getInt(LAUNCHES_KEY, 0),
            firstLaunchAt = settings.getLongOrNull(FIRST_LAUNCH_AT_KEY)?.let(Instant::fromEpochMilliseconds),
            lastRequestAt = settings.getLongOrNull(LAST_REQUEST_AT_KEY)?.let(Instant::fromEpochMilliseconds),
        )

    override fun update(change: (ReviewStats) -> ReviewStats) {
        val new = change(stats)
        settings.putInt(FINISHED_GAMES_KEY, new.finishedGames)
        settings.putInt(LAUNCHES_KEY, new.launches)
        putInstant(FIRST_LAUNCH_AT_KEY, new.firstLaunchAt)
        putInstant(LAST_REQUEST_AT_KEY, new.lastRequestAt)
    }

    private fun putInstant(key: String, value: Instant?) {
        if (value == null) settings.remove(key) else settings.putLong(key, value.toEpochMilliseconds())
    }

    private companion object {

        const val FINISHED_GAMES_KEY = "review_finished_games"
        const val LAUNCHES_KEY = "review_launches"
        const val FIRST_LAUNCH_AT_KEY = "review_first_launch_at"
        const val LAST_REQUEST_AT_KEY = "review_last_request_at"
    }
}
