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
- Kotlin Multiplatform (branch `ios`, see `docs/ios.md`): the game is in `app/src/commonMain`, shared by Android
  (`app/src/androidMain`: `MainActivity`, res/, Play Billing, TTS) and iOS (`app/src/iosMain` + `iosApp/`, built by
  GitHub Actions on a Mac). What each platform provides: `platform/Platform.kt`, plus the `Speaker`,
  `AnimalSoundPlayer`, `Billing` and `GlyphFont` interfaces. `compileCommonMainKotlinMetadata` catches Android-only
  APIs in shared code. Paths below are under `commonMain` unless they say otherwise.
- `App.kt` (`CleverOwlApp`): string routes ("home", "world:X", "game:X", "stickers", "scene", "parent",
  "unlock"), parental gate, play-time limit. `MainActivity` / `MainViewController` only wire up the platform.
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

## Hupi's Adventure (story mode), added 2026-11-16
- Package `story/`: `Curriculum.kt` (7 chapters × 9 scenes: 8 lessons and a show; what each lesson teaches per level, scene steps; `Treasures`),
  `Quests.kt` (questions per item), `ScenePlayer.kt` (plays steps: narrate, hatch, meet, ask together/alone,
  teach Hupi, catch, trace, show, reward, diploma), `Hupi.kt` (owl drawn in code, grows with 7 items),
  `StoryMapScreen.kt` (map + age picker), `StoryHouseScreen.kt` (shelves), `StoryProgress.kt` (progress and the
  learning tracker: learned = right on the first try on 3 different days; due items come back for review).
- Texts: `i18n/StoryStrings.kt` (EN + RO). Chapters 1–2 free (`FreeScenes`), the rest need the unlock.
- World paths ("Learn with Hupi" banner in each world except Animals): `Paths` in `Curriculum.kt`, `PathMapScreen.kt`,
  routes "path:Letters" and "path:Letters/15". Same scenes as the chapter, without the magpie plot and without Hupi
  growing up; scenes done in the adventure count in the path
  (`StoryProgress.chapterDone`), but a path doesn't move the adventure on; the learning tracker is shared. Free: chapters 1–2, plus the first lesson of
  every other path.
- Parent chooses the child's age (map banner behind the gate, or grown-ups' corner) → sets the level; the level
  can still be changed at any time from the top bar. No automatic pause (the parent decides when to stop).
- Tracing (`ui/draw/TracePad.kt`) checks strokes: Easy wide/65%, Medium 80%, Hard narrow/85% in order and direction.
- Debug-only test helpers: `adb shell am start -n com.cojocari.alphabet/.MainActivity --es route story:30 --ei step 5`
  opens any screen/scene step; logcat tag `StoryTest` prints each question's right answer.
- In a scene, tapping Hupi (or the 🔊 badge on him) repeats the current step's instruction (`ctx.setPrompt` in each
  step; `Speaker.awaitQuiet` keeps narration steps from moving on while it repeats).
- Store screenshots: `branding/screenshots/{ro,en}/{phone,tablet}-1..8.jpg`. Made on the tablet with demo progress
  written to `shared_prefs` via `adb exec-in run-as` (back up the real files first, restore after), phone size via
  `adb shell wm size 1080x2400` + `wm density 420` (always `wm size reset` / `wm density reset` after), then framed
  with captions in HTML and rendered by headless Edge. `ui/components/FitText.kt` shrinks titles that don't fit on phones.

## State on 2026-11-16 and next steps
Latest tag: **v4.0** (versionCode 6); version 4.1 (versionCode 7) adds tap-Hupi-to-repeat, phone title fit and
the store screenshots. Earlier in v4.0: Animals world, real sounds, stroke arrows, the tracing fix by level,
Hupi's Adventure (story mode, tested on the tablet: map, hatch, all step types, show, growing up, diploma,
warm-up, house, age picker) and a "Learn with Hupi" story path in every world except Animals.

## 2026-11-22, branch `ios` (not yet merged into `develop`, not tested on the tablet)
- iOS port (Kotlin Multiplatform, see `docs/ios.md`). GitHub Actions builds it, takes simulator screenshots
  (`ci-screenshots` branch) and an unsigned Release `.ipa` for Sideloadly (`ci-ipa` branch). Launch arguments
  `-route story:30 -step 5` open any screen in debug builds, like the adb helpers.
