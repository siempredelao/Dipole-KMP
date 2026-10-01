package gc.david.dipole.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gc.david.dipole.game.BOARD_SIZE
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Hint
import gc.david.dipole.game.Move
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.bear_off_button
import gc.david.dipole.resources.bear_off_explanation
import gc.david.dipole.resources.hint
import gc.david.dipole.resources.hint_best
import gc.david.dipole.resources.hint_better_elsewhere
import gc.david.dipole.resources.hint_pick_stack
import gc.david.dipole.resources.hint_thinking
import gc.david.dipole.resources.ic_settings
import gc.david.dipole.resources.load_game_title
import gc.david.dipole.resources.new_game
import gc.david.dipole.resources.ic_info
import gc.david.dipole.resources.rules_title
import gc.david.dipole.resources.save_game_title
import gc.david.dipole.resources.settings
import gc.david.dipole.resources.sound_and_vibration
import gc.david.dipole.resources.status_black_sits_out
import gc.david.dipole.resources.status_black_to_move
import gc.david.dipole.resources.status_black_wins
import gc.david.dipole.resources.status_computer_thinking
import gc.david.dipole.resources.status_computer_wins
import gc.david.dipole.resources.status_white_sits_out
import gc.david.dipole.resources.status_white_to_move
import gc.david.dipole.resources.status_white_wins
import gc.david.dipole.resources.status_you_win
import gc.david.dipole.resources.status_your_move_black
import gc.david.dipole.resources.status_your_move_white
import gc.david.dipole.resources.tap_target_help
import gc.david.dipole.resources.tray_off_board
import gc.david.dipole.resources.tray_on_board
import gc.david.dipole.resources.undo
import gc.david.dipole.sound.GameSound
import gc.david.dipole.sound.playSound
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val WhiteChecker = Color(0xFFF5F0E6)
private val BlackChecker = Color(0xFF26211E)

