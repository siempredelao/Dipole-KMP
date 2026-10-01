package gc.david.dipole.ui

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Hint
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGame
import gc.david.dipole.tutorial.TutorialPage
import kotlin.time.Instant

/** Everything [DipoleScreen] shows. */
data class DipoleUiState(
    val session: GameSession,
    val selected: Square? = null,
    val dialog: DipoleDialog? = null,
    /** A short confirmation such as "Game saved", cleared once shown. */
    val message: DipoleMessage? = null,
    val hasSavedGames: Boolean = false,
    /** True once the player tapped Hint this turn; switched off when the turn passes. */
    val hintsOn: Boolean = false,
    /** The recommended move from the selected stack, once worked out. */
    val hint: Hint? = null,
    /** True while the rules screen is open on top of the game. */
    val showRules: Boolean = false,
    /** The tutorial page shown on top of everything else, or null when the tutorial is closed. */
    val tutorialPage: TutorialPage? = null,
    /** Goes up by one each time a human wins, telling one celebration from the next. */
    val celebration: Int = 0,
    /** True from a human win until the screen starts its confetti, so it is thrown only once. */
    val celebrating: Boolean = false,
    /** True while the settings menu (New game, Save, Load) is open. */
    val menuOpen: Boolean = false,
) {
    val state: GameState get() = session.state

    private val movesFromSelected: List<Move> get() = selected?.let { state.legalMovesFrom(it) }.orEmpty()

    /** Moves from the selected stack that stay on the board, by destination square. */
    val targets: Map<Square, Move> get() = movesFromSelected.filter { it.to.isOnBoard }.associateBy { it.to }

    /** Moves from the selected stack that leave the board. */
    val bearOffs: List<Move> get() = movesFromSelected.filter { !it.to.isOnBoard }

    /** Squares holding a stack the player can move now. */
    val movable: Set<Square>
        get() = if (session.isComputerTurn) emptySet() else state.legalMoves().map { it.from }.toSet()

    /** Hints are offered whenever a human is to move. */
    val canHint: Boolean get() = !session.isComputerTurn && !state.isOver
}

sealed interface DipoleDialog {
    data object NewGame : DipoleDialog

    /**
     * Second step of New game against the computer: the human picks a [side] (initially the one
     * picked last time) and a difficulty, with [suggested] picked last time.
     */
    data class ChooseDifficulty(val suggested: Difficulty, val side: Player) : DipoleDialog

    /** Asks for a save name; the default name comes from [savedAt], formatted for the user's language. */
    data class Save(val savedAt: Instant) : DipoleDialog
    data class Load(val savedGames: List<SavedGame>) : DipoleDialog
    data object Appearance : DipoleDialog
}

enum class DipoleMessage { GameSaved, GameLoaded, SaveUnreadable }

sealed interface DipoleAction {
    data class SquareTapped(val square: Square) : DipoleAction
    data class BearOffChosen(val move: Move) : DipoleAction
    data object NewGameClicked : DipoleAction
    data class ModeChosen(val mode: GameMode) : DipoleAction
    data class SideChosen(val side: Player) : DipoleAction
    data class DifficultyChosen(val difficulty: Difficulty) : DipoleAction
    data object BackToModeClicked : DipoleAction
    data object UndoClicked : DipoleAction
    data object HintClicked : DipoleAction
    data object RulesClicked : DipoleAction
    data object MenuClicked : DipoleAction
    data object MenuDismissed : DipoleAction
    data object AppearanceClicked : DipoleAction
    data object RulesClosed : DipoleAction
    data object TutorialClicked : DipoleAction
    /** The player swiped to [page]. */
    data class TutorialPageShown(val page: TutorialPage) : DipoleAction
    data object TutorialNextClicked : DipoleAction
    data object TutorialBackClicked : DipoleAction
    /** Skip, Start playing or system back: closes the tutorial for good. */
    data object TutorialClosed : DipoleAction
    data object SaveClicked : DipoleAction
    data class SaveConfirmed(val name: String) : DipoleAction
    data object LoadClicked : DipoleAction
    data class SavedGameChosen(val game: SavedGame) : DipoleAction
    data class SavedGameDeleted(val game: SavedGame) : DipoleAction
    data object DialogDismissed : DipoleAction
    data object MessageShown : DipoleAction
    data object CelebrationShown : DipoleAction
}
