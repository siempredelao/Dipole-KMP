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

import com.russhwolf.settings.Settings

/** The player's totals and the achievements already earned on this device. */
interface AchievementStore {

    var totals: PlayerTotals

    var earned: Set<Achievement>
}

/** Stores them in [settings] under keys starting with `achievements_`, so it can share the app's store. */
class SettingsAchievementStore(
    private val settings: Settings,
) : AchievementStore {

    override var totals: PlayerTotals
        get() = PlayerTotals(winsVsComputer = settings.getInt(WINS_KEY, 0))
        set(value) = settings.putInt(WINS_KEY, value.winsVsComputer)

    override var earned: Set<Achievement>
        get() = settings.getString(EARNED_KEY, "").split(',')
            .mapNotNull { key -> Achievement.entries.firstOrNull { it.key == key } }
            .toSet()
        set(value) = settings.putString(EARNED_KEY, value.joinToString(",") { it.key })

    private companion object {

        const val WINS_KEY = "achievements_wins_vs_computer"
        const val EARNED_KEY = "achievements_earned"
    }
}
