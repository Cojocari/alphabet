#!/bin/sh
# Copies the files the iOS app needs from the Android resources into iosApp/Generated:
# the animal recordings, converted to AAC (.m4a, which iOS plays natively; Ogg isn't),
# and the Apache License text. Needs ffmpeg (brew install ffmpeg).
set -eu
root=$(cd "$(dirname "$0")/../.." && pwd)
raw="$root/app/src/androidMain/res/raw"
out="$root/iosApp/Generated"
rm -rf "$out"
mkdir -p "$out"
for f in "$raw"/animal_*; do
  name=$(basename "$f")
  ffmpeg -nostdin -loglevel error -y -i "$f" -vn -c:a aac -b:a 128k "$out/${name%.*}.m4a"
done
cp "$raw/apache_license_2_0.txt" "$out/"
echo "Prepared $(ls "$out" | wc -l | tr -d ' ') files in $out"
