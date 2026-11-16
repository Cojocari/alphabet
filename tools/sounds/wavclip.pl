#!/usr/bin/perl
# Usage: wavclip.pl in.wav out.wav seconds highpassHz
# Picks the <seconds> window with the most high-frequency energy, high-passes it,
# normalises it, fades the ends and writes a 16-bit mono WAV.
use strict; use warnings;
my ($in, $out, $secs, $hp) = @ARGV;
open my $fh, '<:raw', $in or die; local $/; my $d = <$fh>; close $fh;
my $p = 12; my ($ch, $rate, $bits, $data);
while ($p < length $d) {
  my ($id, $len) = unpack 'A4 V', substr($d, $p, 8);
  if ($id eq 'fmt') { (undef, $ch, $rate, undef, undef, $bits) = unpack 'v v V V v v', substr($d, $p + 8, 16) }
  if ($id eq 'data') { $data = substr($d, $p + 8, $len) }
  $p += 8 + $len + ($len & 1);
}
my $bps = $bits / 8; my $frame = $bps * $ch; my $n = int(length($data) / $frame);
print "format: $ch ch, $rate Hz, $bits bit, ", sprintf("%.1f", $n / $rate), " s\n";
my @x;
for my $i (0 .. $n - 1) {
  my $s = 0;
  for my $c (0 .. $ch - 1) {
    my $o = $i * $frame + $c * $bps;
    if ($bps == 2) { $s += unpack('s<', substr($data, $o, 2)) / 32768 }
    else { my $v = unpack('V', substr($data, $o, 3) . "\0"); $v -= 1 << 24 if $v & 0x800000; $s += $v / 8388608 }
  }
  push @x, $s / $ch;
}
# One-pole high-pass, applied twice for a steeper slope.
my $a = exp(-2 * 3.14159265 * $hp / $rate);
for (1 .. 2) { my ($px, $py) = (0, 0); for my $i (0 .. $#x) { my $y = $a * ($py + $x[$i] - $px); $px = $x[$i]; $py = $y; $x[$i] = $y } }
# Energy per 100 ms, then the best window.
my $w = int($rate / 10); my @e;
for (my $i = 0; $i + $w <= @x; $i += $w) { my $s = 0; $s += $_ * $_ for @x[$i .. $i + $w - 1]; push @e, $s }
my $span = int($secs * 10); my ($best, $bestE) = (0, -1);
for my $k (0 .. @e - $span) { my $s = 0; $s += $_ for @e[$k .. $k + $span - 1]; ($best, $bestE) = ($k, $s) if $s > $bestE }
my $mean = 0; $mean += $_ for @e; $mean /= @e;
printf "best window starts at %.1f s (energy %.1fx the average)\n", $best / 10, ($bestE / $span) / $mean;
my @c = @x[$best * $w .. $best * $w + $span * $w - 1];
my $peak = 0; for (@c) { $peak = abs $_ if abs $_ > $peak }
my $fade = int($rate * 0.03);
my $pcm = '';
for my $i (0 .. $#c) {
  my $g = 1; $g = $i / $fade if $i < $fade; $g = ($#c - $i) / $fade if $#c - $i < $fade;
  $pcm .= pack 's<', int($c[$i] / $peak * 0.89 * $g * 32767);
}
open my $o, '>:raw', $out or die;
print $o 'RIFF', pack('V', 36 + length $pcm), 'WAVE', 'fmt ', pack('V v v V V v v', 16, 1, 1, $rate, $rate * 2, 2, 16), 'data', pack('V', length $pcm), $pcm;
close $o;
printf "wrote %s, %d KB\n", $out =~ s{.*/}{}r, length($pcm) / 1024;
