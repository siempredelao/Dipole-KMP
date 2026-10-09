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
