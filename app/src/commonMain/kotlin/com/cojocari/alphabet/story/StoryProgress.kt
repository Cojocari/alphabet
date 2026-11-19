package com.cojocari.alphabet.story

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.platform.KeyValueStore
import com.cojocari.alphabet.platform.keyValueStore
import com.cojocari.alphabet.platform.todayEpochDay

/** What sort of thing a child learns in the story. */
enum class Kind { Color, Shape, Number, Letter, Word, Math }

/**
 * One thing to learn: a colour (index in ArtColors), a shape (index in Shapes), a number,
 * a letter or word (prefixed by the language, e.g. "Romanian/Ș"), or a math skill such as
 * "add:5" (adding up to 5).
 */
data class Item(val kind: Kind, val key: String) {
    val id: String get() = "${kind.name}:$key"

    companion object {
        fun fromId(id: String): Item? {
            val kind = Kind.entries.firstOrNull { id.startsWith("${it.name}:") } ?: return null
            return Item(kind, id.removePrefix("${kind.name}:"))
        }
    }
}

enum class Status { New, Practising, Learned }

/** First-try correct answers needed, on different days, before something counts as learned. */
const val DaysToLearn = 3

/**
 * Hupi's Adventure progress, saved on the device: scenes finished, Hupi's scarf, the daily
 * warm-up, and the learning tracker. The tracker notes, for each thing to learn, on which
 * days the child got it right on the first try; [DaysToLearn] different days make it learned.
 * Things not yet learned, or learned but not seen for a while, come back for review.
 */
class StoryProgress(private val prefs: KeyValueStore = keyValueStore("story")) {

    /** Scenes finished, in order. The next scene to play is [done]. */
    var done by mutableIntStateOf(prefs.getInt("done", 0))
        private set

    /** Index in [ScarfColors]. */
    var scarf by mutableIntStateOf(prefs.getInt("scarf", 0))
        private set

    private var warmupDay by mutableLongStateOf(prefs.getLong("warmup", -1))

    /** Chapters finished: Hupi grows and gains an item with each one (0–7). */
    val stage: Int get() = (done / ScenesPerChapter).coerceAtMost(ChapterCount)

    val finished: Boolean get() = done >= TotalScenes

    fun finishScene(index: Int) {
        if (index != done) return
        done++
        prefs.putInt("done", done)
    }

    /** Scenes finished in each chapter when played from a world's own path (see [Paths]). */
    private val pathDone = mutableStateMapOf<Int, Int>().apply {
        (0 until ChapterCount).forEach { c -> prefs.getInt("path$c", 0).takeIf { it > 0 }?.let { put(c, it) } }
    }

    /** Scenes finished in [chapter], in the big adventure or in a world's path (0–7). */
    fun chapterDone(chapter: Int): Int =
        maxOf(pathDone[chapter] ?: 0, (done - chapter * ScenesPerChapter).coerceIn(0, ScenesPerChapter))

    /** True when [scene] was finished in the big adventure or in its world's path. */
    fun sceneDone(scene: Int) = scene % ScenesPerChapter < chapterDone(chapterOf(scene))

    /** The next scene to play in [path], or null when the whole path is finished. */
    fun pathNext(path: List<Int>): Int? = pathScenes(path).firstOrNull { !sceneDone(it) }

    fun finishPathScene(scene: Int) {
        val chapter = chapterOf(scene)
        if (scene % ScenesPerChapter != chapterDone(chapter)) return
        pathDone[chapter] = scene % ScenesPerChapter + 1
        prefs.putInt("path$chapter", scene % ScenesPerChapter + 1)
    }

    fun updateScarf(index: Int) {
        scarf = index
        prefs.putInt("scarf", index)
    }

    val warmupDoneToday: Boolean get() = warmupDay == today()

    fun finishWarmup() {
        warmupDay = today()
        prefs.putLong("warmup", warmupDay)
    }

    // ---------------------------------------------------------------- learning tracker

    private class Record(val goodDays: List<Long>, val seen: Int, val lastSeen: Long)

    private val records = mutableStateMapOf<String, Record>().apply { putAll(load()) }

    private fun today() = todayEpochDay()

    /** Notes an answer about [item]; [firstTry] is true when the child got it right straight away. */
    fun record(item: Item, firstTry: Boolean) {
        val today = today()
        val old = records[item.id]
        val days = old?.goodDays.orEmpty()
        val good = if (firstTry && today !in days) (days + today).takeLast(8) else days
        records[item.id] = Record(good, (old?.seen ?: 0) + 1, today)
        save()
    }

    fun status(item: Item): Status {
        val r = records[item.id] ?: return Status.New
        return if (r.goodDays.size >= DaysToLearn) Status.Learned else if (r.seen > 0) Status.Practising else Status.New
    }

    /** Everything the child has met so far. */
    fun met(): List<Item> = records.keys.mapNotNull { Item.fromId(it) }

    /**
     * Things due for review today, most urgent first: those still being practised and not
     * seen today, then learned ones not seen for a while (2, 4, 8… days, up to 3 weeks).
     */
    fun due(filter: (Item) -> Boolean = { true }): List<Item> {
        val today = today()
        return records.entries
            .mapNotNull { (id, r) -> Item.fromId(id)?.let { it to r } }
            .filter { (item, _) -> filter(item) }
            .filter { (_, r) ->
                if (r.goodDays.size < DaysToLearn) r.lastSeen < today
                else today - r.lastSeen >= (1L shl (r.goodDays.size - 2)).coerceAtMost(21)
            }
            .sortedWith(compareBy({ it.second.goodDays.size >= DaysToLearn }, { it.second.lastSeen }))
            .map { it.first }
    }

    fun reset() {
        done = 0
        records.clear()
        pathDone.clear()
        prefs.clear()
        warmupDay = -1
    }

    private fun save() {
        val text = records.entries.joinToString("\n") { (id, r) ->
            "$id|${r.goodDays.joinToString(",")}|${r.seen}|${r.lastSeen}"
        }
        prefs.putString("mastery", text)
    }

    private fun load(): Map<String, Record> =
        prefs.getString("mastery").orEmpty().lines().mapNotNull { line ->
            val p = line.split("|")
            if (p.size < 4) return@mapNotNull null
            runCatching {
                p[0] to Record(
                    p[1].split(",").filter { it.isNotBlank() }.map { it.toLong() },
                    p[2].toInt(),
                    p[3].toLong(),
                )
            }.getOrNull()
        }.toMap()
}

val LocalStory = staticCompositionLocalOf<StoryProgress> { error("No StoryProgress provided") }
