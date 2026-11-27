# iOS version

The game is one Kotlin Multiplatform project. The same Compose code runs on Android and on
iPhone/iPad (Compose Multiplatform). Only a thin layer is written per platform.

## Where things are

| Path | What |
|---|---|
| `app/src/commonMain` | The whole game, shared: screens, story, games, texts, progress. |
| `app/src/androidMain` | Android: `MainActivity`, text-to-speech, MediaPlayer sounds, Google Play Billing, resources (`res/`). |
| `app/src/iosMain` | iOS: `MainViewController` (entry point), Apple voices (`IosSpeaker`), sounds (`IosAnimalSoundPlayer`), App Store purchase (`AppStoreBilling`), letter outlines for tracing. |
| `app/src/commonMain/.../platform/Platform.kt` | The list of what each platform must provide (storage, date, sounds, saving drawings…). |
| `iosApp/` | The Xcode app: `project.yml` (XcodeGen), `iosApp/iOSApp.swift`, app icon, Romanian/English app name. |
| `iosApp/scripts/prepare-resources.sh` | Converts the animal recordings to `.m4a` for iOS and copies the licence text. |
| `.github/workflows/ios.yml` | Builds the iOS app on a GitHub-hosted Mac at every push to the `ios` branch. |

## Building

- **Android:** unchanged: `./gradlew assembleDebug`, `testDebugUnitTest`, `bundleRelease` (Windows is fine).
- **iOS:** needs macOS and Xcode, so GitHub Actions does it. Push to `ios` (or *Actions → iOS → Run workflow*).
  On a Mac it would be: `sh iosApp/scripts/prepare-resources.sh && cd iosApp && xcodegen generate && open iosApp.xcodeproj`.
- `./gradlew :app:compileCommonMainKotlinMetadata` checks on Windows that the shared code uses no Android-only API.

## Differences on iOS

- Voice: Apple's built-in voices (Romanian: Ioana). Parents can download better "Enhanced" voices in
  Settings → Accessibility → Spoken Content → Voices; the app prefers them.
- Sound and voice play even with the silent switch on.
- No back button: every screen has its own back arrow.
- "Rate the app" shows Apple's rating sheet.
- Drawings are saved to Photos (iOS asks once for permission).
- The purchase is the App Store product `unlock_all` (non-consumable), with a Restore button.

## Next steps for the App Store

1. Apple Developer Program in the company's name (D-U-N-S number), $99/year.
2. In App Store Connect: create the app (bundle ID `com.cojocari.alphabet`), the in-app purchase `unlock_all`,
   (non-consumable; turn on **Family Sharing** for it: the purchase screen on iPhone promises it),
   and an App Store Connect API key (Users and Access → Integrations → Keys, role App Manager).
3. Add the key to GitHub as encrypted secrets (never in the code); then the workflow can sign the app and upload
   it to TestFlight, so it can be installed on an iPhone with the TestFlight app.
4. Kids category: age band 5 and under / 6–8, privacy "Data Not Collected", screenshots from the simulator.
