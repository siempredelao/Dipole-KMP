package gc.david.dipole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.back
import gc.david.dipole.resources.ic_arrow_back
import gc.david.dipole.resources.rules
import gc.david.dipole.resources.rules_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The rules of the game, opened from the info icon next to the title. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(painterResource(Res.drawable.ic_arrow_back), stringResource(Res.string.back), tint = Color.White)
            }
            Text(
                stringResource(Res.string.rules_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            stringResource(Res.string.rules),
            color = Color.LightGray,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        )
    }
}
