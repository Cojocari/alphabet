#!/bin/bash
# Cuts the quiet tail off every recorded clip (see TrimClips.java). Safe to run again.
# Usage: tools/voice/trim.sh [en|ro ...]   (default: every language)
# Needs JDK 17+ (JAVA_HOME or java on PATH). Downloads the JLayer MP3 decoder (LGPL, used only
# by this tool, not shipped in the app) from Maven Central into build/voice-tools/.
set -eu
cd "$(dirname "$0")/../.."
jar=build/voice-tools/jlayer-1.0.1.jar
sha1=2bfef7a5a4c9af2184ff74b460b6d7d24349b98a
if [ ! -f "$jar" ]; then
  mkdir -p build/voice-tools
  curl -sfL -o "$jar" https://repo1.maven.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar
fi
[ "$(sha1sum "$jar" | cut -c1-40)" = "$sha1" ] || { echo "Unexpected JLayer jar checksum"; rm -f "$jar"; exit 1; }
java=${JAVA_HOME:+$JAVA_HOME/bin/}java
langs=${*:-$(ls app/src/androidMain/assets/voice)}
dirs=(); for l in $langs; do dirs+=("app/src/androidMain/assets/voice/$l"); done
# Windows Java wants Windows paths.
if command -v cygpath >/dev/null; then
  "$java" -cp "$(cygpath -w "$jar")" "$(cygpath -w tools/voice/TrimClips.java)" "${dirs[@]}"
else
  "$java" -cp "$jar" tools/voice/TrimClips.java "${dirs[@]}"
fi
