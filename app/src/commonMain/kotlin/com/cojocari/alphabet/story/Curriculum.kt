package com.cojocari.alphabet.story

import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.Animals
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.i18n.Language
import kotlin.random.Random

const val ChapterCount = 7

/** Eight lessons and Hupi's show at the end of each chapter. */
const val ScenesPerChapter = 9

/** Lessons per chapter (the scenes before the show). */
const val LessonsPerChapter = ScenesPerChapter - 1
const val TotalScenes = ChapterCount * ScenesPerChapter

/** Chapters 1 and 2 are free; the rest come with the unlock. */
const val FreeScenes = 2 * ScenesPerChapter

fun chapterOf(scene: Int) = scene / ScenesPerChapter

/**
 * A world's own story path: only the chapters about that subject, played on their own,
 * without the magpie plot and without Hupi growing up (that stays in the big adventure).
 * Progress is shared: a chapter finished in the big adventure is open in the path too.
 */
object Paths {
    val Letters = listOf(Subject.LettersRiver, Subject.LettersLibrary)
    val Numbers = listOf(Subject.Numbers)
    val Reading = listOf(Subject.Reading)
    val Writing = listOf(Subject.Writing)
    val Math = listOf(Subject.Math)
    val Art = listOf(Subject.ColorsShapes)
    val all = listOf(Letters, Numbers, Reading, Writing, Math, Art)
}

/** The scenes of a path, in order. */
fun pathScenes(path: List<Int>) = path.flatMap { c -> (0 until ScenesPerChapter).map { c * ScenesPerChapter + it } }

/** Free path scenes: everything in the free chapters, and the first lesson of every other path. */
fun pathFree(scene: Int, path: List<Int>) = scene < FreeScenes || scene == path.first() * ScenesPerChapter
fun isShow(scene: Int) = scene % ScenesPerChapter == ScenesPerChapter - 1

/** Index of each chapter's subject, used by the scene builder. */
object Subject {
    const val ColorsShapes = 0
    const val Numbers = 1
    const val LettersRiver = 2
    const val LettersLibrary = 3
    const val Math = 4
    const val Writing = 5
    const val Reading = 6
}

/** The animal friend of each lesson, by chapter (the last scene, Hupi's show, has the whole cast). */
private val Friends = listOf(
    listOf("bird", "bee", "mouse", "frog", "cricket", "owl", "snake", "bear"),
    listOf("cow", "pig", "sheep", "hen", "dog", "goat", "horse", "rooster"),
    listOf("duck", "frog", "bear", "cat", "horse", "mouse", "snake", "dog"),
    listOf("owl", "mouse", "cat", "parrot", "monkey", "elephant", "bird", "dog"),
    listOf("donkey", "rooster", "chick", "pig", "goat", "horse", "hen", "cow"),
    listOf("wolf", "bear", "goat", "sheep", "bird", "cricket", "tiger", "owl"),
    listOf("elephant", "lion", "monkey", "parrot", "tiger", "dolphin", "whale", "cat"),
)

/**
 * The treasure in each lesson's chest (by chapter, then lesson), things from that place.
 * They fill the treasure shelf in Hupi's house. Only emoji every supported device can draw.
 */
val Treasures = listOf(
    listOf("🌸", "🍄", "🦋", "🐞", "🌈", "🍀", "🌻", "🌰"),
    listOf("🥕", "🌽", "🥚", "🍯", "🧀", "🍓", "🌾", "🐣"),
    listOf("🐚", "🐠", "🦀", "⛵", "🐢", "💧", "🎣", "🦆"),
    listOf("📕", "🔖", "🖍️", "🔭", "🗺️", "📜", "🧩", "🎨"),
    listOf("🍎", "🍌", "🍉", "🍒", "🧁", "🍭", "🎈", "🍪"),
    listOf("❄️", "⛄", "🧣", "🛷", "💎", "🔔", "🌙", "🦔"),
    listOf("✏️", "📏", "🎒", "🏅", "🧸", "🎺", "🔬", "🌍"),
)

/** The treasure won in [scene], or null for a show. */
fun treasureOf(scene: Int): String? = Treasures[chapterOf(scene)].getOrNull(scene % ScenesPerChapter)

/** The lesson of each chapter where the magpie swoops in and must be chased away. */
const val MagpieLesson = 3

