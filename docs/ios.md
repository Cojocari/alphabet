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

## App Store (signed builds with Codemagic)

The signed app is built by Codemagic (`codemagic.yaml`, workflow **iOS to App Store Connect**) on the same team
account as Listhive, reusing its App Store Connect API key (`shoplist_asc`) and its Apple Distribution certificate
(team COJOCARI SOFT SRL). Each build gets the build number `<versionCode>.<Codemagic build>`, e.g. 11.3.
`iosApp/iosApp/PrivacyInfo.xcprivacy` is Apple's privacy manifest (no tracking, no data collected).

One-time setup (about 20 minutes):
1. developer.apple.com → **Identifiers → +** → App IDs → App: description `Hupi the Owl`, explicit bundle ID
   `com.cojocari.alphabet`. In-App Purchase is on by default; nothing else to tick.
2. App Store Connect → **Apps → + → New App**: iOS, name **Hupi the Owl: ABC & 123 Kids**, primary language
   English (U.S.), bundle ID `com.cojocari.alphabet`, SKU `hupi-001`, full access.
3. developer.apple.com → **Profiles → +** → **App Store Connect** → App ID `com.cojocari.alphabet` → the Apple
   Distribution certificate Listhive uses → name `Hupi App Store` → Generate.
4. codemagic.io → **Settings → codemagic.yaml settings → Code signing identities → iOS provisioning profiles →
   Fetch profiles** → tick `Hupi App Store` → reference name `hupi_app_store` → Download selected.
5. codemagic.io → **Add application** → GitHub → `Cojocari/alphabet` → configuration from `codemagic.yaml`.
6. **Start new build** → branch `develop` → **iOS to App Store Connect**. About 30 minutes; the first build is
   slower (it downloads the Kotlin/Native toolchain, cached afterwards). The build appears in App Store Connect →
   TestFlight after Apple processes it; add yourself under Internal Testing and install it with TestFlight.

Everything to fill in App Store Connect (agreements, listing in EN and RO, age rating, privacy, the in-app
purchase `unlock_all`, review notes, China mainland unticked): **`docs/app-store-listing.md`**.

Screenshots: the workflow **App Store screenshots** (`.github/workflows/appstore-shots.yml`, runs by hand or when
the file changes) takes them raw on an iPhone 6.9″ (1320×2868, Apple's required iPhone size) and an iPad 13″
(2064×2752) simulator with demo progress (`-demo YES`) and pushes them to the `ci-appstore-shots` branch;
`sh branding/frame-appstore.sh` adds captions and writes `branding/screenshots/appstore/{en,ro}/`. Check the
result: if the app crashes at launch, every shot shows the simulator's home screen (it happened once, when the
demo progress asked a chapter's show for lessons; `DemoProgressTest` guards it now).
