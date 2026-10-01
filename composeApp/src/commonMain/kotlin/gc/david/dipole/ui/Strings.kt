package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Player
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.date_format
import gc.david.dipole.resources.difficulty_easy
import gc.david.dipole.resources.difficulty_easy_description
import gc.david.dipole.resources.difficulty_hard
import gc.david.dipole.resources.difficulty_hard_description
import gc.david.dipole.resources.difficulty_medium
import gc.david.dipole.resources.difficulty_medium_description
import gc.david.dipole.resources.message_game_loaded
import gc.david.dipole.resources.message_game_saved
import gc.david.dipole.resources.message_save_unreadable
import gc.david.dipole.resources.mode_two_players
import gc.david.dipole.resources.mode_vs_computer
import gc.david.dipole.resources.months_short
import gc.david.dipole.resources.player_black
import gc.david.dipole.resources.player_white
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

// Translated texts for game values. The strings live in composeResources/values*/strings.xml.

val Difficulty.label: StringResource
    get() = when (this) {
        Difficulty.Easy -> Res.string.difficulty_easy
        Difficulty.Medium -> Res.string.difficulty_medium
        Difficulty.Hard -> Res.string.difficulty_hard
    }

val Difficulty.description: StringResource
    get() = when (this) {
        Difficulty.Easy -> Res.string.difficulty_easy_description
        Difficulty.Medium -> Res.string.difficulty_medium_description
        Difficulty.Hard -> Res.string.difficulty_hard_description
    }

val Player.label: StringResource
    get() = if (this == Player.White) Res.string.player_white else Res.string.player_black

val GameMessage.text: StringResource
    get() = when (this) {
        GameMessage.GameSaved -> Res.string.message_game_saved
        GameMessage.GameLoaded -> Res.string.message_game_loaded
        GameMessage.SaveUnreadable -> Res.string.message_save_unreadable
    }

/** "vs Computer · Easy" or "2 Players". */
@Composable
fun modeLabel(mode: GameMode, difficulty: Difficulty): String =
    if (mode == GameMode.VsComputer) {
        "${stringResource(Res.string.mode_vs_computer)} · ${stringResource(difficulty.label)}"
    } else {
        stringResource(Res.string.mode_two_players)
    }

/** A date and time like "30 Sep 2026 21:42", in the user's language and time zone. */
@Composable
fun formatDate(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = instant.toLocalDateTime(timeZone)
    val month = stringArrayResource(Res.array.months_short)[t.month.ordinal]
    val time = "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
    return stringResource(Res.string.date_format, t.day, month, t.year, time)
}
