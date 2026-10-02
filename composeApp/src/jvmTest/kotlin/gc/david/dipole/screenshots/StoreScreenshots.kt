package gc.david.dipole.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.celebration.ConfettiLauncher
import gc.david.dipole.tutorial.TutorialPage
import gc.david.dipole.ui.ConfettiOverlay
import gc.david.dipole.ui.DipoleTheme
import gc.david.dipole.ui.GameScreen
import gc.david.dipole.ui.GameUiState
import gc.david.dipole.ui.GameUiStateMapper
import gc.david.dipole.ui.RulesScreen
import gc.david.dipole.ui.SettingsUiState
import gc.david.dipole.ui.TutorialScreen
import gc.david.dipole.ui.TutorialUiState
import gc.david.dipole.ui.TutorialViewModel
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.random.Random
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Renders the Google Play screenshots from the real screens, in every language the app has, for
 * phones and 7- and 10-inch tablets. Run it with `./gradlew :composeApp:storeScreenshots`; the PNGs
 * land in `composeApp/build/store-screenshots/<language>/<device>/`, named like
 * `es-ES-phone-01-game.png`.
 *
 * Text uses this computer's default font rather than Android's Roboto.
 */
fun main(args: Array<String>) {
    val outDir = File(args.firstOrNull() ?: "build/store-screenshots")
    val shots = shots(hint = StoreScreenshotStates.hint())
    for ((language, locale) in languages) {
        // Compose resources pick the strings for the default locale.
        Locale.setDefault(Locale.forLanguageTag(locale))
        for (device in Device.entries) {
            for (shot in shots) {
                // Play rejects two uploads with the same file name, so each name carries its language and device.
                val file = outDir.resolve("$language/${device.folder}/$language-${device.folder}-${shot.name}.png")
                file.parentFile.mkdirs()
                ImageIO.write(render(device, shot.content), "png", file)
            }
        }
        println("$language done")
    }
    println("Screenshots written to ${outDir.absolutePath}")
}

/** Play Console language codes, each with the locale the app's strings are looked up for. */
private val languages = listOf(
    "en-US" to "en-US",
    "es-ES" to "es-ES",
    "fr-FR" to "fr-FR",
    "it-IT" to "it-IT",
    "pt-BR" to "pt-BR",
    "de-DE" to "de-DE",
    "pl-PL" to "pl-PL",
    "tr-TR" to "tr-TR",
    "da-DK" to "da-DK",
    "no-NO" to "nb-NO",
    "sv-SE" to "sv-SE",
)

/** Play's screenshot slots, all 9:16 portrait so the listing can be featured. */
private enum class Device(val folder: String, val width: Int, val height: Int, val density: Float) {
    /** 411 × 731 dp, a typical phone. */
    Phone("phone", 1080, 1920, 2.625f),

    /** 600 × 1067 dp. */
    SevenInchTablet("tablet-7-inch", 1080, 1920, 1.8f),

    /** 800 × 1422 dp. */
    TenInchTablet("tablet-10-inch", 1440, 2560, 1.8f),
}

private class Shot(val name: String, val content: @Composable () -> Unit)

private fun shots(hint: GameUiState): List<Shot> = listOf(
    Shot("01-game") { Game(StoreScreenshotStates.game, AppearanceMode.Light, BoardTheme.Wood) },
    Shot("02-hint") { Game(hint, AppearanceMode.Dark, BoardTheme.Wood) },
    Shot("03-new-game") { Game(StoreScreenshotStates.newGame, AppearanceMode.Dark, BoardTheme.Tournament) },
    Shot("04-win") {
        Game(StoreScreenshotStates.won, AppearanceMode.Light, BoardTheme.Marble) {
            ConfettiOverlay(
                celebration = 1,
                celebrating = false,
                onStart = {},
                launcher = ConfettiLauncher(Random(CONFETTI_SEED)),
                previewSeconds = CONFETTI_SECONDS,
            )
        }
    },
    Shot("05-tutorial") {
        DipoleTheme(AppearanceMode.Light) {
            TutorialScreen(
                TutorialUiState(TutorialPage.Captures, captureTargets = TutorialViewModel.captureTargets()),
                onAction = {},
            )
        }
    },
    Shot("06-rules") { DipoleTheme(AppearanceMode.Dark) { RulesScreen(onBack = {}, onShowTutorial = {}) } },
    Shot("07-themes") { Game(StoreScreenshotStates.appearance, AppearanceMode.Dark, BoardTheme.Slate) },
)

private const val CONFETTI_SEED = 7
private const val CONFETTI_SECONDS = 0.9f

/** The game screen for [uiState] in the given look, with [overlay] drawn on top. */
@Composable
private fun Game(
    uiState: GameUiState,
    mode: AppearanceMode,
    board: BoardTheme,
    overlay: @Composable () -> Unit = {},
) {
    DipoleTheme(mode, board) {
        Box(Modifier.fillMaxSize()) {
            GameScreen(
                GameUiStateMapper.map(uiState),
                SettingsUiState(appearanceMode = mode, boardTheme = board),
                onAction = {},
                onSettingsAction = {},
                onRulesClick = {},
            )
            overlay()
        }
    }
}

/**
 * Draws [content] on [device] and returns it without an alpha channel, as Play asks. Inspection mode
 * holds the blinking hint and pulsing highlights at full strength, as in the previews.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun render(device: Device, content: @Composable () -> Unit): BufferedImage {
    val scene = ImageComposeScene(
        width = device.width,
        height = device.height,
        density = Density(device.density),
        content = { CompositionLocalProvider(LocalInspectionMode provides true, content = content) },
    )
    try {
        // A few frames let dialogs and effects settle.
        var image = scene.render(0)
        for (frame in 1..SETTLE_FRAMES) image = scene.render(frame * FRAME_NANOS)
        return image.toOpaqueImage()
    } finally {
        scene.close()
    }
}

private const val SETTLE_FRAMES = 6
private const val FRAME_NANOS = 250_000_000L

private fun Image.toOpaqueImage(): BufferedImage {
    val png = encodeToData(EncodedImageFormat.PNG) ?: error("Couldn't encode the screenshot")
    val withAlpha = ImageIO.read(ByteArrayInputStream(png.bytes))
    return BufferedImage(withAlpha.width, withAlpha.height, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().run {
            drawImage(withAlpha, 0, 0, null)
            dispose()
        }
    }
}
