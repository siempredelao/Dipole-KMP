package gc.david.dipole

import androidx.compose.ui.window.ComposeUIViewController
import gc.david.dipole.review.IosReviewPrompter
import gc.david.dipole.ui.DipoleApp

fun MainViewController() = ComposeUIViewController { DipoleApp(reviewPrompter = IosReviewPrompter()) }
