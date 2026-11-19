package com.cojocari.alphabet

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import com.cojocari.alphabet.audio.Sfx
import com.cojocari.alphabet.billing.PlayBilling
import com.cojocari.alphabet.data.Progress
import com.cojocari.alphabet.platform.AndroidPlatform
import com.cojocari.alphabet.story.StoryProgress

/** Android entry point: sets up storage, Google Play billing and sounds, then shows [CleverOwlApp]. */
class MainActivity : ComponentActivity() {
    private lateinit var progress: Progress
    private lateinit var billing: PlayBilling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidPlatform.context = applicationContext
        // Tablets can be held either way; phones stay upright so everything stays big.
        requestedOrientation =
            if (resources.configuration.smallestScreenWidthDp >= 600) ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        progress = Progress()
        val story = StoryProgress()
        billing = PlayBilling(applicationContext) { progress.updatePremium(true) }
        billing.activity = this
        billing.connect()
        val sfx = Sfx { progress.soundEffects }

        setContent {
            CleverOwlApp(
                progress = progress,
                story = story,
                billing = billing,
                sfx = sfx,
                isVisible = { lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) },
                // Debug builds can open any screen directly: adb shell am start ... --es route story:30 --ei step 5
                startRoute = if (BuildConfig.DEBUG) intent.getStringExtra("route") ?: "home" else "home",
                startStep = if (BuildConfig.DEBUG) intent.getIntExtra("step", 0) else 0,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        progress.onAppOpen() // new day → streak, daily gift, play-time counter
    }

    override fun onDestroy() {
        billing.activity = null
        billing.disconnect()
        super.onDestroy()
    }
}
