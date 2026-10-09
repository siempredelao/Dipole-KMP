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

/** How the stores know the app. */
data class StoreApp(
    /** The Android application ID, as in Google Play. */
    val androidPackage: String,
    /** The numeric App Store ID, once the app is on the App Store. */
    val appleAppId: String? = null,
)

/** The app's page in the store, for a "Rate" menu entry. */
expect object StoreLinks {

    /** The link to rate [app] on this platform, or null where there is no store (desktop, web) or no listing yet. */
    fun storePageUrl(app: StoreApp): String?
}
