package com.cojocari.alphabet.story

import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.i18n.Language
import org.junit.Assert.assertEquals
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
}
