package com.cojocari.alphabet.audio

import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.Animals
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.data.RomanianDiacriticNames
import com.cojocari.alphabet.data.Stickers
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.i18n.StoryStrings
import com.cojocari.alphabet.i18n.Strings
import com.cojocari.alphabet.story.ChapterCount
import com.cojocari.alphabet.story.Item
import com.cojocari.alphabet.story.Kind
import com.cojocari.alphabet.story.ScenesPerChapter
import com.cojocari.alphabet.story.TotalScenes
import com.cojocari.alphabet.story.chapterItems
import com.cojocari.alphabet.story.chapterOf
import com.cojocari.alphabet.story.friendOf
import com.cojocari.alphabet.story.meetLine
import com.cojocari.alphabet.story.name
import com.cojocari.alphabet.story.questFor
import com.cojocari.alphabet.story.short
import com.cojocari.alphabet.ui.games.PickRound
import com.cojocari.alphabet.ui.games.addRound
import com.cojocari.alphabet.ui.games.colorRound
import com.cojocari.alphabet.ui.games.compareRound
import com.cojocari.alphabet.ui.games.findNumberRound
import com.cojocari.alphabet.ui.games.firstLetterRound
import com.cojocari.alphabet.ui.games.howManyRound
import com.cojocari.alphabet.ui.games.readPictureRound
import com.cojocari.alphabet.ui.games.shapeRound
import com.cojocari.alphabet.ui.games.subtractRound
import com.cojocari.alphabet.ui.games.whichWordRound
import java.io.File
import kotlin.random.Random
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMembers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lists every sentence the app can say, per language, for the recorded voice
 * (tools/voice/README.md). Writes tools/voice/catalog-<lang>.tsv: clip name, tab, sentence.
 *
 * Every phrase in [Strings] and [StoryStrings] is called with every value the game can pass
 * (all letters, numbers, colours, animals, story items…), and the game and story question
 * generators run with many seeds. A sentence that is still missing at runtime falls back to
 * the device voice and is logged in debug builds (logcat tag "VoiceMiss").
 */
class VoiceCatalogTest {
    /** Shown on screen, never spoken (or spoken with numbers the catalogue can't foresee). */
    private val notSpoken = setOf(
        "chapterLabel", "chapterTitle", "storyAt", "gateQuestion", "lettersLearned", "unlockButton",
        "unlockBodyFor", "playedToday", "cap", "privacyPolicy", "privacyPolicyUrl", "appName", "unlockUnavailable", "familyNote",
        // Names used inside other sentences, never said on their own.
        "letterName", "numberName", "colorName", "wordName", "mathName", "friendName", "animalName", "animalSound",
        // Built from story items and lessons below, with only the combinations that exist.
        "catchAsk", "friendAsk",
    )

    /** Phrases that include the child's name: recorded without it (see [stripName]). */
    private val withName = setOf("greet", "newStickerFor", "restSay", "diploma")
    private val placeholderName = "Qwertyna"

    @Test
    fun splittingAndNaming() {
        assertEquals(listOf("Well done!", "That's B.", "Find C?"), voiceSentences(" Well done!  That's B. Find C? "))
        assertEquals(listOf("Oh... yes!"), voiceSentences("Oh... yes!").let { listOf(it.joinToString(" ")) })
        assertEquals("Hi!", stripName("Hi Maria!", "Maria"))
        assertEquals("Wow!", stripName("Wow, Maria!", "maria"))
        assertEquals("Te-ai jucat frumos azi!", stripName("Te-ai jucat frumos azi, Maria!", "Maria"))
        assertEquals(voiceClipName("Bună!"), voiceClipName("Bună!"))
        assertTrue(voiceClipName("Bună!") != voiceClipName("Buna!"))
    }

