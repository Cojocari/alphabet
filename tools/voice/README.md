# Recorded voice

Hupi's voice is recorded ahead of time with Microsoft Azure AI Speech (synthetic voices) instead
of being made on the device by text-to-speech. Each sentence the app can say is one MP3 clip in
`app/src/androidMain/assets/voice/<lang>/<clip>.mp3`. Android reads them from the assets; iOS
copies the same folder into the app bundle (`iosApp/project.yml`).

| Language | Voice |
|---|---|
| English (`en`) | `en-US-AnaNeural` (a child's voice) |
| Romanian (`ro`) | `ro-RO-Ioana:MAI-Voice-2.1` |

At runtime (`audio/RecordedSpeaker.kt`) a text is split into sentences (`voiceSentences`); if every
sentence has a clip, the clips are played one after another. Otherwise the device voice says the
whole text, so one text never switches voices halfway. Debug builds log each sentence without a
clip: `adb logcat -s VoiceMiss`.

The child's name is never recorded: a sentence with the name plays the recording without it
("Hi Maria!" → "Hi!", see `stripName`).

## After changing any text

1. List every sentence again (writes `catalog-en.tsv` and `catalog-ro.tsv` here):

   ```
   ./gradlew testDebugUnitTest --tests "*VoiceCatalogTest*"
   ```

   The test calls every phrase in `Strings` and `StoryStrings` with all the values the game uses,
   and runs the game and story question generators. A new phrase function with a parameter it
   doesn't know fails the test with "No values for …": add its values in `domain()`.

2. Record the missing clips (and delete clips no longer used):

   ```
   tools/voice/generate.sh ro en
   ```

   It needs the Azure Speech key in a file outside the repository, `~/azure-speech.txt`
   (or the path in `AZURE_SPEECH_FILE`), never committed:

   ```
   AZURE_SPEECH_KEY=...
   AZURE_SPEECH_REGION=eastus
   ```

   Only missing clips are recorded, so it can be stopped and restarted. The free tier (F0) allows
   about 20 requests a minute and 500,000 characters a month; all ~5,800 sentences are about
   150,000 characters and take about 5 hours.

   At the end it runs `tools/voice/trim.sh`, which cuts the quiet tail off each clip (the voices
   add up to 1.5 s of near-silence, which made "A." + "A is for Apple!" sound far apart). It
   decodes with JLayer, downloaded from Maven Central into `build/` and not shipped in the app.

## Pitfalls

- On Windows, text passed to `curl` as a command-line argument loses its diacritics (ă, ș, ț
  become a, s, t). The script writes each request to a UTF-8 file and sends that.
- English reads a lone capital "A" as the article ("uh"), so "A is for Apple!" sounded like
  another letter. The script marks lone capitals as letters (`say-as characters`), except "A"
  or "I" before a word other than "is" ("A treasure chest!", "I can…").
- The clip name is the FNV-1a hash of the sentence, so changing one comma means a new clip.
