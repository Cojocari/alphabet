# Sound tools

Used to find, check and shorten the animal recordings in `app/src/main/res/raw/animal_*`.
Credits and licences: `docs/sound-credits.md` (keep it and `SoundCredits` in
`ui/parent/Licenses.kt` in sync).

| Script | What it does |
|---|---|
| `bsb.sh "<query>" [n]` | Lists BigSoundBank search results: length, sound number, title, description. Open `https://bigsoundbank.com/sound-<number>-x.html` to check the licence (must say CC0), download `https://bigsoundbank.com/UPLOAD/ogg/<number>.ogg`. |
| `oggtrim.pl in.ogg out.ogg <seconds>` | Shortens an Ogg Vorbis file without re-encoding (cuts at page boundaries, marks the end, fixes the checksum). |
| `wavclip.pl in.wav out.wav <seconds> <highpassHz>` | For WAV sources: finds the window with the most high-frequency energy, high-passes it, normalises it and writes 16-bit mono WAV. |

Rules: only CC0, public domain or CC BY recordings (CC BY needs a credit line in the app);
no AI-generated sounds; check the licence of each individual file; keep downloads outside the
repo until checked. Wikimedia Commons rate-limits quickly: wait a few seconds between API calls.
Every sound plays at least `MinSoundMs` (2 s): short ones are repeated by `AnimalSoundPlayer`.
