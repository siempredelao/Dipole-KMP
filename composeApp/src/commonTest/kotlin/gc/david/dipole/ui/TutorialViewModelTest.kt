package gc.david.dipole.ui

import gc.david.dipole.tutorial.TutorialPage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TutorialViewModelTest {

    private val preferences = FakeGamePreferences()

    @Test
    fun theTutorialOpensOnTheFirstLaunchOnly() {
        assertTrue(TutorialViewModel.opensOnLaunch(preferences))
        preferences.tutorialSeen = true
        assertFalse(TutorialViewModel.opensOnLaunch(preferences))
    }

    @Test
    fun nextAndBackMoveThroughThePages() {
        val vm = TutorialViewModel(preferences)
        vm.onAction(TutorialAction.BackClicked)
        assertEquals(TutorialPage.Goal, vm.uiState.value.page)
        vm.onAction(TutorialAction.NextClicked)
        vm.onAction(TutorialAction.NextClicked)
        assertEquals(TutorialPage.PlainMoves, vm.uiState.value.page)
        vm.onAction(TutorialAction.BackClicked)
        assertEquals(TutorialPage.Moving, vm.uiState.value.page)
        vm.onAction(TutorialAction.PageShown(TutorialPage.Captures))
        assertEquals(TutorialPage.Captures, vm.uiState.value.page)
        assertFalse(vm.uiState.value.finished)
    }

    @Test
    fun nextOnTheLastPageFinishesTheTutorial() {
        val vm = TutorialViewModel(preferences)
        vm.onAction(TutorialAction.PageShown(TutorialPage.entries.last()))
        vm.onAction(TutorialAction.NextClicked)
        assertTrue(vm.uiState.value.finished)
        assertTrue(preferences.tutorialSeen)
    }

    @Test
    fun skippingMarksTheTutorialSeen() {
        val vm = TutorialViewModel(preferences)
        vm.onAction(TutorialAction.Closed)
        assertTrue(vm.uiState.value.finished)
        assertTrue(preferences.tutorialSeen)
    }

    @Test
    fun reopeningStartsAtTheFirstPageEvenWhenSeen() {
        preferences.tutorialSeen = true
        assertEquals(TutorialPage.Goal, TutorialViewModel(preferences).uiState.value.page)
    }

    @Test
    fun aLateSwipeAfterFinishingChangesNothing() {
        val vm = TutorialViewModel(preferences)
        vm.onAction(TutorialAction.Closed)
        vm.onAction(TutorialAction.PageShown(TutorialPage.Moving))
        assertEquals(TutorialPage.Goal, vm.uiState.value.page)
        assertTrue(vm.uiState.value.finished)
    }

    @Test
    fun captureExamplesAreMarkedAsCaptures() {
        val captureTargets = TutorialViewModel(preferences).uiState.value.captureTargets
        assertEquals(TutorialPage.Captures.examples.map { it.to }.toSet(), captureTargets[TutorialPage.Captures])
        assertEquals(emptySet(), captureTargets[TutorialPage.Moving])
    }
}
