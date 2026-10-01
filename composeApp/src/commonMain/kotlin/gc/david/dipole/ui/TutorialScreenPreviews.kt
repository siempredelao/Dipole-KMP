package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.tutorial.TutorialPage

@Composable
private fun TutorialPreview(page: TutorialPage, mode: AppearanceMode = AppearanceMode.Dark) {
    DipoleTheme(mode = mode) {
        TutorialScreen(page, onAction = {})
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialGoalPreview() = TutorialPreview(TutorialPage.Goal)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialMovingPreview() = TutorialPreview(TutorialPage.Moving)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialPlainMovesPreview() = TutorialPreview(TutorialPage.PlainMoves)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialCapturesPreview() = TutorialPreview(TutorialPage.Captures)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialOffBoardPreview() = TutorialPreview(TutorialPage.OffBoard)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialSittingOutPreview() = TutorialPreview(TutorialPage.SittingOut)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialTipsPreview() = TutorialPreview(TutorialPage.Tips)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialLightPreview() = TutorialPreview(TutorialPage.Captures, AppearanceMode.Light)
