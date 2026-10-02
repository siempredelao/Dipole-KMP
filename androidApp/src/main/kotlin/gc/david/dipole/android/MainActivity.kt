package gc.david.dipole.android

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import gc.david.dipole.ui.DipoleApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Android 12+ animates the splash icon: keep it up until all three arrows have appeared.
        // Not on a recreation (e.g. rotation), when there is no splash to wait for.
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val shownAt = SystemClock.uptimeMillis()
            splashScreen.setKeepOnScreenCondition { SystemClock.uptimeMillis() - shownAt < SPLASH_ANIMATION_MILLIS }
        }

        // No custom exit animation: Android 12+ draws the splash icon on its own surface, which
        // ignored our fade and vanished before the background, making the splash blink. The
        // system's own exit fades icon and background together.
        setContent { DipoleApp() }
    }

    private companion object {

        /** Length of drawable-v31/splash_icon_animated.xml. */
        const val SPLASH_ANIMATION_MILLIS = 1_350L
    }
}