fun friendOf(scene: Int): Animal? {
    val key = Friends[chapterOf(scene)].getOrNull(scene % ScenesPerChapter) ?: return null
    return Animals.firstOrNull { it.key == key }
}

/** The chapter's cast, for Hupi's show. */
fun castOf(chapter: Int): List<Animal> = Friends[chapter].mapNotNull { k -> Animals.firstOrNull { it.key == k } }

private fun letter(language: Language, c: Char) = Item(Kind.Letter, "${language.name}/$c")
private fun number(n: Int) = Item(Kind.Number, "$n")
private fun color(i: Int) = Item(Kind.Color, "$i")
private fun shape(i: Int) = Item(Kind.Shape, "$i")
private fun math(key: String) = Item(Kind.Math, key)

/** Letters little ones start with: no Q, W, X, Y (few words start with them). */
private fun easyLetters(language: Language) =
    language.alphabet.filter { !it.inside && it.char !in "QWXY" }.map { it.char }

/** Part [part] of [all] cut into [parts] nearly equal pieces, in order. */
private fun <T> slice(all: List<T>, part: Int, parts: Int) = all.subList(part * all.size / parts, (part + 1) * all.size / parts)

/** The whole alphabet spread over the 16 lessons of chapters 3 and 4, at every level. */
private fun letterLesson(language: Language, lesson: Int): List<Char> =
    slice(language.alphabet.map { it.char }, lesson, 2 * LessonsPerChapter)

/** New things taught in a lesson ([scene] must not be a show). */
fun lessonItems(scene: Int, level: Difficulty, language: Language): List<Item> {
    val lesson = scene % ScenesPerChapter
    return when (chapterOf(scene)) {
        // All ten colours at every level (red, yellow, blue, green, orange, purple, pink,
        // brown, black, white); the bigger ones also learn the six shapes.
        Subject.ColorsShapes -> when (level) {
            Difficulty.Easy -> listOf(listOf(0), listOf(2), listOf(4), listOf(3), listOf(1), listOf(5), listOf(6, 7), listOf(8, 9))[lesson].map(::color)
            else -> listOf(
                listOf(color(0), color(2)), listOf(color(4), color(3)), listOf(color(1), color(5)), listOf(color(6), color(7)),
                listOf(color(8), color(9)), listOf(shape(0), shape(1)), listOf(shape(2), shape(3)), listOf(shape(4), shape(5)),
            )[lesson]
        }
        Subject.Numbers -> when (level) {
            Difficulty.Easy -> listOf(number(1), number(2), number(3), number(4), number(5), number(0), math("count:5"), math("more:5"))
                .let { listOf(it[lesson]) }
            Difficulty.Medium -> listOf(
                listOf(number(1), number(2)), listOf(number(3), number(4)), listOf(number(5), number(6)), listOf(number(7), number(8)),
                listOf(number(9), number(10)), listOf(number(0)), listOf(math("count:10")), listOf(math("compare:10")),
            )[lesson]
            Difficulty.Hard -> listOf(
                listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9), listOf(10, 0), listOf(11, 12), listOf(13, 14, 15),
                listOf(16, 17, 18), listOf(19, 20),
            )[lesson].map(::number)
        }
        Subject.LettersRiver -> letterLesson(language, lesson).map { letter(language, it) }
        Subject.LettersLibrary -> letterLesson(language, lesson + LessonsPerChapter).map { letter(language, it) }
        Subject.Math -> listOf(
            level.pick(
                listOf("count:5", "more:5", "add:3", "count:5", "more:5", "add:3", "count:5", "add:5"),
                listOf("count:10", "add:5", "sub:5", "compare:10", "add:10", "sub:10", "add:10", "sub:10"),
                listOf("add:10", "sub:10", "compare:10", "add:20", "sub:20", "add:20", "sub:20", "compare:10"),
            )[lesson]
        ).map(::math)
        // Numbers and letters take turns: Easy one at a time, Medium two, Hard three.
        Subject.Writing -> {
            val size = level.pick(1, 2, 3)
            val turn = lesson / 2
            if (lesson % 2 == 0) {
                val digits = (1..9).toList() + 0
                digits.drop(turn * size).take(size).map(::number)
            } else {
                language.alphabet.drop(turn * size).take(size).map { letter(language, it.char) }
            }
        }
        else -> when (level) {
            // Little ones: which letter does the picture start with? Letters from all over the alphabet.
            Difficulty.Easy -> listOf(letter(language, slice(easyLetters(language), lesson, LessonsPerChapter).first()))
            Difficulty.Medium -> slice(language.words.filter { it.text.length <= 4 }, lesson, LessonsPerChapter)
                .map { Item(Kind.Word, "${language.name}/${it.text}") }
            Difficulty.Hard -> slice(language.words, lesson, LessonsPerChapter)
                .map { Item(Kind.Word, "${language.name}/${it.text}") }
        }
    }
}