private val appColors: AppColors @Composable get() = LocalAppColors.current
private val boardColors: BoardColors @Composable get() = LocalBoardColors.current

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DipoleScreen(uiState: DipoleUiState, onAction: (DipoleAction) -> Unit) {
    if (uiState.showRules) {
        RulesScreen(onBack = { onAction(DipoleAction.RulesClosed) })
        return
    }
    val session = uiState.session
    val state = uiState.state
    val bearOffs = uiState.bearOffs
    val hintAlpha = blinkAlpha(uiState.hint)
    val haptics = LocalHapticFeedback.current
    val flight = rememberFlight(session) { landed ->
        if (uiState.soundOn) {
            playSound(if (landed.isCapture) GameSound.Capture else GameSound.Move)
            haptics.performHapticFeedback(
                if (landed.isCapture) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
            )
        }
    }
    val hintedMove = uiState.hint?.move

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                // An invisible spacer the size of the icon keeps the title centred.
                Spacer(Modifier.size(48.dp))
                Text("Dipole", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = appColors.text)
                IconButton(onClick = { onAction(DipoleAction.RulesClicked) }) {
                    Icon(
                        painterResource(Res.drawable.ic_info),
                        contentDescription = stringResource(Res.string.rules_title),
                        tint = appColors.secondaryText,
                    )
                }
            }
            SettingsMenu(uiState, onAction, Modifier.align(Alignment.CenterEnd))
        }
        Text(
            modeLabel(session.mode, session.difficulty),
            color = appColors.secondaryText,
            fontSize = 14.sp,
        )
        Text(
            statusText(session),
            color = appColors.text,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        val trayModifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
        // The human's side sits at the bottom: White, unless playing Black against the computer.
        val bottomPlayer = if (session.mode == GameMode.VsComputer) session.humanSide else Player.White
        val flipped = bottomPlayer == Player.Black
        PlayerTray(state, bottomPlayer.opponent, trayModifier)
        Board(
            state = state,
            flipped = flipped,
            flight = flight,
            selected = uiState.selected,
            movable = uiState.movable,
            targets = uiState.targets,
            lastMove = session.lastMove,
            hintedSquare = hintedMove?.to?.takeIf { it.isOnBoard },
            hintAlpha = hintAlpha,
            onSquareClick = { onAction(DipoleAction.SquareTapped(it)) },
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        )
        PlayerTray(state, bottomPlayer, trayModifier)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(DipoleAction.UndoClicked) }, enabled = session.canUndo) {
                Text(stringResource(Res.string.undo))
            }
            val onHint = { onAction(DipoleAction.HintClicked) }
            if (uiState.hintsOn) {
                Button(onClick = onHint, colors = ButtonDefaults.buttonColors(containerColor = appColors.hint)) {
                    Text(stringResource(Res.string.hint))
                }
            } else {
                OutlinedButton(onClick = onHint, enabled = uiState.canHint) { Text(stringResource(Res.string.hint)) }
            }
        }
        hintText(uiState)?.let { Text(stringResource(it), color = appColors.hint, textAlign = TextAlign.Center) }
        if (bearOffs.isNotEmpty()) {
            Text(
                stringResource(Res.string.bear_off_explanation),
                color = appColors.text,
                textAlign = TextAlign.Center,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bearOffs.forEach { move ->
                    val border = if (move == hintedMove) BorderStroke(3.dp, appColors.hint.copy(alpha = hintAlpha)) else null
                    OutlinedButton(
                        onClick = { onAction(DipoleAction.BearOffChosen(move)) },
                        border = border ?: ButtonDefaults.outlinedButtonBorder(),
                    ) {
                        Text(stringResource(Res.string.bear_off_button, move.count, arrow(move.direction, flipped)))
                    }
                }
            }
        } else if (uiState.selected != null) {
            Text(stringResource(Res.string.tap_target_help), color = appColors.secondaryText)
        }
        uiState.message?.let { message ->
            Text(stringResource(message.text), color = appColors.secondaryText)
            LaunchedEffect(message) {
                delay(MESSAGE_MILLIS)
                onAction(DipoleAction.MessageShown)
            }
        }
    }

    val dismiss = { onAction(DipoleAction.DialogDismissed) }
    when (val dialog = uiState.dialog) {
        DipoleDialog.NewGame -> NewGameDialog(
            onModeChosen = { onAction(DipoleAction.ModeChosen(it)) },
            onDismiss = dismiss,
        )
        is DipoleDialog.ChooseDifficulty -> DifficultyDialog(
            suggested = dialog.suggested,
            side = dialog.side,
            onSideChosen = { onAction(DipoleAction.SideChosen(it)) },
            onDifficultyChosen = { onAction(DipoleAction.DifficultyChosen(it)) },
            onBack = { onAction(DipoleAction.BackToModeClicked) },
            onDismiss = dismiss,
        )
        is DipoleDialog.Save -> SaveGameDialog(
            savedAt = dialog.savedAt,
            onSave = { onAction(DipoleAction.SaveConfirmed(it)) },
            onDismiss = dismiss,
        )
        is DipoleDialog.Load -> LoadGameDialog(
            savedGames = dialog.savedGames,
            onLoad = { onAction(DipoleAction.SavedGameChosen(it)) },
            onDelete = { onAction(DipoleAction.SavedGameDeleted(it)) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}

private const val MESSAGE_MILLIS = 2_000L

/** The settings icon and its menu: New game, Save game, Load game and the sound switch. */
@Composable
private fun SettingsMenu(uiState: DipoleUiState, onAction: (DipoleAction) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        IconButton(onClick = { onAction(DipoleAction.MenuClicked) }) {
            Icon(
                painterResource(Res.drawable.ic_settings),
                contentDescription = stringResource(Res.string.settings),
                tint = appColors.secondaryText,
            )
        }
        DropdownMenu(expanded = uiState.menuOpen, onDismissRequest = { onAction(DipoleAction.MenuDismissed) }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.new_game)) },
                onClick = { onAction(DipoleAction.NewGameClicked) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.save_game_title)) },
                onClick = { onAction(DipoleAction.SaveClicked) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.load_game_title)) },
                onClick = { onAction(DipoleAction.LoadClicked) },
                enabled = uiState.hasSavedGames,
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.sound_and_vibration)) },
                onClick = { onAction(DipoleAction.SoundToggled) },
                trailingIcon = { Switch(checked = uiState.soundOn, onCheckedChange = null) },
            )
        }
    }
}
private const val BLINKS = 3
private const val BLINK_HALF_MILLIS = 200

/**
 * Fades in and out [BLINKS] times whenever a new [hint] arrives, then stays at 0. Previews don't
 * animate, so there the hint is simply shown.
 */
@Composable
private fun blinkAlpha(hint: Hint?): Float {
    if (LocalInspectionMode.current) return if (hint == null) 0f else 1f
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(hint) {
        alpha.snapTo(0f)
        if (hint == null) return@LaunchedEffect
        repeat(BLINKS) {
            alpha.animateTo(1f, tween(BLINK_HALF_MILLIS))
            alpha.animateTo(0f, tween(BLINK_HALF_MILLIS))
        }
    }
    return alpha.value
}

