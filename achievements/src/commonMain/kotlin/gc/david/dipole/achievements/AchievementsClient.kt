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

/** Reports achievements to the platform's game service: Play Games on Android, Game Center on iOS. */
interface AchievementsClient {

    /** False where there is no game service, which hides the Achievements menu entry. */
    val isAvailable: Boolean

    /** Signs the player in, if needed, and calls [onSignedIn] once reports can be sent. */
    fun start(onSignedIn: () -> Unit)

    fun unlock(achievement: Achievement)

    /** For incremental achievements: [steps] done so far, out of [Achievement.steps]. */
    fun setProgress(achievement: Achievement, steps: Int)

    /** Opens the service's own achievements screen. */
    fun showAchievements()

    /** For platforms without a game service, previews and tests. */
    object None : AchievementsClient {
        override val isAvailable = false
        override fun start(onSignedIn: () -> Unit) = Unit
        override fun unlock(achievement: Achievement) = Unit
        override fun setProgress(achievement: Achievement, steps: Int) = Unit
        override fun showAchievements() = Unit
    }
}
