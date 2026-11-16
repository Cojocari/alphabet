#!/bin/bash
# Lists BigSoundBank search results: length  id  title :: description
q=$(printf '%s' "$1" | sed 's/ /+/g')
curl -s -A "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" "https://bigsoundbank.com/search?q=$q" | tr -d '\n' \
 | perl -ne 'while (/MyAudio(\d+)".*?Listen to this sound : ([^"]*)"(.*?)Length: ([\d:]+)/g) { my ($id,$t,$d,$l)=($1,$2,$3,$4); $d =~ s/<[^>]*>//g; $d =~ s/\s+/ /g; printf "%s %s %-28s %s\n", $l, $id, $t, substr($d,0,110) }' | head -${2:-12}
