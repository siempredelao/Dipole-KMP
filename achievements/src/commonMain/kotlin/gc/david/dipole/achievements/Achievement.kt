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

/**
 * Everything a player can earn. [key] names it in the stores: Game Center uses `gc.david.dipole.<key>`
 * and the Play Games IDs are looked up as `achievement_<key>` in the app's resources. Keys never change
 * once published. [steps] is set for the ones that fill up over several games.
 */
enum class Achievement(val key: String, val steps: Int? = null) {
    FirstSteps("first_steps"),
    FirstVictory("first_victory"),
    FriendlyMatch("friendly_match"),
    RisingPlayer("rising_player"),
    Grandmaster("grandmaster"),
    BothSides("both_sides"),
    Flawless("flawless"),
    NoHelpNeeded("no_help_needed"),
    BigCapture("big_capture"),
    SwiftVictory("swift_victory"),
    Regular("regular", steps = 10),
    DipoleMaster("dipole_master", steps = 50),
    AiImpostor("ai_impostor"),
    ;

    val appleId: String get() = "gc.david.dipole.$key"
}
