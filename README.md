# Clever Owl / Bufnița Isteață 🦉

An Android learning game for children aged 2 to 6, in **Romanian and English**.
Everything is big, colourful and spoken aloud, so children who can't read yet can play on their own.

## Seven worlds, 24 games

| World | Games |
|---|---|
| 🔤 Letters | Cards · Balloons · Find it · Memory · First letter |
| 🔢 Numbers | Cards 1–20 · How many? · Find the number · Number balloons |
| 📖 Reading | Read it · Which word? |
| ✏️ Writing | Trace ABC · Trace abc · Trace 0–10 · Spell it |
| ➕ Math | Add · Take away · More or less |
| 🎨 Colours & Shapes | Free draw · Colours · Shapes |
| 🐾 Animals | Farm & pets · Wild · All: 28 animals with real sounds |

Every game (except cards and free drawing) has **Easy / Medium / Hard** levels, and rounds are
generated from the chosen level. The Romanian version teaches the 26 base letters (Ă, Â, Î, Ș, Ț appear only inside words).

**Rewards:** stars, a 40-sticker album, sticker scenes, a daily gift, a day streak and an owl mascot.
**Grown-ups' corner** (behind a parental gate): unlock, voice, sound, child's name, daily play-time
limit, progress, and contact the developer.

## Monetisation
One-time in-app purchase `unlock_all` (Google Play Billing). 8 games are free at Easy and Medium (the farm animals have no levels);
the other 14 can be played as a short demo with fixed rounds; Hard is part of the unlock.
No ads, no subscriptions, no data collection.

## Building

Requirements: JDK 17 and the Android SDK (platform 36; Gradle downloads it if missing).

```bash
./gradlew installDebug    # debug build on a connected device
./gradlew bundleRelease   # signed bundle for Google Play: app/build/outputs/bundle/release/app-release.aab
```

Release signing reads `keystore.properties` (git-ignored). Debug builds have a
"Developer: unlock without paying" switch in the grown-ups' corner.

## Docs
- [Release checklist](docs/release-checklist.md): Play Console step by step
- [Store listing](docs/store-listing.md): titles, descriptions and graphics, in RO and EN
- [Privacy policy](https://cojocarisoft.com/cleverowl/privacy.html) (source in the company website folder, see `docs/privacy-policy.md`)
- [Romanian voice recording script](docs/recording-script-ro.md)
- [Animal sound credits](docs/sound-credits.md)
- [branding/](branding/): logo (`logo.svg`), Play Store icon and feature graphics. The owl is drawn in
  `owl.svgpart`; the launcher icon (`res/drawable/ic_launcher_foreground.xml`) uses the same shapes.
  The PNGs are rendered from the `.html` files with a headless browser. Text uses the Nunito font
  (SIL Open Font License, Google Fonts).

## Tech
Kotlin, Jetpack Compose, Material 3, Google Play Billing. Speech uses the device's text-to-speech
engine. Sound effects are synthesised in code. Works offline.
