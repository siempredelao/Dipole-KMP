package gc.david.dipole.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme

/** Colours around the board: background and text, which change between light and dark mode. */
data class AppColors(
    val background: Color,
    val text: Color,
    val secondaryText: Color,
    val outline: Color,
    /** Hint text, readable on [background]. */
    val hint: Color,
    /** Confetti for a win, one colour per [gc.david.dipole.celebration.ConfettiLauncher.COLOR_COUNT]. */
    val confetti: List<Color> = ConfettiColors,
)

/** Bright enough for the dark background, deep enough for the light one. */
private val ConfettiColors = listOf(
    Color(0xFFE63946), // red
    Color(0xFFFFB02E), // amber, as in the app icon
    Color(0xFF2A9D8F), // teal
    Color(0xFF457B9D), // blue
    Color(0xFF9B5DE5), // purple
    Color(0xFF43AA8B), // green
)

private val DarkAppColors = AppColors(
    background = Color(0xFF1E1A17),
    text = Color.White,
    secondaryText = Color(0xFFC8C8C8),
    outline = Color.Gray,
    hint = Color(0xFF5CC8F2),
)

private val LightAppColors = AppColors(
    background = Color(0xFFF6F1E7),
    text = Color(0xFF2B2420),
    secondaryText = Color(0xFF5E544C),
    outline = Color(0xFF8A8078),
    hint = Color(0xFF0A6E99),
)

/** The board's colours, the same in light and dark mode. */
data class BoardColors(
    val lightSquare: Color,
    val darkSquare: Color,
    val border: Color,
    /** Tint on the squares of the last move. */
    val lastMove: Color,
    /** Squares a selected stack can move to, and squares it can capture on. */
    val target: Color,
    val capture: Color,
    /** Colour of the count shown inside target circles. */
    val targetText: Color,
    val hint: Color,
    val selectedRing: Color,
    val movableRing: Color,
    val checkerOutline: Color,
    val checkerOutlineWidth: Dp = 2.dp,
    /**
     * Marks that don't rely on colour alone: dashed rings on captures and outlines on the last move
     * and the hint.
     */
    val strongMarks: Boolean = false,
)

val BoardTheme.colors: BoardColors
    get() = when (this) {
        BoardTheme.Wood -> BoardColors(
            lightSquare = Color(0xFFEBD3A8),
            darkSquare = Color(0xFF7A4E2D),
            border = Color(0xFF4A2E1A),
            lastMove = Color(0x55F2D95C),
            target = Color(0xFF7FC97F),
            capture = Color(0xFFE0605A),
            targetText = Color.White,
            hint = Color(0xFF5CC8F2),
            selectedRing = Color(0xFFF2D95C),
            movableRing = Color(0xFFB8A27A),
            checkerOutline = Color.Gray,
        )
        BoardTheme.Tournament -> BoardColors(
            lightSquare = Color(0xFFEEEED2),
            darkSquare = Color(0xFF769656),
            border = Color(0xFF4B6331),
            lastMove = Color(0x66F6F669),
            target = Color(0xFF4FA3E0),
            capture = Color(0xFFE0605A),
            targetText = Color.White,
            hint = Color(0xFFFFB74D),
            selectedRing = Color(0xFFF6F669),
            movableRing = Color(0xFFDDE6C8),
            checkerOutline = Color.Gray,
        )
        BoardTheme.Slate -> BoardColors(
            lightSquare = Color(0xFFC9D3DD),
            darkSquare = Color(0xFF4A6278),
            border = Color(0xFF2E3D4B),
            lastMove = Color(0x55F2D95C),
            target = Color(0xFF7FC97F),
            capture = Color(0xFFE0605A),
            targetText = Color.White,
            hint = Color(0xFFFFB74D),
            selectedRing = Color(0xFFF2D95C),
            movableRing = Color(0xFF9FB3C8),
            checkerOutline = Color.Gray,
        )
        BoardTheme.Marble -> BoardColors(
            lightSquare = Color(0xFFE6E6E6),
            darkSquare = Color(0xFF8C8C8C),
            border = Color(0xFF555555),
            lastMove = Color(0x55F2D95C),
            target = Color(0xFF7FC97F),
            capture = Color(0xFFE0605A),
            targetText = Color.White,
            hint = Color(0xFF5CC8F2),
            selectedRing = Color(0xFFF2D95C),
            movableRing = Color(0xFFD0D0D0),
            checkerOutline = Color(0xFF555555),
        )
        // Okabe-Ito colours, which stay distinct with colour blindness. Every mark keeps at least a
        // 3:1 contrast with the navy squares it sits on (WCAG non-text contrast).
        BoardTheme.HighContrast -> BoardColors(
            lightSquare = Color(0xFFF2F2F2),
            darkSquare = Color(0xFF1F3A5F),
            border = Color.Black,
            lastMove = Color(0xFFFFD400),
            target = Color(0xFF56B4E9),
            capture = Color(0xFFE69F00),
            targetText = Color.Black,
            hint = Color(0xFFCC79A7),
            selectedRing = Color(0xFFFFD400),
            movableRing = Color.White,
            checkerOutline = Color.White,
            checkerOutlineWidth = 3.dp,
            strongMarks = true,
        )
    }

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
val LocalBoardColors = staticCompositionLocalOf { BoardTheme.Wood.colors }

@Composable
fun DipoleTheme(
    mode: AppearanceMode = AppearanceMode.System,
    board: BoardTheme = BoardTheme.Wood,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        AppearanceMode.System -> isSystemInDarkTheme()
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    val appColors = if (dark) DarkAppColors else LightAppColors
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        CompositionLocalProvider(LocalAppColors provides appColors, LocalBoardColors provides board.colors) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = appColors.background,
                contentColor = appColors.text,
                content = content,
            )
        }
    }
}