- Romanian alphabet teaches only the 26 base letters (Ă, Â, Î, Ș, Ț removed from `RomanianAlphabet`; words keep
  them; `RomanianDiacriticNames` names them when spelling; old saved letters ignored via `belongsTo`).
- Story: Meet no longer says Hupi's wrong guess and moves on after 3 s; next question 1.5 s after a right answer;
  the story waits at most 1.8 s for an animal sound; Easy: hint after 1 mistake, and one Hupi game per lesson
  (Catch on even scenes, Teach on odd); `hupiCheers` after one own right answer in three.
- Tracing game (`DrawScreen`): two rounds per letter; round 2 is `TracePad(dotted = true)` (no outline, dots with
  arrowheads). Story trace step unchanged.
- Planning board: Trello "Clever Owl / Bufnița Isteață" (private, the owner's Momentum workspace).

## 2026-11-24, branch `ios`: recorded voice and phone layout
- Recorded voice (`tools/voice/README.md`): every sentence pre-recorded with Azure AI Speech (free F0 tier) as
  `app/src/androidMain/assets/voice/<lang>/<hash>.mp3`; EN `en-US-AnaNeural`, RO `ro-RO-Ioana:MAI-Voice-2.1`.
  `audio/RecordedSpeaker.kt` plays them sentence by sentence, device TTS for anything missing (debug logcat
  `VoiceMiss`). `VoiceCatalogTest` lists all sentences (`tools/voice/catalog-*.tsv`); `tools/voice/generate.sh`
  records the missing ones. Key file `~/azure-speech.txt`, outside the repo, never committed.
- Voice fixes (2026-11-24/25): `generate.sh` reads lone English capitals as letters (`say-as`), spells the Romanian
  letter "ce" phonetically, records sentences in `tools/voice/slow.txt` 20% slower, then runs `tools/voice/trim.sh`
  (cuts the voices' silent tails). The app pauses 120 ms between sentences, 450 ms after a question. Ioana (MAI) gives
  a different take each time: to fix one clip, delete it and run `generate.sh ro` again, or record a few takes and
  let the owner choose. Azure speech-to-text (same key, `transcriptions:transcribe`, accepts MP3) helps find bad takes.
- Phones: an iPhone 17 Pro is 402 dp wide. `isCompactWidth()` (< 480 dp) gives smaller top-bar buttons, `TopBar` is a
  `FlowRow` (wraps instead of overflowing), `ScaleToFit` shrinks wide content (sums, age pickers), the counting field
  sizes its things to the width. Check new screens on the emulator with `wm size 1206x2622` + `wm density 480`.

## 2026-11-24, branch `ios`: longer story
- Chapters have 8 lessons + the show (`ScenesPerChapter = 9`, 63 scenes). Saved progress from the old 7-scene
  chapters moves to the same lesson (`StoryProgress.migrate`, key `perChapter`).
- Every level learns all 10 colours (chapter 1; Medium/Hard also the 6 shapes) and all 26 letters (chapters 3–4,
  16 lessons). Writing alternates numbers and letters. `CurriculumTest` checks the coverage.
- Each lesson ends with a treasure chest (tap to open, `TreasureChest` in `story/StoryArt.kt`); the 56 treasures
  (`Treasures`) fill a shelf in Hupi's house and mark finished stops on the map. Owned = scene done, no new storage.
- Once a chapter (lesson 4, `MagpieLesson`) the magpie (`Magpie`, drawn in code) flies around with something shiny:
  tap her 2–4 times (by level) and she drops it. Not in world paths. She also appears in her narrations.
- Friends' requests vary by lesson (`friendAsk(chapter, lesson, friend)`, four per chapter); the voice catalogue
  takes them from the real lessons. The show's reward: the chapter's friends dance and confetti repeats.

To do / to check with the owner:
1. Owner to play the story with a child and report pace, humour and difficulty.
2. Owner to listen to all animal sounds and check the tracing arrows on letters like a, g, 5, 8.
3. Owner to listen to the recorded voice (Ana / Ioana) in both languages.
4. Release: store screenshots (now with the story), privacy policy URL, TMview trademark check, then
   `docs/release-checklist.md`.
