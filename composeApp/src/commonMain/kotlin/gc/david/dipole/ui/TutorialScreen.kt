package gc.david.dipole.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gc.david.dipole.game.Square
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.back
import gc.david.dipole.resources.bear_off_button
import gc.david.dipole.resources.tutorial_captures
import gc.david.dipole.resources.tutorial_captures_title
import gc.david.dipole.resources.tutorial_goal
import gc.david.dipole.resources.tutorial_goal_title
import gc.david.dipole.resources.tutorial_moving
import gc.david.dipole.resources.tutorial_moving_title
import gc.david.dipole.resources.tutorial_next
import gc.david.dipole.resources.tutorial_off_board
import gc.david.dipole.resources.tutorial_off_board_title
import gc.david.dipole.resources.tutorial_page
import gc.david.dipole.resources.tutorial_plain
import gc.david.dipole.resources.tutorial_plain_title
import gc.david.dipole.resources.tutorial_sitting_out
import gc.david.dipole.resources.tutorial_sitting_out_title
import gc.david.dipole.resources.tutorial_skip
import gc.david.dipole.resources.tutorial_start
import gc.david.dipole.resources.tutorial_tips
import gc.david.dipole.resources.tutorial_tips_title
import gc.david.dipole.resources.tutorial_title
import gc.david.dipole.tutorial.TutorialPage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The tutorial wizard, showing the page in [uiState]. Swiping or Back and Next move between pages;
 * Skip and Start playing close it.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TutorialScreen(uiState: TutorialUiState, onAction: (TutorialAction) -> Unit) {
    val page = uiState.page
    val pages = TutorialPage.entries
    val isFirst = page.ordinal == 0
    val isLast = page.ordinal == pages.lastIndex
    BackHandler {
        onAction(if (isFirst) TutorialAction.Closed else TutorialAction.BackClicked)
    }
    val pagerState = rememberPagerState(initialPage = page.ordinal) { pages.size }
    // The ViewModel owns the page: follow it when Back or Next change it...
    LaunchedEffect(page) {
        if (pagerState.currentPage != page.ordinal) pagerState.animateScrollToPage(page.ordinal)
    }
    // ...and tell it when the player swipes to another page.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { onAction(TutorialAction.PageShown(pages[it])) }
    }
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.tutorial_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            if (!isLast) {
                TextButton(onClick = { onAction(TutorialAction.Closed) }) {
                    Text(stringResource(Res.string.tutorial_skip))
                }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { index ->
            TutorialPageContent(pages[index], uiState.captureTargets[pages[index]].orEmpty())
        }
        PageDots(current = page.ordinal, count = pages.size)
        Row(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!isFirst) {
                OutlinedButton(onClick = { onAction(TutorialAction.BackClicked) }) {
                    Text(stringResource(Res.string.back))
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { onAction(TutorialAction.NextClicked) }) {
                Text(stringResource(if (isLast) Res.string.tutorial_start else Res.string.tutorial_next))
            }
        }
    }
}

/** One page: a still board diagram with the page's example moves, then its title and text. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorialPageContent(page: TutorialPage, captureTargets: Set<Square>) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Board(
            state = page.position,
            flipped = false,
            flight = null,
            selected = page.selected,
            movable = emptySet(),
            targets = page.examples.filter { it.to.isOnBoard }.associateBy { it.to },
            captureTargets = captureTargets,
            lastMove = null,
            onSquareClick = null,
            hintedSquare = page.hinted,
            hintAlpha = if (page.hinted != null) pulse() else 0f,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        )
        val offBoard = page.examples.filter { !it.to.isOnBoard }
        if (offBoard.isNotEmpty()) {
            // Look like the off-board buttons of the game screen, without being tappable.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                offBoard.forEach { move ->
                    Text(
                        stringResource(Res.string.bear_off_button, move.count, arrow(move.direction, flipped = false)),
                        color = colors.text,
                        modifier = Modifier
                            .border(1.dp, colors.outline, RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(page.title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Text(stringResource(page.body), fontSize = 16.sp, lineHeight = 24.sp, color = colors.secondaryText)
        }
    }
}

/** The hint highlight's strength, pulsing like the hint on the game screen (full in previews). */
@Composable
private fun pulse(): Float {
    if (LocalInspectionMode.current) return 1f
    val transition = rememberInfiniteTransition()
    val alpha = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
    )
    return alpha.value
}

private const val PULSE_MILLIS = 600

@Composable
private fun PageDots(current: Int, count: Int) {
    val colors = LocalAppColors.current
    val description = stringResource(Res.string.tutorial_page, current + 1, count)
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .align(Alignment.CenterVertically)
                    .background(if (index == current) colors.text else colors.outline, CircleShape),
            )
        }
    }
}

private val TutorialPage.title: StringResource
    get() = when (this) {
        TutorialPage.Goal -> Res.string.tutorial_goal_title
        TutorialPage.Moving -> Res.string.tutorial_moving_title
        TutorialPage.PlainMoves -> Res.string.tutorial_plain_title
        TutorialPage.Captures -> Res.string.tutorial_captures_title
        TutorialPage.OffBoard -> Res.string.tutorial_off_board_title
        TutorialPage.SittingOut -> Res.string.tutorial_sitting_out_title
        TutorialPage.Tips -> Res.string.tutorial_tips_title
    }

private val TutorialPage.body: StringResource
    get() = when (this) {
        TutorialPage.Goal -> Res.string.tutorial_goal
        TutorialPage.Moving -> Res.string.tutorial_moving
        TutorialPage.PlainMoves -> Res.string.tutorial_plain
        TutorialPage.Captures -> Res.string.tutorial_captures
        TutorialPage.OffBoard -> Res.string.tutorial_off_board
        TutorialPage.SittingOut -> Res.string.tutorial_sitting_out
        TutorialPage.Tips -> Res.string.tutorial_tips
    }
