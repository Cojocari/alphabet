#!/bin/sh
# Frames the App Store screenshots: takes the raw simulator shots from the ci-appstore-shots branch
# (.github/workflows/appstore-shots.yml), adds the brand gradient and a caption (appstore-frame.html,
# the same look as the Play screenshots) and writes the upload-ready files:
#   branding/screenshots/appstore/{en,ro}/iphone-1..8.jpg  1320×2868 (iPhone 6.9", required)
#   branding/screenshots/appstore/{en,ro}/ipad-1..8.jpg    2064×2752 (iPad 13", required)
#   branding/screenshots/appstore/{en,ro}/iap-review.png   the purchase screen, for the in-app purchase review
# Run from Git Bash on Windows (uses headless Edge): sh branding/frame-appstore.sh
set -e
cd "$(dirname "$0")/.."
edge="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
raw=build/appstore-raw
out=branding/screenshots/appstore
# The story question shown second (raw name); change it if another step looks better.
story=${STORY:-story30-3}

git fetch -q origin ci-appstore-shots
rm -rf "$raw" && mkdir -p "$raw"
for f in $(git ls-tree --name-only origin/ci-appstore-shots | grep '\.png$'); do
  git show "origin/ci-appstore-shots:$f" > "$raw/$f"
done
profile=$(mktemp -d)
# Each shot gets its own page from the template: the screen keeps its proportions under the caption.
page() { # page file (next to the image), width, height, raw image, caption
  perl -e '
    my ($tpl, $out, $W, $H, $img, $cap) = @ARGV;
    open my $f, "<", $img or die "no $img\n"; binmode $f; read $f, my $png, 24;
    my ($iw, $ih) = unpack "N2", substr($png, 16, 8);
    sub r { int($_[0] + 0.5) }
    my $top = r($H * 0.15); my $b = r($W * 0.012);
    my ($aw, $ah) = ($W * 0.86, $H - $top - r($H * 0.035));
    my $s = $aw / $iw < $ah / $ih ? $aw / $iw : $ah / $ih;
    my ($w, $h) = (r($iw * $s), r($ih * $s));
    my $chars = () = $cap =~ /[^\x80-\xBF]/g; # characters, not UTF-8 bytes
    my $font = $W * ($W < $H * 0.6 ? 0.072 : 0.058) * ($chars > 44 ? 0.85 : 1); # long captions: two lines, not three
    $cap =~ s/&/&amp;/g; $cap =~ s/</&lt;/g;
    my %v = (W => $W, H => $H, TOP => $top, CAP_TOP => r($H * 0.025), CAP_H => $top - r($H * 0.03),
             FONT => r($font), FW => $w, FH => $h, BORDER => $b,
             LEFT => r(($W - $w) / 2 - $b), R => r($W * 0.06), IR => r($W * 0.05), CAP => $cap,
             IMG => ($img =~ m{([^/]+)$})[0]);
    open my $t, "<", $tpl; local $/; my $x = <$t>;
    $x =~ s/\{\{(\w+)\}\}/$v{$1}/g;
    open my $o, ">", $out; print $o $x;
  ' branding/appstore-frame.html "$@"
}

# Order and captions as on Google Play (docs/store-listing.md): number|raw shot|English|Romanian
captions="1|home|Learn through play: letters, numbers, reading|Învață jucându-te: litere, cifre, citire
2|$story|Hupi's Adventure: teach Hupi and learn along|Aventura lui Hupi: îl înveți pe Hupi și înveți și tu
3|map|7 story chapters, step by step|7 capitole de poveste, pas cu pas
4|trace|Trace letters and numbers with helpful arrows|Scrie literele și cifrele cu săgeți ajutătoare
5|letters|\"Learn with Hupi\" in every world|„Învață cu Hupi” în fiecare lume
6|house|See what your child has learned|Vezi ce a învățat copilul
7|balloons|24 games on 3 levels|24 de jocuri, pe 3 niveluri
8|animals|28 animals with real sounds|28 de animale cu sunete reale"

for lang in en ro; do
  mkdir -p "$out/$lang"
  echo "$captions" | while IFS='|' read -r n shot en ro; do
    cap=$en; [ $lang = ro ] && cap=$ro
    cap=$(printf '%s' "$cap" | sed 's/\\"/"/g')
    for kind in iphone ipad; do
      if [ $kind = iphone ]; then src=phone; w=1320; h=2868; else src=tablet; w=2064; h=2752; fi
      dest="$out/$lang/$kind-$n.png"
      page "$raw/frame.html" $w $h "$raw/$lang-$src-$shot.png" "$cap"
      rm -f "$dest"
      # msedge.exe returns at once and writes the picture a few seconds later: wait for it. A new profile
      # each time, or Edge hands the page to the instance still running and draws nothing.
      "$edge" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
        --user-data-dir="$(cygpath -w "$profile/$lang-$kind-$n")" \
        --window-size=$w,$h --screenshot="$(cygpath -w "$PWD/$dest")" \
        "$(cygpath -w "$PWD/$raw/frame.html")" 2>/dev/null
      for i in $(seq 1 60); do [ -s "$dest" ] && break; sleep 1; done
      [ -s "$dest" ] || { echo "Edge made no $dest"; exit 1; }
      sleep 2 # let that Edge quit before the page is rewritten
      echo "$dest"
    done
  done
  # JPEG quality 92 like the Play screenshots: a fifth of the PNG size, no visible difference.
  powershell -NoProfile -Command "Add-Type -AssemblyName System.Drawing;
    \$codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { \$_.MimeType -eq 'image/jpeg' };
    \$p = New-Object System.Drawing.Imaging.EncoderParameters 1;
    \$p.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter ([System.Drawing.Imaging.Encoder]::Quality, [long]92);
    Get-ChildItem '$(cygpath -m "$PWD/$out/$lang")' -Filter '*-?.png' | ForEach-Object {
      \$jpg = [System.IO.Path]::ChangeExtension(\$_.FullName, '.jpg');
      \$i = [System.Drawing.Image]::FromFile(\$_.FullName); \$i.Save(\$jpg, \$codec, \$p); \$i.Dispose();
      if ((Get-Item \$jpg).Length -gt 0) { Remove-Item \$_.FullName } }"
  # The purchase screen for Apple's in-app purchase review, cut above the debug build's developer switch.
  powershell -NoProfile -Command "Add-Type -AssemblyName System.Drawing;
    \$i = [System.Drawing.Image]::FromFile('$(cygpath -m "$PWD/$raw/$lang-phone-unlock.png")');
    \$c = \$i.Clone([System.Drawing.Rectangle]::new(0, 0, \$i.Width, [int](\$i.Height * 0.81)), \$i.PixelFormat);
    \$c.Save('$(cygpath -m "$PWD/$out/$lang/iap-review.png")'); \$c.Dispose(); \$i.Dispose()"
done
rm -rf "$profile"
