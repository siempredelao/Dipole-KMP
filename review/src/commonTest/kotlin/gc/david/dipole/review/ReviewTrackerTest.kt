package gc.david.dipole.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class ReviewTrackerTest {

    private class FakeClock(var now: Instant) : Clock {
        override fun now() = now
        fun advance(by: Duration) { now += by }
    }

    private class InMemoryReviewStore(override var stats: ReviewStats = ReviewStats()) : ReviewStore {
        override fun update(change: (ReviewStats) -> ReviewStats) { stats = change(stats) }
    }

    private val start = Instant.fromEpochMilliseconds(1_800_000_000_000)
    private val clock = FakeClock(start)
    private val store = InMemoryReviewStore()
    private val tracker = ReviewTracker(store, clock)
    private var requests = 0
    private val prompter = ReviewPrompter { requests++ }

    @Test
    fun countsLaunchesAndKeepsTheFirstLaunchDate() {
        tracker.onAppLaunched()
        clock.advance(1.days)
        tracker.onAppLaunched()

        assertEquals(2, store.stats.launches)
        assertEquals(start, store.stats.firstLaunchAt)
    }

    @Test
    fun countsFinishedGames() {
        tracker.onGameFinished()
        tracker.onGameFinished()

        assertEquals(2, store.stats.finishedGames)
    }

    @Test
    fun doesNotAskTooEarly() {
        tracker.onAppLaunched()
        tracker.onHappyMoment(prompter)

        assertEquals(0, requests)
        assertEquals(null, store.stats.lastRequestAt)
    }

    @Test
    fun asksOnceTheRulesAllowAndThenWaits() {
        makeReady()

        tracker.onHappyMoment(prompter)
        assertEquals(1, requests)
        assertEquals(clock.now, store.stats.lastRequestAt)

        clock.advance(1.days)
        tracker.onHappyMoment(prompter)
        assertEquals(1, requests)

        clock.advance(ReviewPolicy.MIN_DAYS_BETWEEN_REQUESTS.days)
        tracker.onHappyMoment(prompter)
        assertEquals(2, requests)
    }

    private fun makeReady() {
        repeat(ReviewPolicy.MIN_LAUNCHES) { tracker.onAppLaunched() }
        repeat(ReviewPolicy.MIN_FINISHED_GAMES) { tracker.onGameFinished() }
        clock.advance(ReviewPolicy.MIN_DAYS_SINCE_FIRST_LAUNCH.days)
    }
}
