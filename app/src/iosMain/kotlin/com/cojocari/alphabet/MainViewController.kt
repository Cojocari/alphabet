@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet

import androidx.compose.ui.window.ComposeUIViewController
import com.cojocari.alphabet.audio.Sfx
import com.cojocari.alphabet.billing.AppStoreBilling
import com.cojocari.alphabet.data.Progress
import com.cojocari.alphabet.story.StoryProgress
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUserDefaults
import com.cojocari.alphabet.platform.isDebugBuild
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIViewController

/**
 * iOS entry point, called from iosApp/iosApp/iOSApp.swift: sets up sound, storage and the
 * App Store purchase, then shows [CleverOwlApp].
 */
fun MainViewController(): UIViewController {
    // Voice and sounds play even with the silent switch on: a child can't read the instructions.
    AVAudioSession.sharedInstance().apply {
        setCategory(AVAudioSessionCategoryPlayback, null)
        setActive(true, null)
    }

    val progress = Progress()
    val story = StoryProgress()
    val billing = AppStoreBilling { progress.updatePremium(true) }.also { it.start() }
    val sfx = Sfx { progress.soundEffects }

    // New day → streak, daily gift, play-time counter (like onResume on Android).
    progress.onAppOpen()
    NSNotificationCenter.defaultCenter.addObserverForName(
        UIApplicationDidBecomeActiveNotification, null, NSOperationQueue.mainQueue,
    ) { _ -> progress.onAppOpen() }

    // Launch arguments like "-route home" show up as user defaults.
    val launchArgs = NSUserDefaults.standardUserDefaults
    return ComposeUIViewController {
        CleverOwlApp(
            progress = progress,
            story = story,
            billing = billing,
            sfx = sfx,
            isVisible = { UIApplication.sharedApplication.applicationState == UIApplicationState.UIApplicationStateActive },
            // Debug builds can open any screen directly: xcrun simctl launch <device> com.cojocari.alphabet -route story:30 -step 5
            startRoute = if (isDebugBuild) launchArgs.stringForKey("route") ?: "home" else "home",
            startStep = if (isDebugBuild) launchArgs.integerForKey("step").toInt() else 0,
        )
    }
}
