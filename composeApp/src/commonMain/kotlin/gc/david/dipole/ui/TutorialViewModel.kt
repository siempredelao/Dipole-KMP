package gc.david.dipole.ui

import androidx.lifecycle.ViewModel
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.tutorial.TutorialPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The tutorial's current [page]; [finished] once it was skipped or completed, to leave the screen. */
data class TutorialUiState(
    val page: TutorialPage = TutorialPage.entries.first(),
    val finished: Boolean = false,
)

sealed interface TutorialAction {
    /** The player swiped to [page]. */
    data class PageShown(val page: TutorialPage) : TutorialAction
    data object NextClicked : TutorialAction
    data object BackClicked : TutorialAction

    /** Skip, or system back on the first page. */
    data object Closed : TutorialAction
}

class TutorialViewModel(private val preferences: GamePreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()

    fun onAction(action: TutorialAction) {
        if (_uiState.value.finished) return
        when (action) {
            is TutorialAction.PageShown -> _uiState.update { it.copy(page = action.page) }
            TutorialAction.NextClicked -> {
                val next = TutorialPage.entries.getOrNull(_uiState.value.page.ordinal + 1)
                if (next == null) finish() else _uiState.update { it.copy(page = next) }
            }
            TutorialAction.BackClicked -> _uiState.update {
                it.copy(page = TutorialPage.entries.getOrNull(it.page.ordinal - 1) ?: it.page)
            }
            TutorialAction.Closed -> finish()
        }
    }

    /** Once finished or skipped, the tutorial no longer opens on launch. */
    private fun finish() {
        preferences.tutorialSeen = true
        _uiState.update { it.copy(finished = true) }
    }

    companion object {
        /** The very first launch opens the tutorial instead of the game. */
        fun opensOnLaunch(preferences: GamePreferences): Boolean = !preferences.tutorialSeen
    }
}
