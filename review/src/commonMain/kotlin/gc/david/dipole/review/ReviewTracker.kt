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

import kotlin.time.Clock

/** The app's way in: it reports what happens and the tracker asks for a review when [ReviewPolicy] allows. */
class ReviewTracker(
    private val store: ReviewStore,
    private val clock: Clock,
) {

    fun onAppLaunched() {
        val now = clock.now()
        store.update { it.copy(launches = it.launches + 1, firstLaunchAt = it.firstLaunchAt ?: now) }
    }

    fun onGameFinished() {
        store.update { it.copy(finishedGames = it.finishedGames + 1) }
    }

    /**
     * A good moment to ask, like a win. [prompter] is passed per call because on Android it belongs
     * to the current Activity, which changes on rotation.
     */
    fun onHappyMoment(prompter: ReviewPrompter) {
        val now = clock.now()
        if (!ReviewPolicy.shouldAsk(store.stats, now)) return

        store.update { it.copy(lastRequestAt = now) }
        prompter.request()
    }
}