private fun hintText(uiState: DipoleUiState): StringResource? {
    if (!uiState.hintsOn) return null
    val hint = uiState.hint
    return when {
        uiState.selected == null -> Res.string.hint_pick_stack
        hint == null -> Res.string.hint_thinking
        hint.betterMoveElsewhere -> Res.string.hint_better_elsewhere
        else -> Res.string.hint_best
    }
}

@Composable
private fun statusText(session: GameSession): String {
    val state = session.state
    val vsComputer = session.mode == GameMode.VsComputer
    state.winner?.let { winner ->
        val text = when {
            vsComputer && winner == session.humanSide -> Res.string.status_you_win
            vsComputer -> Res.string.status_computer_wins
            winner == Player.White -> Res.string.status_white_wins
            else -> Res.string.status_black_wins
        }
        return stringResource(text)
    }
    val turn = when {
        session.isComputerTurn -> Res.string.status_computer_thinking
        vsComputer && session.humanSide == Player.White -> Res.string.status_your_move_white
        vsComputer -> Res.string.status_your_move_black
        state.toMove == Player.White -> Res.string.status_white_to_move
        else -> Res.string.status_black_to_move
    }
    if (!session.opponentSatOut) return stringResource(turn)
    val satOut = when (state.toMove.opponent) {
        Player.White -> Res.string.status_white_sits_out
        Player.Black -> Res.string.status_black_sits_out
    }
    return "${stringResource(satOut)} ${stringResource(turn)}"
}

/** A player's checkers on the board, next to the pile of their checkers removed from play. */
@Composable
private fun PlayerTray(state: GameState, player: Player, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(16.dp).background(player.color, CircleShape).border(1.dp, appColors.outline, CircleShape))
        Text(
            stringResource(Res.string.tray_on_board, stringResource(player.label), state.checkersOf(player)),
            color = appColors.text,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        )
        Text(
            stringResource(Res.string.tray_off_board),
            color = appColors.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 8.dp),
        )
        RemovedPile(player, state.removedCheckersOf(player))
    }
}

/** Draws [count] removed checkers as a physical stack of discs seen from the side. */
@Composable
private fun RemovedPile(player: Player, count: Int) {
    val discHeight = 8.dp
    val step = 3.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(width = 36.dp, height = discHeight + step * (GameState.STARTING_STACK - 1)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (count == 0) {
                Box(Modifier.size(width = 36.dp, height = discHeight).border(1.dp, appColors.outline, DiscShape))
            }
            repeat(count) { i ->
                Box(
                    Modifier
                        .offset(y = -step * i)
                        .size(width = 36.dp, height = discHeight)
                        .background(player.color, DiscShape)
                        .border(1.dp, appColors.outline, DiscShape),
                )
            }
        }
        Text(
            "$count",
            color = appColors.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp).widthIn(min = 20.dp),
        )
    }
}

private val DiscShape = RoundedCornerShape(50)

