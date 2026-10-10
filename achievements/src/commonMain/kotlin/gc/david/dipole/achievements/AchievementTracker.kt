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

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameMode

/**
 * The app's way in: it reports finished games and the tutorial, and the tracker works out what was
 * earned, keeps it on the device and reports it to the [AchievementsClient] in use.
 *
 * The client belongs to the current screen (on Android, the Activity), so the app [attach]es it. Once
 * the player is signed in, everything earned so far is sent again: the stores ignore repeats, and this
 * catches up on anything earned while signed out or before the store was set up.
 */
class AchievementTracker(
    private val store: AchievementStore,
) {

    private var client: AchievementsClient = AchievementsClient.None

    fun attach(client: AchievementsClient) {
        this.client = client
        client.start { if (this.client === client) sendAll(client) }
    }

    fun detach(client: AchievementsClient) {
        if (this.client === client) this.client = AchievementsClient.None
    }

    fun onTutorialFinished() {
        earn(setOf(Achievement.FirstSteps))
    }

    fun onGameFinished(game: FinishedGame) {
        val session = game.session
        val winner = DipoleRules.winner(session.state) ?: return
        if (session.mode == GameMode.VsComputer && winner == session.humanSide) {
            store.totals = store.totals.copy(winsVsComputer = store.totals.winsVsComputer + 1)
            AchievementRules.progress(store.totals).forEach { (achievement, steps) -> client.setProgress(achievement, steps) }
        }
        earn(AchievementRules.earned(game, store.totals))
    }

    private fun earn(achievements: Set<Achievement>) {
        val new = achievements - store.earned
        if (new.isEmpty()) return

        store.earned += new
        new.filter { it.steps == null }.forEach(client::unlock)
    }

    private fun sendAll(client: AchievementsClient) {
        store.earned.filter { it.steps == null }.forEach(client::unlock)
        AchievementRules.progress(store.totals).forEach { (achievement, steps) -> client.setProgress(achievement, steps) }
    }
}
