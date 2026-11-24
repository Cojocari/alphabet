package com.cojocari.alphabet.story

import com.cojocari.alphabet.data.ArtColors
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.platform.KeyValueStore
import com.cojocari.alphabet.i18n.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Checks the story content for every language, level and scene. */
class CurriculumTest {
    @Test
    fun everyLessonTeachesSomethingThatExists() {
        for (language in Language.entries) for (level in Difficulty.entries) for (scene in 0 until TotalScenes) {
            if (isShow(scene)) continue
            val items = lessonItems(scene, level, language)
            assertTrue("$language $level scene $scene has nothing to teach", items.isNotEmpty())
            items.forEach { item ->
                when (item.kind) {
                    Kind.Letter -> assertNotNull("$language $level scene $scene: ${item.id}", item.letter(language))
                    Kind.Word -> assertNotNull("$language $level scene $scene: ${item.id}", item.word(language))
                    else -> {}
                }
                assertTrue("${item.id} belongs to $language", item.belongsTo(language))
                assertEquals(item, Item.fromId(item.id))
            }
        }
    }

    @Test
    fun everyChapterHasAFriendPerLessonAndSomethingToShow() {
        for (scene in 0 until TotalScenes) {
            if (!isShow(scene)) assertNotNull("scene $scene friend", friendOf(scene))
        }
        for (language in Language.entries) for (level in Difficulty.entries) for (chapter in 0 until ChapterCount) {
            assertTrue(chapterItems(chapter, level, language).isNotEmpty())
        }
    }

    @Test
    fun questionsAreWellFormed() {
        val rnd = Random(1)
        for (language in Language.entries) for (level in Difficulty.entries) for (scene in 0 until TotalScenes) {
            if (isShow(scene)) continue
            for (item in lessonItems(scene, level, language)) for (count in 2..4) repeat(5) {
                val firstLetter = chapterOf(scene) == Subject.Reading && level == Difficulty.Easy
                val q = questFor(item, language, level, count, rnd, firstLetter)
                assertTrue("${item.id}: answer index", q.answer in q.choices.indices)
                assertEquals("${item.id}: names", q.choices.size, q.names.size)
                if (item.kind != Kind.Math && item.kind != Kind.Color) {
                    assertEquals("${item.id}: duplicate tiles", q.choices.size, q.choices.map { it.text }.distinct().size)
                }
                assertTrue("${item.id}: ask", q.ask.isNotBlank())
                if (item.kind != Kind.Math) {
                    assertTrue("${item.id}: name", item.name(language).isNotBlank())
                    assertTrue("${item.id}: meet", item.meetLine(language).isNotBlank())
                }
            }
        }
    }

    @Test
    fun worldPathsCoverEveryChapterOnceAndStartFree() {
        assertEquals((0 until ChapterCount).toList(), Paths.all.flatten().sorted())
        for (path in Paths.all) {
            val scenes = pathScenes(path)
            assertEquals(path.size * ScenesPerChapter, scenes.size)
            assertTrue("first lesson of $path is free", pathFree(scenes.first(), path))
            assertTrue("show ends $path", isShow(scenes.last()))
        }
        // Free chapters stay free; a paid chapter offers only its first lesson.
        assertTrue(pathScenes(Paths.Art).all { pathFree(it, Paths.Art) })
        assertEquals(1, pathScenes(Paths.Letters).count { pathFree(it, Paths.Letters) })
    }

    @Test
    fun romanianAlphabetTeachesOnlyTheBaseLetters() {
        assertEquals(26, Language.Romanian.alphabet.size)
        assertTrue(Language.Romanian.alphabet.none { it.char in "ĂÂÎȘȚ" })
        // Old saved progress may still hold Ș: it is ignored, but words keep their diacritics.
        assertFalse(Item(Kind.Letter, "Romanian/Ș").belongsTo(Language.Romanian))
        assertTrue(Item(Kind.Letter, "Romanian/S").belongsTo(Language.Romanian))
        assertTrue(Item(Kind.Word, "Romanian/pește").belongsTo(Language.Romanian))
    }

