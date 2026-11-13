package com.cojocari.alphabet.data

/** A short, easy-to-read word with a picture, for the reading and spelling games. */
class Word(val text: String, val emoji: String)

/** Three- and four-letter words a beginner can sound out. */
val EnglishWords = listOf(
    Word("cat", "🐱"), Word("dog", "🐶"), Word("sun", "☀️"), Word("bee", "🐝"),
    Word("pig", "🐷"), Word("fish", "🐟"), Word("moon", "🌙"), Word("key", "🔑"),
    Word("car", "🚗"), Word("bus", "🚌"), Word("egg", "🥚"), Word("cup", "☕"),
    Word("bed", "🛏️"), Word("fox", "🦊"), Word("owl", "🦉"), Word("cake", "🎂"),
    Word("ball", "⚽"), Word("tree", "🌳"), Word("star", "⭐"), Word("cow", "🐮"),
    Word("hat", "🎩"), Word("frog", "🐸"), Word("duck", "🦆"), Word("milk", "🥛"),
)

/** Short Romanian words, written exactly as children learn them in the abecedar. */
val RomanianWords = listOf(
    Word("casă", "🏠"), Word("urs", "🐻"), Word("ou", "🥚"), Word("măr", "🍎"),
    Word("nor", "☁️"), Word("cal", "🐴"), Word("lup", "🐺"), Word("rac", "🦀"),
    Word("pom", "🌳"), Word("tort", "🎂"), Word("stea", "⭐"), Word("lună", "🌙"),
    Word("soare", "☀️"), Word("pește", "🐟"), Word("minge", "⚽"), Word("vapor", "🚢"),
    Word("lapte", "🥛"), Word("vacă", "🐮"), Word("rață", "🦆"), Word("pisică", "🐱"),
    Word("cheie", "🔑"), Word("tren", "🚂"), Word("leu", "🦁"), Word("floare", "🌸"),
)