    @Test
    fun writeCatalog() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        val dir = File(root, "tools/voice").apply { mkdirs() }
        for (language in Language.entries) {
            val sentences = catalog(language)
            val lines = sentences.map { "${voiceClipName(it)}\t$it" }
            assertEquals("clip name collision", sentences.size, lines.map { it.substringBefore('\t') }.toSet().size)
            File(dir, "catalog-${language.code}.tsv").writeText(lines.joinToString("\n", postfix = "\n"))
            println("${language.code}: ${sentences.size} sentences, ${sentences.sumOf { it.length }} characters")
        }
    }

    private fun catalog(language: Language): Set<String> {
        val t = language.strings
        val s = language.story
        val out = sortedSetOf<String>()
        fun add(value: Any?) {
            when (value) {
                is String -> voiceSentences(value).forEach { out += it }
                is Iterable<*> -> value.forEach { add(it) }
                is Array<*> -> value.forEach { add(it) }
            }
        }
        fun addNamed(value: Any?) {
            if (value is String) voiceSentences(value).forEach { out += stripName(it, placeholderName) }
        }

        // Story items and the questions about them (also gives every name Hupi can use).
        val items = (0 until ChapterCount).flatMap { c -> Difficulty.entries.flatMap { chapterItems(c, it, language) } }.toSet()
        val things = sortedSetOf<String>()
        items.forEach { item ->
            add(item.meetLine(language))
            add(item.short(language).ifEmpty { item.name(language) } + "!")
            things += item.name(language)
            if (item.kind == Kind.Letter || item.kind == Kind.Number || item.kind == Kind.Color) {
                val kindLabel = when (item.kind) { Kind.Letter -> "letter"; Kind.Number -> "number"; else -> "color" }
                add(s.catchAsk(kindLabel, item.short(language)))
            }
            for (level in Difficulty.entries) for (count in 2..4) for (seed in 0 until 40) for (firstLetter in listOf(false, true)) {
                if (firstLetter && item.kind != Kind.Letter) continue
                val quest = questFor(item, language, level, count, Random(seed), firstLetter)
                add(quest.ask)
                t.praise.forEach { add(quest.right(it)) }
                quest.choices.indices.forEach { add(quest.wrong(it)) }
                things += quest.names.filter { it.isNotBlank() }
            }
        }

        // Each lesson's friend and what they need.
        for (scene in 0 until TotalScenes) {
            val friend = friendOf(scene) ?: continue
            add(s.friendAsk(chapterOf(scene), scene % ScenesPerChapter, s.friendName(friend)))
        }

        // Mini-game rounds.
        val rounds: List<(Language, Difficulty, Random) -> PickRound> = listOf(
            ::firstLetterRound, ::findNumberRound, ::howManyRound, ::readPictureRound, ::whichWordRound,
            ::addRound, ::subtractRound, ::compareRound, ::colorRound, ::shapeRound,
        )
        for (round in rounds) for (level in Difficulty.entries) for (seed in 0 until 400) {
            val r = round(language, level, Random(seed))
            add(r.ask)
            t.praise.forEach { add(r.right(it)) }
            r.choices.indices.forEach { add(r.wrong(it)) }
        }

        // Letters as they are named on cards, in memory and when spelling.
        language.alphabet.forEach { add(it.name + "."); add(it.name); add(it.word) }
        if (language == Language.Romanian) RomanianDiacriticNames.values.forEach { add(it) }

        // Every phrase, called with every value the game can pass.
        fun domain(fn: KCallable<*>, p: KParameter): List<Any?> = when (p.type.classifier) {
            Letter::class -> language.alphabet
            Animal::class -> Animals
            Boolean::class -> listOf(true, false)
            Int::class -> when (p.name) {
                "n" -> (0..20).toList()
                "a", "b" -> (0..20).toList()
                "chapter" -> (0 until ChapterCount).toList()
                "stage" -> (0..7).toList()
                "stroke" -> (1..12).toList()
                "days" -> (1..30).toList()
                "count" -> (0..Stickers.size).toList()
                else -> error("No values for ${fn.name}(${p.name}: Int)")
            }
            String::class -> when (p.name) {
                "praise" -> t.praise
                "word" -> if ("Number" in fn.name) t.numbers else language.words.map { it.text }
                "name" -> if (fn.name in withName) listOf(placeholderName, "") else t.colorNames
                "thing" -> things.toList()
                "friend" -> Animals.map { s.friendName(it) }
                "kind" -> items.filter { it.kind == Kind.Math }.map { it.key.substringBefore(':') }.distinct()
                "key" -> items.filter { it.kind == Kind.Math }.map { it.key }
                else -> error("No values for ${fn.name}(${p.name}: String)")
            }
            else -> error("No values for ${fn.name}(${p.name}: ${p.type})")
        }
        // Sums only with the numbers the games use.
        fun fits(fn: KCallable<*>, args: Map<String?, Any?>): Boolean {
            val a = args["a"] as? Int ?: return true
            val b = args["b"] as? Int ?: return true
            return when {
                fn.name.startsWith("add") -> a >= 1 && b >= 1 && a + b <= 20
                fn.name.startsWith("sub") -> a in 2..20 && b in 1 until a
                else -> true
            }
        }
        fun callAll(obj: Any, type: KClass<*>) {
            for (fn in type.declaredMembers) {
                if (fn.name in notSpoken) continue
                val params = fn.parameters.drop(1)
                val combos = params.fold(listOf(emptyMap<String?, Any?>())) { acc, p ->
                    acc.flatMap { m -> domain(fn, p).map { m + (p.name to it) } }
                }.filter { fits(fn, it) }
                combos.forEach { args ->
                    val value = fn.call(obj, *params.map { args[it.name] }.toTypedArray())
                    if (fn.name in withName) addNamed(value) else add(value)
                }
            }
        }
        callAll(t, Strings::class)
        callAll(s, StoryStrings::class)
        return out
    }
}
