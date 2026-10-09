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

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Google Play's in-app review sheet. Play only shows it to apps installed from Play, and decides on
 * its own quota whether it appears; failures are ignored, as there is nothing to tell the player.
 */
class PlayReviewPrompter(
    private val activity: Activity,
) : ReviewPrompter {

    override fun request() {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful && !activity.isFinishing) {
                manager.launchReviewFlow(activity, request.result)
            }
        }
    }
}
