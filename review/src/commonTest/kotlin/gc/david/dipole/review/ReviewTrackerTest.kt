/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Dipole, the game and its rules, was designed by Mark Steere
 * (Copyright (c) May 2007 by Mark Steere, https://www.marksteeregames.com).
 * This code, an implementation of the game, was developed by David Aguiar Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
