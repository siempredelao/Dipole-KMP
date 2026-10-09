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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class ReviewPolicyTest {

    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)

    private val ready = ReviewStats(
        finishedGames = ReviewPolicy.MIN_FINISHED_GAMES,
        launches = ReviewPolicy.MIN_LAUNCHES,
        firstLaunchAt = now - ReviewPolicy.MIN_DAYS_SINCE_FIRST_LAUNCH.days,
        lastRequestAt = null,
    )

    @Test
    fun asksWhenEveryRulePasses() {
        assertTrue(ReviewPolicy.shouldAsk(ready, now))
    }

    @Test
    fun waitsForEnoughFinishedGames() {
        assertFalse(ReviewPolicy.shouldAsk(ready.copy(finishedGames = ReviewPolicy.MIN_FINISHED_GAMES - 1), now))
    }

    @Test
    fun waitsForASecondLaunch() {
        assertFalse(ReviewPolicy.shouldAsk(ready.copy(launches = ReviewPolicy.MIN_LAUNCHES - 1), now))
    }

    @Test
    fun waitsSomeDaysAfterTheFirstLaunch() {
        val tooRecent = ready.copy(firstLaunchAt = ready.firstLaunchAt!! + 1.milliseconds)
        assertFalse(ReviewPolicy.shouldAsk(tooRecent, now))
    }

    @Test
    fun neverAsksWithoutAFirstLaunch() {
        assertFalse(ReviewPolicy.shouldAsk(ready.copy(firstLaunchAt = null), now))
    }

    @Test
    fun waitsBetweenRequests() {
        val gap = ReviewPolicy.MIN_DAYS_BETWEEN_REQUESTS.days
        assertFalse(ReviewPolicy.shouldAsk(ready.copy(lastRequestAt = now - gap + 1.milliseconds), now))
        assertTrue(ReviewPolicy.shouldAsk(ready.copy(lastRequestAt = now - gap), now))
    }
}
