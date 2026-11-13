# Rules for the release build's code shrinker (R8).
# Compose, AndroidX and Google Play Billing ship their own rules, so little is needed here.

# Keep line numbers in crash reports (Play Console → Android vitals) readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