/** Everything taught in a chapter. */
fun chapterItems(chapter: Int, level: Difficulty, language: Language): List<Item> =
    (0 until LessonsPerChapter).flatMap { lessonItems(chapter * ScenesPerChapter + it, level, language) }.distinct()

// ---------------------------------------------------------------- scene steps

enum class AskMode { Together, Normal, Forgot, Warmup }

/** One beat of a scene. */
sealed interface Step {
    class Narrate(val text: String, val mood: Mood = Mood.Happy, val friend: Animal? = null, val sound: Boolean = false) : Step
    data object Hatch : Step
    class Meet(val item: Item) : Step
    class Ask(val item: Item, val mode: AskMode, val firstLetter: Boolean = false) : Step
    class Teach(val item: Item, val firstLetter: Boolean = false) : Step
    class Catch(val item: Item) : Step
    class Trace(val item: Item) : Step
    class ShowOff(val items: List<Item>) : Step
    /** The magpie flies around with something shiny; tapping her makes her drop [item]. */
    class Magpie(val item: Item) : Step
    /** The friend's thanks; a lesson's treasure chest ([treasure]); Hupi growing up after a show ([stage]). */
    class Reward(val friend: Animal?, val stage: Int?, val treasure: String? = null) : Step
    data object Diploma : Step
}

private fun catchable(item: Item) = item.kind == Kind.Letter || item.kind == Kind.Number || item.kind == Kind.Color

/**
 * The steps of a scene. A lesson goes: story intro, the friend's problem, meet each new thing
 * (Hupi gets it funny-wrong), find it together (Hupi points; skipped on Hard), find it alone,
 * sometimes catch it among floating tiles, teach Hupi (he guesses, the child judges),
 * remind Hupi of something learned earlier, then the friend's thanks and a treasure chest.
 * Once a chapter (lesson [MagpieLesson]) the magpie swoops in and must be tapped away.
 * The last scene of a chapter is Hupi's show: a mixed review, Hupi performing, a new item
 * for Hupi and the next clue about the magpie.
 */