@Composable
private fun Board(
    state: GameState,
    flipped: Boolean,
    flight: Flight?,
    selected: Square?,
    movable: Set<Square>,
    targets: Map<Square, Move>,
    lastMove: Move?,
    onSquareClick: (Square) -> Unit,
    hintedSquare: Square? = null,
    hintAlpha: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val colors = boardColors
    BoxWithConstraints(modifier.aspectRatio(1f).border(3.dp, colors.border)) {
        val cell = maxWidth / BOARD_SIZE
        Column {
            for (screenRow in 0 until BOARD_SIZE) {
                Row {
                    for (screenCol in 0 until BOARD_SIZE) {
                        val square = squareAt(screenRow, screenCol, flipped)
                        val target = targets[square]
                        val highlighted = square == lastMove?.from || square == lastMove?.to
                        Box(
                            modifier = Modifier
                                .size(cell)
                                .background(if (square.isDark) colors.darkSquare else colors.lightSquare)
                                .then(if (highlighted) lastMoveMark(colors) else Modifier)
                                .then(if (square.isDark) Modifier.clickable { onSquareClick(square) } else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            // While checkers are flying in, their landing square still shows what was there.
                            val landing = flight?.takeIf { square == it.animation.move.to }
                            val shown = if (landing != null) landing.animation.before.board[square] else state.board[square]
                            shown?.let { stack ->
                                // A captured stack fades out as the capturing checkers arrive.
                                val fade = if (landing?.animation?.isCapture == true) 1f - landing.progress else 1f
                                Checker(
                                    stack = stack,
                                    isSelected = square == selected,
                                    isMovable = square in movable,
                                    modifier = Modifier.fillMaxSize(0.8f).alpha(fade),
                                )
                            }
                            if (target != null) {
                                val capture = state.kindOf(target) is MoveKind.Capture
                                val color = if (capture) colors.capture else colors.target
                                Box(
                                    Modifier
                                        .fillMaxSize(if (state.board[square] == null) 0.45f else 0.9f)
                                        .then(targetRing(color, dashed = capture && colors.strongMarks))
                                        .background(color.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("${target.count}", color = colors.targetText, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (square == hintedSquare) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(colors.hint.copy(alpha = 0.45f * hintAlpha))
                                        .border(4.dp, colors.hint.copy(alpha = hintAlpha)),
                                )
                            }
                        }
                    }
                }
            }
        }
        flight?.let { FlyingChecker(it, flipped, cell) }
    }
}

/** The last move's squares: tinted, or outlined on boards that don't rely on colour alone. */
private fun lastMoveMark(colors: BoardColors): Modifier =
    if (colors.strongMarks) Modifier.border(3.dp, colors.lastMove) else Modifier.background(colors.lastMove)

/** The ring around a target square; captures get a thick dashed ring on high-contrast boards. */
private fun targetRing(color: Color, dashed: Boolean): Modifier =
    if (!dashed) {
        Modifier.border(3.dp, color, CircleShape)
    } else {
        Modifier.drawBehind {
            val width = 5.dp.toPx()
            drawCircle(
                color = color,
                radius = size.minDimension / 2 - width / 2,
                style = Stroke(width = width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
            )
        }
    }

/** The moving checkers, part way between their start and landing squares. */
@Composable
private fun FlyingChecker(flight: Flight, flipped: Boolean, cell: Dp) {
    val move = flight.animation.move
    val p = flight.progress
    val (fromRow, fromCol) = screenPosition(move.from, flipped)
    val (toRow, toCol) = screenPosition(move.to, flipped)
    val x = cell * (fromCol + (toCol - fromCol) * p)
    val y = cell * (fromRow + (toRow - fromRow) * p)
    // Checkers leaving the board fade out on their way off it.
    val fade = if (move.to.isOnBoard) 1f else 1f - p
    Box(Modifier.offset(x, y).size(cell).alpha(fade), contentAlignment = Alignment.Center) {
        Checker(flight.animation.mover, isSelected = false, isMovable = false, modifier = Modifier.fillMaxSize(0.8f))
    }
}

/** Where [square] is drawn, as (row, column) counted from the top left; works off the board too. */
private fun screenPosition(square: Square, flipped: Boolean): Pair<Int, Int> =
    if (flipped) {
        square.row to BOARD_SIZE - 1 - square.col
    } else {
        BOARD_SIZE - 1 - square.row to square.col
    }

@Composable
private fun Checker(stack: Stack, isSelected: Boolean, isMovable: Boolean, modifier: Modifier = Modifier) {
    val colors = boardColors
    val ring = when {
        isSelected -> colors.selectedRing
        isMovable -> colors.movableRing
        else -> colors.checkerOutline
    }
    Box(
        modifier
            .background(stack.owner.color, CircleShape)
            .border(if (isSelected) colors.checkerOutlineWidth + 2.dp else colors.checkerOutlineWidth, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${stack.size}",
            color = if (stack.owner == Player.White) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

private val Player.color: Color get() = if (this == Player.White) WhiteChecker else BlackChecker

/**
 * The square drawn at [screenRow], [screenCol] (counted from the top left). Normally row 8 (Black's
 * home row) is at the top, like a printed diagram; [flipped] turns the board round for Black.
 */
private fun squareAt(screenRow: Int, screenCol: Int, flipped: Boolean): Square =
    if (flipped) {
        Square(screenRow, BOARD_SIZE - 1 - screenCol)
    } else {
        Square(BOARD_SIZE - 1 - screenRow, screenCol)
    }

/** Arrow for a direction as seen on screen, which is turned round when the board is [flipped]. */
private fun arrow(direction: Direction, flipped: Boolean): String {
    if (!flipped) return arrow(direction)
    return arrow(Direction.entries.first { it.dRow == -direction.dRow && it.dCol == -direction.dCol })
}

/** Arrow for a direction as seen on screen, with White at the bottom. */
private fun arrow(direction: Direction): String = when (direction) {
    Direction.North -> "↑"
    Direction.NorthEast -> "↗"
    Direction.NorthWest -> "↖"
    Direction.South -> "↓"
    Direction.SouthEast -> "↘"
    Direction.SouthWest -> "↙"
    Direction.East -> "→"
    Direction.West -> "←"
}
