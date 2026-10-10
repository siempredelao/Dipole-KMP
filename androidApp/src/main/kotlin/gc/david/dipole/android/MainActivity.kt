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

package gc.david.dipole.android

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import gc.david.dipole.achievements.PlayGamesAchievementsClient
import gc.david.dipole.review.PlayReviewPrompter
import gc.david.dipole.ui.DipoleApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Android 12+ animates the splash icon: keep it up until all three arrows have appeared.
        // Not on a recreation (e.g. rotation), when there is no splash to wait for.
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val shownAt = SystemClock.uptimeMillis()
            splashScreen.setKeepOnScreenCondition { SystemClock.uptimeMillis() - shownAt < SPLASH_ANIMATION_MILLIS }
        }

        // No custom exit animation: Android 12+ draws the splash icon on its own surface, which
        // ignored our fade and vanished before the background, making the splash blink. The
        // system's own exit fades icon and background together.
        val achievements = PlayGamesAchievementsClient.createOrNone(this)
        setContent { DipoleApp(reviewPrompter = PlayReviewPrompter(this), achievements = achievements) }
    }

    private companion object {

        /** Length of drawable-v31/splash_icon_animated.xml. */
        const val SPLASH_ANIMATION_MILLIS = 1_350L
    }
}
