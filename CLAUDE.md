# Clever Owl / Bufnița Isteață: notes for Claude

Android learning game for children aged 2–6, Romanian (main market) and English. Formerly
"Alphabet Fun". Package `com.cojocari.alphabet` (keep it: the Play listing will use it).
Kotlin, Jetpack Compose, Material 3, Google Play Billing 8. See `README.md` for the feature list.

## Build and run
- JDK 17 and the Android SDK (platform 36). In Git Bash on Windows:
  `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.21.9-hotspot"` (adjust per machine),
  then `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, `./gradlew bundleRelease`.
- `gradlew.bat` from PowerShell has been unreliable; use `./gradlew` in bash.
- No Python on the original machine (use Perl, which ships with Git Bash, or PowerShell);
  `gh` CLI not installed.
- Test device: Xiaomi Redmi Pad Pro over adb (2560×1600, 320 dpi). Xiaomi asks the user to tap
  "Install" on the tablet: run `timeout 90 adb install -r app/build/outputs/apk/debug/app-debug.apk`.
  Don't send taps (`adb shell input`) while the user is playing on the tablet.
- `AnimalSynthTest` writes every synthesised sound to `app/build/animal-sounds/*.wav`.

## Release signing (never in git)
- `keystore.properties` (repo root) and the upload key `alphabet-upload.jks` are git-ignored.
  On a new machine copy both from the owner's backup; `storeFile` in `keystore.properties`
  must point to the `.jks`. Never commit, print or upload them.
- Play Console steps: `docs/release-checklist.md`. Store texts: `docs/store-listing.md`.
  Graphics: `branding/` (icon-512, feature graphics RO/EN, rendered from HTML with headless Edge).
- In-app product id: `unlock_all` (one-time).

## How the owner works
- The owner names versions (v1.0, v2.0, v2.1, v3.0, v3.1…). On a version: bump `versionCode`
  and `versionName` in `app/build.gradle.kts`, commit on `develop`, tag `vX.Y`, push branch + tag.
- Never move or force-push a published tag; propose the next number instead.
- Commit and push only when asked. Work happens on `develop`; `main` still has only the
  initial commit (merge when asked).
- The owner writes quickly in English (Romanian native). Romanian texts must be grammatical
  Romanian; speech is device text-to-speech (default English voice `en-us-x-iom`).

## Architecture in brief
- `MainActivity.kt`: string routes ("home", "world:X", "game:X", "stickers", "scene", "parent",
  "unlock"), parental gate, play-time limit, billing wiring.
- `ui/Worlds.kt`: `GameId` (24 games), `World` (7 worlds), `FreeGames`, `GameContent`
  (demo/locked logic via `GameAccess`, seeded random for demos).
- `i18n/Strings.kt`: every phrase in English and Romanian.
- Monetisation: 8 free games (Easy/Medium; Hard needs the unlock); other games are demos
  (`DemoRounds = 5`, or one full round). Animals: farm free, 4 wild animals open in the demo.
- Animals world: `data/Animals.kt` (28 animals), `ui/animals/AnimalsScreen.kt` (grid, big view,
  arrows, swipe, bottom strip), `audio/AnimalSoundPlayer.kt` (recordings in `res/raw/animal_*`,
  clip lengths, minimum 2 s, fade-out, synth fallback), `audio/AnimalSynth.kt` (snake hiss and
  fallback). Animals whose emoji the device can't draw are hidden (donkey 🫏 needs Android 14).
- Tracing: `ui/draw/DrawScreen.kt` + `ui/draw/StrokeOrder.kt` (numbered strokes with arrows and a
  moving guide dot; coordinates relative to each glyph's outline; diacritics placed on the
  mark's own outline).
- Sounds: credits in `docs/sound-credits.md` and `SoundCredits` in `ui/parent/Licenses.kt`;
  tools and rules in `tools/sounds/README.md`.

## State on 2026-11-16 and next steps
Latest tag: **v3.1** (new name + owl logo + licences screen). On `develop` since then, not tagged:
Animals world with real recorded sounds, stroke-order arrows for tracing, sheep plays 3 bleats,
2 s minimum sound length, turkey replaced by donkey (no free real turkey gobble exists),
dolphin replaced (old file was noise), Romanian "ham-ham" spoken as "ham, ham".

To do / to check with the owner:
1. Owner to listen to all animal sounds on the tablet and report bad ones (pig is a piglet
   squeal; tiger reuses the lion roar slowed down; snake is synthesised).
2. Check the tracing arrows on the tablet (letters a, g, 5, 8 may sit slightly off the stroke).
3. Confirm "Câinele face ham, ham" now sounds right; option: "hau, hau".
4. Ask whether to tag this as v3.2 (bump versionCode 6 / versionName 3.2).
5. Release: take store screenshots on the tablet, publish the privacy policy at a public URL,
   trademark check for both names on TMview (classes 9 and 41), then `docs/release-checklist.md`.
