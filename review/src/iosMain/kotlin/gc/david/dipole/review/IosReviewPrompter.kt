package gc.david.dipole.review

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindowScene

/**
 * Apple's rating dialog. iOS shows it at most 3 times a year (always in debug builds, never in
 * TestFlight) and doesn't say whether it appeared.
 */
class IosReviewPrompter : ReviewPrompter {

    override fun request() {
        val scene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: return
        SKStoreReviewController.requestReviewInScene(scene)
    }
}