fun buildScene(
    scene: Int,
    language: Language,
    level: Difficulty,
    story: StoryProgress,
    premium: Boolean,
    rnd: Random = Random.Default,
    path: List<Int>? = null,
): List<Step> {
    val s = language.story
    val chapter = chapterOf(scene)
    val steps = mutableListOf<Step>()

    if (isShow(scene)) {
        steps += Step.Narrate(s.showIntro, Mood.Happy)
        val all = chapterItems(chapter, level, language)
        // Practise what isn't learned yet first.
        val review = all.sortedBy { story.status(it) == Status.Learned }.take(level.pick(4, 5, 6)).shuffled(rnd)
        review.forEach { steps += Step.Ask(it, AskMode.Normal, firstLetter = chapter == Subject.Reading && it.kind == Kind.Letter) }
        steps += Step.ShowOff(all.filter { it.kind != Kind.Math }.take(8).ifEmpty { review })
        if (path != null) {
            // A world's path: no growing up, no magpie; just the applause and, at the very end, well done.
            steps += Step.Reward(null, null)
            if (chapter == path.last()) steps += Step.Narrate(s.pathDone, Mood.Happy)
            else if (!premium && !pathFree(scene + 1, path)) steps += Step.Narrate(s.pathLocked, Mood.Normal)
            return steps
        }
        steps += Step.Reward(null, chapter + 1)
        if (chapter < ChapterCount - 1) {
            steps += Step.Narrate(s.cliffhanger(chapter), Mood.Surprised)
            if (scene + 1 == FreeScenes && !premium) steps += Step.Narrate(s.paywall, Mood.Normal)
        } else {
            steps += Step.Narrate(s.finale, Mood.Happy)
            steps += Step.Diploma
        }
        return steps
    }

    val friend = friendOf(scene)
    if (scene == 0 && path == null) {
        steps += Step.Hatch
        steps += Step.Narrate(s.magpieStory, Mood.Surprised)
    }
    if (scene % ScenesPerChapter == 0) {
        steps += if (path == null) Step.Narrate(s.chapterIntro(chapter), Mood.Puzzled) else Step.Narrate(s.pathIntro(chapter), Mood.Happy)
    }
    val lesson = scene % ScenesPerChapter
    if (friend != null) steps += Step.Narrate(s.friendAsk(chapter, lesson, s.friendName(friend)), Mood.Normal, friend, sound = true)

    val items = lessonItems(scene, level, language)
    val firstLetter = chapter == Subject.Reading && level == Difficulty.Easy
    when (chapter) {
        Subject.Writing -> {
            items.forEach { steps += Step.Meet(it); steps += Step.Trace(it) }
            items.forEach { steps += Step.Ask(it, AskMode.Normal) }
        }
        Subject.Math -> {
            val skill = items.first()
            steps += Step.Meet(skill)
            steps += Step.Ask(skill, AskMode.Together)
            repeat(level.pick(2, 3, 3)) { steps += Step.Ask(skill, AskMode.Normal) }
            if (!skill.key.startsWith("more") && !skill.key.startsWith("compare")) steps += Step.Teach(skill)
        }
        else -> {
            items.forEach {
                steps += Step.Meet(it)
                if (level != Difficulty.Hard) steps += Step.Ask(it, AskMode.Together, firstLetter)
            }
            items.shuffled(rnd).forEach { steps += Step.Ask(it, AskMode.Normal, firstLetter) }
            val catchItem = items.firstOrNull { catchable(it) && !firstLetter }?.takeIf { scene % 2 == 0 }
            if (catchItem != null) steps += Step.Catch(catchItem)
            // Little ones get one Hupi game per lesson (catch on even scenes, teach on odd ones),
            // so their lessons stay short; Medium and Hard get both.
            if (level != Difficulty.Easy || catchItem == null) items.randomOrNull(rnd)?.let { steps += Step.Teach(it, firstLetter) }
        }
    }

    // "Hupi forgot…": something met in an earlier scene that is due for review
    // (in a world's path, only things of that path's kind).
    val current = items.map { it.id }.toSet()
    val kinds = path?.flatMap { chapterItems(it, level, language) }?.map { it.kind }?.toSet()
    story.due { it.id !in current && it.belongsTo(language) && (kinds == null || it.kind in kinds) }.take(level.pick(1, 1, 2)).forEach {
        steps += Step.Ask(it, AskMode.Forgot)
    }
    // Once a chapter, in the big adventure, the magpie swoops in with one of today's things.
    if (path == null && lesson == MagpieLesson) items.firstOrNull()?.let { steps += Step.Magpie(it) }
    steps += Step.Reward(friend, null, treasureOf(scene))
    if (path != null && !premium && scene + 1 in pathScenes(path) && !pathFree(scene + 1, path)) {
        steps += Step.Narrate(s.pathLocked, Mood.Normal)
    }
    return steps
}

/**
 * Letters and words belong to one language; other things to every language. Letters no longer
 * taught (Ă, Â, Î, Ș, Ț, left in old saved progress) belong to none, so they don't come back.
 */
fun Item.belongsTo(language: Language) = when (kind) {
    Kind.Letter -> key.startsWith("${language.name}/") && letter(language) != null
    Kind.Word -> key.startsWith("${language.name}/") && word(language) != null
    else -> true
}

/** The morning warm-up: three quick questions about things due for review. */
fun buildWarmup(language: Language, story: StoryProgress, rnd: Random = Random.Default): List<Step> {
    val s = language.story
    val due = story.due { it.belongsTo(language) }
    val pool = (due + story.met().filter { it.belongsTo(language) }.shuffled(rnd)).distinct().take(3)
    if (pool.isEmpty()) return emptyList()
    return buildList {
        add(Step.Narrate(s.warmupIntro, Mood.Sleepy))
        pool.forEach { add(Step.Ask(it, AskMode.Warmup)) }
        add(Step.Narrate(s.warmupDone, Mood.Happy))
    }
}
