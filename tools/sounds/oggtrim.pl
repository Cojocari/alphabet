#!/usr/bin/perl
# Usage: oggtrim.pl in.ogg out.ogg seconds
# Keeps the Ogg Vorbis pages up to <seconds>, marks the last page end-of-stream, fixes its CRC.
use strict; use warnings;
my ($in, $out, $secs) = @ARGV;
open my $fh, '<:raw', $in or die; local $/; my $d = <$fh>; close $fh;
my @crc; for my $i (0..255) { my $r = $i << 24; for (1..8) { $r = ($r & 0x80000000) ? (($r << 1) ^ 0x04c11db7) : ($r << 1); $r &= 0xffffffff } $crc[$i] = $r }
sub crc { my $c = 0; for my $b (unpack 'C*', $_[0]) { $c = (($c << 8) & 0xffffffff) ^ $crc[(($c >> 24) ^ $b) & 0xff] } $c }
my ($pos, $rate, @pages) = (0, 0);
while ($pos < length $d) {
  die "bad page at $pos" unless substr($d, $pos, 4) eq 'OggS';
  my $nseg = ord substr($d, $pos + 26, 1);
  my @lac = unpack 'C*', substr($d, $pos + 27, $nseg);
  my $len = 27 + $nseg; $len += $_ for @lac;
  my $page = substr($d, $pos, $len);
  if (!$rate) { my $i = index($page, "\x01vorbis"); $rate = unpack('V', substr($page, $i + 12, 4)) if $i >= 0 }
  my ($lo, $hi) = unpack 'VV', substr($page, 6, 8);
  push @pages, [$page, ($lo == 0xFFFFFFFF && $hi == 0xFFFFFFFF) ? -1 : $hi * 4294967296 + $lo];
  $pos += $len;
}
die "no vorbis rate" unless $rate;
my $limit = $secs * $rate; my @keep;
for my $p (@pages) { push @keep, $p; last if $p->[1] > $limit && @keep > 3 }
my $last = $keep[-1][0];
substr($last, 5, 1) = chr(ord(substr($last, 5, 1)) | 4);   # end of stream
substr($last, 22, 4) = "\0\0\0\0"; substr($last, 22, 4) = pack 'V', crc($last);
$keep[-1][0] = $last;
open my $o, '>:raw', $out or die; print $o join '', map { $_->[0] } @keep; close $o;
printf "%-22s %4d Hz  %6.1fs kept of %6.1fs\n", $in =~ s{.*/}{}r, $rate, $keep[-1][1] / $rate, $pages[-1][1] / $rate;
