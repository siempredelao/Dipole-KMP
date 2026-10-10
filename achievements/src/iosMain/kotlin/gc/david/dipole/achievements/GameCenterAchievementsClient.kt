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

import platform.GameKit.GKAccessPoint
import platform.GameKit.GKAchievement
import platform.GameKit.GKGameCenterViewControllerStateAchievements
import platform.GameKit.GKLocalPlayer
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

/**
 * Apple's Game Center. The achievement IDs are [Achievement.appleId], created with the same IDs in App
 * Store Connect. If the player isn't signed in to Game Center, nothing is reported.
 */
class GameCenterAchievementsClient : AchievementsClient {

    override val isAvailable = true

    private val player get() = GKLocalPlayer.localPlayer

    override fun start(onSignedIn: () -> Unit) {
        player.authenticateHandler = { signInScreen, _ ->
            when {
                // Game Center asks once; afterwards the player can sign in from Settings.
                signInScreen != null -> rootViewController()?.presentViewController(signInScreen, animated = true, completion = null)
                player.authenticated -> onSignedIn()
            }
        }
    }

    override fun unlock(achievement: Achievement) = report(achievement, percent = 100.0)

    override fun setProgress(achievement: Achievement, steps: Int) {
        val total = achievement.steps ?: return
        report(achievement, percent = 100.0 * steps / total)
    }

    override fun showAchievements() {
        GKAccessPoint.shared.triggerAccessPointWithState(GKGameCenterViewControllerStateAchievements) {}
    }

    private fun report(achievement: Achievement, percent: Double) {
        if (!player.authenticated) return
        val report = GKAchievement(identifier = achievement.appleId).apply {
            percentComplete = percent
            showsCompletionBanner = true
        }
        GKAchievement.reportAchievements(listOf(report), withCompletionHandler = null)
    }

    private fun rootViewController(): UIViewController? =
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.keyWindow
            ?.rootViewController
}