    @Test
    fun mathStaysInRange() {
        val rnd = Random(7)
        for (key in listOf("count:5", "more:5", "add:3", "count:10", "add:5", "sub:5", "compare:10", "add:10", "sub:10", "add:20", "sub:20")) {
            repeat(500) {
                val q = questFor(Item(Kind.Math, key), Language.English, Difficulty.Medium, 3, rnd)
                assertTrue("$key answer", q.answer in q.choices.indices)
                val max = key.substringAfter(':').toInt()
                q.choices.forEach { c -> c.text.toIntOrNull()?.let { assertTrue("$key choice $it", it in 0..max.coerceAtLeast(4)) } }
            }
        }
    }
    @Test
    fun everyLevelTeachesAllColoursAndAllLetters() {
        for (language in Language.entries) for (level in Difficulty.entries) {
            val colors = chapterItems(Subject.ColorsShapes, level, language).filter { it.kind == Kind.Color }.map { it.key.toInt() }
            assertEquals("$level colours", ArtColors.indices.toSet(), colors.toSet())
            val letters = (chapterItems(Subject.LettersRiver, level, language) + chapterItems(Subject.LettersLibrary, level, language))
                .map { it.letter(language)!!.char }
            assertEquals("$language $level letters", language.alphabet.map { it.char }, letters)
        }
    }

    @Test
    fun everyLessonHasItsOwnTreasureAndTheMagpieComesOnceAChapter() {
        val treasures = (0 until TotalScenes).filter { !isShow(it) }.map { treasureOf(it) }
        assertTrue(treasures.none { it == null })
        assertEquals(treasures.size, treasures.toSet().size)
        assertTrue((0 until TotalScenes).filter { isShow(it) }.all { treasureOf(it) == null })
        val story = StoryProgress(MemoryStore())
        for (language in Language.entries) for (level in Difficulty.entries) for (scene in 0 until TotalScenes) {
            val steps = buildScene(scene, language, level, story, premium = true, rnd = Random(scene))
            val magpies = steps.count { it is Step.Magpie }
            assertEquals("scene $scene magpie", if (scene % ScenesPerChapter == MagpieLesson) 1 else 0, magpies)
            if (!isShow(scene)) assertEquals(treasureOf(scene), (steps.last { it is Step.Reward } as Step.Reward).treasure)
            // World paths have no magpie plot.
            val path = Paths.all.first { chapterOf(scene) in it }
            assertTrue(buildScene(scene, language, level, story, true, Random(scene), path).none { it is Step.Magpie })
        }
    }

    @Test
    fun savedProgressMovesToTheSameLessonInLongerChapters() {
        // Old layout: 7 scenes a chapter. 16 = chapter 3, lesson 3 (2 × 7 + 2 scenes done).
        val store = MemoryStore().apply { putInt("done", 16); putInt("path1", 7); putInt("path5", 3) }
        val story = StoryProgress(store)
        assertEquals(2 * ScenesPerChapter + 2, story.done)
        assertEquals(2, story.stage)
        assertEquals(ScenesPerChapter, story.chapterDone(1))
        assertEquals(3, story.chapterDone(5))
        // Moved only once.
        assertEquals(2 * ScenesPerChapter + 2, StoryProgress(store).done)
        assertEquals(0, StoryProgress(MemoryStore()).done)
    }

    private class MemoryStore : KeyValueStore {
        private val map = HashMap<String, Any>()
        override fun getInt(key: String, default: Int) = map[key] as? Int ?: default
        override fun getLong(key: String, default: Long) = map[key] as? Long ?: default
        override fun getBoolean(key: String, default: Boolean) = map[key] as? Boolean ?: default
        override fun getString(key: String) = map[key] as? String
        override fun putInt(key: String, value: Int) { map[key] = value }
        override fun putLong(key: String, value: Long) { map[key] = value }
        override fun putBoolean(key: String, value: Boolean) { map[key] = value }
        override fun putString(key: String, value: String) { map[key] = value }
        override fun remove(key: String) { map.remove(key) }
        override fun clear() = map.clear()
    }
}
