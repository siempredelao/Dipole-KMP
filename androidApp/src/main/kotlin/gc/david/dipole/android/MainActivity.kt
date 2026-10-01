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
        splashScreen.setOnExitAnimationListener { splash ->
            splash.view.animate()
                .alpha(0f)
                .setDuration(SPLASH_FADE_MILLIS)
                .withEndAction { splash.remove() }
                .start()
        }
        setContent { DipoleApp() }
    }

    private companion object {
        /** Length of drawable-v31/splash_icon_animated.xml. */
        const val SPLASH_ANIMATION_MILLIS = 1_300L
        const val SPLASH_FADE_MILLIS = 200L
    }
}
