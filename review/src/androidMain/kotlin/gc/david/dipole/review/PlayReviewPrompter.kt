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
