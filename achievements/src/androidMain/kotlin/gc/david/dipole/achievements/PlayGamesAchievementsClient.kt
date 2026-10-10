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


package gc.david.dipole.achievements

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk

/**
 * Google Play Games. The IDs come from `games-ids.xml`, exported from Play Console into the app's
 * resources: `game_services_project_id` and one `achievement_<key>` string per [Achievement]. Play
 * signs the player in silently; without a Play Games profile the reports are simply dropped.
 */
class PlayGamesAchievementsClient private constructor(
    private val activity: Activity,
) : AchievementsClient {

    override val isAvailable = true

    override fun start(onSignedIn: () -> Unit) {
        PlayGamesSdk.initialize(activity)
        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) onSignedIn()
        }
    }

    override fun unlock(achievement: Achievement) {
        val id = idOf(achievement) ?: return
        PlayGames.getAchievementsClient(activity).unlock(id)
    }

    override fun setProgress(achievement: Achievement, steps: Int) {
        val id = idOf(achievement) ?: return
        // Play rejects 0 steps; progress only ever goes up.
        if (steps > 0) PlayGames.getAchievementsClient(activity).setSteps(id, steps)
    }

    override fun showAchievements() {
        PlayGames.getAchievementsClient(activity).achievementsIntent.addOnSuccessListener { intent ->
            @Suppress("DEPRECATION")
            activity.startActivityForResult(intent, SHOW_ACHIEVEMENTS_REQUEST)
        }
    }

    /** The Play ID for [achievement], or null until it's in `games-ids.xml`. */
    private fun idOf(achievement: Achievement): String? =
        activity.stringResource("achievement_${achievement.key}")

    companion object {

        private const val SHOW_ACHIEVEMENTS_REQUEST = 9_001

        /** Play Games for [activity], or [AchievementsClient.None] until the app has a Play Games project ID. */
        fun createOrNone(activity: Activity): AchievementsClient =
            if (activity.stringResource("game_services_project_id") == null) {
                AchievementsClient.None
            } else {
                PlayGamesAchievementsClient(activity)
            }

        // The IDs are looked up by name so the module doesn't depend on the app's R class.
        @SuppressLint("DiscouragedApi")
        private fun Activity.stringResource(name: String): String? {
            val id = resources.getIdentifier(name, "string", packageName)
            return if (id == 0) null else getString(id).takeIf { it.isNotBlank() }
        }
    }
}
