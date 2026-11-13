# Alphabet Fun 🔤🖍️❓

A simple Android game that helps children aged 2 to 5 learn the letters of the alphabet.
Everything is big, colourful and spoken out loud, so a child who can't read yet can still play.

## Games

| | Game | What the child does |
|---|---|---|
| 🔤 | **Letters** | Flip through big letter cards (A is for Apple 🍎) and hear each one spoken. |
| 🖍️ | **Draw** | Trace a big letter with a finger using bright crayons. Fill it in to earn stars. |
| ❓ | **Quiz** | Hear "Find the letter B!" and tap the right one of three big letters. |

## Building

Requirements: JDK 17 and the Android SDK (platform 35).

```bash
./gradlew assembleDebug        # build the APK
./gradlew installDebug         # install on a connected device/emulator
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
Or open the folder in Android Studio and press ▶ Run.

Speech uses the device's built-in text-to-speech engine (English, US).

## Tech

Kotlin, Jetpack Compose, Material 3. No third-party libraries, no network, no ads.
