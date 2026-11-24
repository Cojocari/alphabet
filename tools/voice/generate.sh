#!/bin/bash
# Records every sentence of the app with an Azure voice (see README.md in this folder).
# Usage: tools/voice/generate.sh en|ro [more languages]
# Needs a key file OUTSIDE the repository (default ~/azure-speech.txt, or AZURE_SPEECH_FILE):
#   AZURE_SPEECH_KEY=...
#   AZURE_SPEECH_REGION=eastus
# Only missing clips are recorded, so it can be stopped and started again at any time.
set -u
cd "$(dirname "$0")/../.."
keyfile=${AZURE_SPEECH_FILE:-$HOME/azure-speech.txt}
[ -f "$keyfile" ] || { echo "No key file at $keyfile"; exit 1; }
set -a; eval "$(tr -d '\r' < "$keyfile"; echo)"; set +a

# Free tier (F0): about 20 requests a minute.
PAUSE=${PAUSE:-3.1}
FORMAT=audio-24khz-48kbitrate-mono-mp3

voice_for() { case $1 in en) echo "en-US en-US-AnaNeural" ;; ro) echo "ro-RO ro-RO-Ioana:MAI-Voice-2.1" ;; *) return 1 ;; esac; }

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

for lang in "$@"; do
  read -r locale voice <<< "$(voice_for "$lang")" || { echo "Unknown language $lang"; exit 1; }
  catalog=tools/voice/catalog-$lang.tsv
  out=app/src/androidMain/assets/voice/$lang
  mkdir -p "$out"
  total=$(wc -l < "$catalog"); done_=0; made=0; failed=0
  while IFS=$'\t' read -r clip sentence; do
    done_=$((done_ + 1))
    [ -s "$out/$clip.mp3" ] && continue
    text=$(printf '%s' "$sentence" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g')
    # The text goes through a UTF-8 file: Windows command-line arguments would turn ă, ș, ț into a, s, t.
    printf '<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="%s"><voice name="%s">%s</voice></speak>' \
      "$locale" "$voice" "$text" > "$tmp/request.xml"
    started=$EPOCHREALTIME
    for attempt in 1 2 3 4 5 6; do
      code=$(curl -s -o "$tmp/clip.mp3" -D "$tmp/headers" -w "%{http_code}" -X POST \
        "https://$AZURE_SPEECH_REGION.tts.speech.microsoft.com/cognitiveservices/v1" \
        -H "Ocp-Apim-Subscription-Key: $AZURE_SPEECH_KEY" \
        -H "Content-Type: application/ssml+xml; charset=utf-8" \
        -H "X-Microsoft-OutputFormat: $FORMAT" \
        -H "User-Agent: voice-catalog-tool" \
        --data-binary "@$tmp/request.xml")
      if [ "$code" = 429 ]; then
        wait=$(grep -i '^retry-after:' "$tmp/headers" | tr -dc '0-9'); sleep "${wait:-30}"; continue
      fi
      break
    done
    if [ "$code" = 200 ] && [ -s "$tmp/clip.mp3" ]; then
      mv "$tmp/clip.mp3" "$out/$clip.mp3"; made=$((made + 1))
    else
      failed=$((failed + 1)); echo "FAIL $lang $clip HTTP $code: $sentence"
    fi
    [ $((made % 50)) = 0 ] && [ "$made" -gt 0 ] && echo "$lang: $done_/$total (recorded $made, failed $failed)"
    # Keep about PAUSE seconds between the starts of two requests.
    rest=$(awk -v p="$PAUSE" -v s="$started" -v n="$EPOCHREALTIME" 'BEGIN { r = p - (n - s); print (r > 0 ? r : 0) }')
    sleep "$rest"
  done < "$catalog"
  # Clips for sentences the app no longer says.
  removed=0
  for f in "$out"/*.mp3; do
    [ -e "$f" ] || continue
    grep -q "^$(basename "$f" .mp3)"$'\t' "$catalog" || { rm "$f"; removed=$((removed + 1)); }
  done
  echo "$lang done: recorded $made, failed $failed, removed $removed old clips, $(ls "$out" | wc -l) clips, $(du -sh "$out" | cut -f1)"
done
