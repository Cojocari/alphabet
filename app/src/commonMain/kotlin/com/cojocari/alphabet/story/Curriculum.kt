package com.cojocari.alphabet.story

import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.Animals
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.i18n.Language
import kotlin.random.Random

const val ChapterCount = 7

/** Six lessons and Hupi's show at the end of each chapter. */
const val ScenesPerChapter = 7
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
    listOf("bird", "bee", "mouse", "frog", "cricket", "owl"),
    listOf("cow", "pig", "sheep", "hen", "dog", "goat"),
    listOf("duck", "frog", "bear", "cat", "horse", "mouse"),
    listOf("owl", "mouse", "cat", "parrot", "monkey", "elephant"),
    listOf("donkey", "rooster", "chick", "pig", "goat", "horse"),
    listOf("wolf", "bear", "goat", "sheep", "bird", "cricket"),
    listOf("elephant", "lion", "monkey", "parrot", "tiger", "dolphin"),
)

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

/** Letters little ones start with: no Q, W, X, Y and no Ă, Â (they never start a word). */
private fun easyLetters(language: Language) =
    language.alphabet.filter { !it.inside && it.char !in "QWXY" }.map { it.char }

/** The whole alphabet spread over 12 lessons (chapters 3 and 4). */
private fun letterLesson(language: Language, level: Difficulty, lesson: Int): List<Char> {
    if (level == Difficulty.Easy) return listOfNotNull(easyLetters(language).getOrNull(lesson))
    val all = language.alphabet.map { it.char }
    return all.subList(lesson * all.size / 12, (lesson + 1) * all.size / 12)
}

/** New things taught in a lesson ([scene] must not be a show). */
fun lessonItems(scene: Int, level: Difficulty, language: Language): List<Item> {
    val lesson = scene % ScenesPerChapter
    return when (chapterOf(scene)) {
        Subject.ColorsShapes -> when (level) {
            // Red, yellow, blue, green, orange, purple.
            Difficulty.Easy -> listOf(color(listOf(0, 2, 4, 3, 1, 5)[lesson]))
            else -> listOf(
                listOf(color(0), color(2)), listOf(color(4), color(3)), listOf(color(1), color(5)),
                listOf(shape(0), shape(1)), listOf(shape(2), shape(3)), listOf(shape(4), shape(5)),
            )[lesson]
        }
        Subject.Numbers -> when (level) {
            Difficulty.Easy -> if (lesson < 5) listOf(number(lesson + 1)) else listOf(math("count:5"))
            Difficulty.Medium -> listOf(listOf(1, 2), listOf(3, 4), listOf(5, 6), listOf(7, 8), listOf(9, 10), listOf(0))[lesson].map(::number)
            Difficulty.Hard -> listOf(
                listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9), listOf(10, 11, 12), listOf(13, 14, 15, 16), listOf(17, 18, 19, 20),
            )[lesson].map(::number)
        }
        Subject.LettersRiver -> letterLesson(language, level, lesson).map { letter(language, it) }
        Subject.LettersLibrary -> letterLesson(language, level, lesson + 6).map { letter(language, it) }
        Subject.Math -> listOf(
            level.pick(
                listOf("count:5", "more:5", "add:3", "count:5", "more:5", "add:3"),
                listOf("count:10", "add:5", "sub:5", "compare:10", "add:10", "sub:10"),
                listOf("add:10", "sub:10", "compare:10", "add:20", "sub:20", "add:20"),
            )[lesson]
        ).map(::math)
        Subject.Writing -> {
            val letters = easyLetters(language)
            when (level) {
                Difficulty.Easy -> listOf(number(1), number(2), number(3), letter(language, letters[0]), letter(language, letters[1]), letter(language, letters[2]))
                    .let { listOf(it[lesson]) }
                Difficulty.Medium -> listOf(
                    listOf(number(1), number(2)), listOf(number(3), number(4)), listOf(number(5), letter(language, letters[0])),
                    listOf(letter(language, letters[1]), letter(language, letters[2])), listOf(letter(language, letters[3]), letter(language, letters[4])),
                    listOf(letter(language, letters[5]), letter(language, letters[6])),
                )[lesson]
                Difficulty.Hard -> {
                    val n = listOf(1..3, 4..6, 7..9)
                    if (lesson < 3) n[lesson].map(::number)
                    else letters.subList((lesson - 3) * 3, (lesson - 3) * 3 + 3).map { letter(language, it) }
                }
            }
        }
        else -> when (level) {
            // Little ones: which letter does the picture start with?
            Difficulty.Easy -> listOf(letter(language, easyLetters(language)[lesson]))
            Difficulty.Medium -> language.words.filter { it.text.length <= 4 }.chunked(2).getOrElse(lesson) { emptyList() }
                .map { Item(Kind.Word, "${language.name}/${it.text}") }
            Difficulty.Hard -> language.words.chunked(3).getOrElse(lesson) { emptyList() }
                .map { Item(Kind.Word, "${language.name}/${it.text}") }
        }
    }
}

/** Everything taught in a chapter. */
fun chapterItems(chapter: Int, level: Difficulty, language: Language): List<Item> =
    (0 until ScenesPerChapter - 1).flatMap { lessonItems(chapter * ScenesPerChapter + it, level, language) }.distinct()

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
    class Reward(val friend: Animal?, val stage: Int?) : Step
    data object Diploma : Step
}

private fun catchable(item: Item) = item.kind == Kind.Letter || item.kind == Kind.Number || item.kind == Kind.Color

/**
 * The steps of a scene. A lesson goes: story intro, the friend's problem, meet each new thing
 * (Hupi gets it funny-wrong), find it together (Hupi points; skipped on Hard), find it alone,
 * sometimes catch it among floating tiles, teach Hupi (he guesses, the child judges),
 * remind Hupi of something learned earlier, then the friend's thanks.
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
    if (friend != null) steps += Step.Narrate(s.friendAsk(chapter, s.friendName(friend)), Mood.Normal, friend, sound = true)

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
            // One Hupi game per lesson, to keep lessons short: catching him on even scenes,
            // teaching him on odd ones (or when nothing can be caught).
            val catchItem = items.firstOrNull { catchable(it) && !firstLetter }?.takeIf { scene % 2 == 0 }
            if (catchItem != null) steps += Step.Catch(catchItem)
            else items.randomOrNull(rnd)?.let { steps += Step.Teach(it, firstLetter) }
        }
    }

    // "Hupi forgot…": something met in an earlier scene that is due for review
    // (in a world's path, only things of that path's kind).
    val current = items.map { it.id }.toSet()
    val kinds = path?.flatMap { chapterItems(it, level, language) }?.map { it.kind }?.toSet()
    story.due { it.id !in current && it.belongsTo(language) && (kinds == null || it.kind in kinds) }.take(level.pick(1, 1, 2)).forEach {
        steps += Step.Ask(it, AskMode.Forgot)
    }
    steps += Step.Reward(friend, null)
    if (path != null && !premium && scene + 1 in pathScenes(path) && !pathFree(scene + 1, path)) {
        steps += Step.Narrate(s.pathLocked, Mood.Normal)
    }
    return steps
}

/**
 * Letters and words belong to one language; other things to every language. Letters no longer
 * taught (Ă, Â, Î, Ș, Ț, left in old saved progress) belong to none, so they don't come back.
 */
fun Item.belongsTo(language: Language) =
    (kind != Kind.Letter && kind != Kind.Word) ||
        (key.startsWith("${language.name}/") && (kind != Kind.Letter || letter(language) != null))

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
