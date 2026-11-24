package com.cojocari.alphabet.audio

/**
 * Helpers shared by the recorded voice ([RecordedSpeaker]) and the tool that lists every sentence
 * for recording (VoiceCatalogTest, tools/voice): both must split and name sentences the same way.
 *
 * Each sentence is one recorded clip, so "Well done! That's B." uses the clips "Well done!" and
 * "That's B.", and praise can be combined with any answer without recording every pair.
 */
fun voiceSentences(text: String): List<String> {
    val sentences = mutableListOf<String>()
    val current = StringBuilder()
    for (i in text.indices) {
        val c = text[i]
        current.append(c)
        val end = c == '.' || c == '!' || c == '?' || c == '…'
        if (end && (i == text.lastIndex || text[i + 1].isWhitespace())) {
            sentences += current.toString()
            current.clear()
        }
    }
    sentences += current.toString()
    return sentences.map { normalizeSentence(it) }.filter { s -> s.any { it.isLetterOrDigit() } }
}

/** Trims and collapses spaces, so the same sentence always gets the same clip. */
fun normalizeSentence(sentence: String): String =
    sentence.trim().split(' ', '\n', '\t').filter { it.isNotEmpty() }.joinToString(" ")

/** The clip's file name (without ".mp3"): FNV-1a 64-bit hash of the sentence's UTF-8 bytes. */
fun voiceClipName(sentence: String): String {
    var hash = 0xcbf29ce484222325uL
    for (b in sentence.encodeToByteArray()) {
        hash = hash xor b.toUByte().toULong()
        hash *= 0x100000001b3uL
    }
    return hash.toString(16).padStart(16, '0')
}

/**
 * The sentence without the child's [name]: "Hi Maria!" → "Hi!", "Wow, Maria! …" → "Wow! …".
 * Recordings can't say every name, so sentences with the name use the recording without it.
 */
fun stripName(sentence: String, name: String): String {
    if (name.isBlank()) return sentence
    var s = sentence
    while (true) {
        val i = s.indexOf(name, ignoreCase = true)
        if (i < 0) break
        var start = i
        while (start > 0 && (s[start - 1] == ' ' || s[start - 1] == ',')) start--
        s = s.substring(0, start) + s.substring(i + name.length)
    }
    return normalizeSentence(s)
}
