package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.data.ArtColors
import com.cojocari.alphabet.data.CountingThings
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.data.FunColors
import com.cojocari.alphabet.data.Shapes
import com.cojocari.alphabet.data.numberColor
import com.cojocari.alphabet.data.objects
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.ui.theme.Ink
import kotlin.random.Random

/*
 * Question generators for the PickGame engine. Each one turns the chosen level
 * (Easy / Medium / Hard) into its own settings, so every round is freshly generated.
 */

/** Soft tile colours for picture answers, so the emoji stays readable. */
private val Pastels = listOf(
    Color(0xFFFFD6E0), Color(0xFFD7F2E3), Color(0xFFD6E8FF), Color(0xFFFFF1C1), Color(0xFFEBDDFF),
)

/** [n] different values from [pool] including [answer], shuffled; returns them and the answer's index. */
private fun <T> options(answer: T, pool: List<T>, n: Int, rnd: Random): Pair<List<T>, Int> {
    val list = (listOf(answer) + pool.filter { it != answer }.distinct().shuffled(rnd).take(n - 1)).shuffled(rnd)
    return list to list.indexOf(answer)
}

/** Easy: 2 choices, Medium: 3, Hard: 4. */
private fun choices(d: Difficulty) = d.pick(2, 3, 4)

/** Avoids asking the same thing twice in a row. */
private val lastAsked = mutableMapOf<String, Any?>()
private fun <T> fresh(game: String, pool: List<T>, rnd: Random): T {
    // Demos use their own fixed sequence, so they are the same every time.
    if (rnd !== Random.Default) return pool.random(rnd)
    val pick = pool.filter { it != lastAsked[game] }.ifEmpty { pool }.random(rnd)
    lastAsked[game] = pick
    return pick
}

// ---------------------------------------------------------------- Letters

fun firstLetterRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = language.alphabet.filterNot { it.inside }
    val target = fresh("first", pool, rnd)
    val (choices, answer) = options(target, pool, choices(d), rnd)
    return PickRound(
        ask = t.pictureAsk(target),
        choices = choices.map { Choice(it.char.toString(), it.color) },
        answer = answer,
        right = { t.pictureRight(it, target) },
        wrong = { t.pictureWrong(choices[it]) },
        learned = target.char,
    ) { solved ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(target.emoji, fontSize = 150.sp)
            // Easy shows the word straight away as a hint; otherwise only once solved.
            Text(
                if (solved || d == Difficulty.Easy) target.word else " ",
                fontSize = 44.sp, fontWeight = FontWeight.Black, color = target.color,
            )
        }
    }
}

// ---------------------------------------------------------------- Numbers

fun findNumberRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = d.pick((1..5).toList(), (0..10).toList(), (0..20).toList())
    val target = fresh("findNumber", pool, rnd)
    val (choices, answer) = options(target, pool, choices(d), rnd)
    return PickRound(
        ask = t.findNumber(target),
        choices = choices.map { Choice("$it", numberColor(it), if (it >= 10) 72.sp else 96.sp) },
        answer = answer,
        right = { t.rightNumber(it, target) },
        wrong = { t.wrongNumber(choices[it]) },
    ) { solved ->
        if (solved && target in 1..10) {
            Text(objects(CountingThings[target % CountingThings.size], target), fontSize = 54.sp, textAlign = TextAlign.Center, lineHeight = 66.sp)
        } else {
            Text(if (solved) "$target" else "👂", fontSize = 150.sp, fontWeight = FontWeight.Black, color = numberColor(target))
        }
    }
}

fun howManyRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = d.pick((1..3).toList(), (1..6).toList(), (1..10).toList())
    val target = fresh("howMany", pool, rnd)
    val thing = CountingThings.random(rnd)
    val (choices, answer) = options(target, d.pick((1..4).toList(), (1..7).toList(), (1..10).toList()), choices(d) + 1, rnd)
    return PickRound(
        ask = t.countAsk,
        choices = choices.map { Choice("$it", numberColor(it)) },
        answer = answer,
        right = { t.countRight(it, target) },
        wrong = { t.countWrong(choices[it]) },
    ) { _ -> CountingField(thing, target) }
}

// ---------------------------------------------------------------- Reading

/** Easy: words up to 3 letters, Medium: up to 4, Hard: all. */
private fun readingWords(language: Language, d: Difficulty) =
    language.words.filter { it.text.length <= d.pick(3, 4, 99) }.ifEmpty { language.words }

fun readPictureRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = readingWords(language, d)
    val target = fresh("read", pool, rnd)
    val (choices, answer) = options(target, language.words, choices(d), rnd)
    return PickRound(
        ask = t.readAsk,
        choices = choices.mapIndexed { i, w -> Choice(w.emoji, Pastels[i % Pastels.size], 80.sp) },
        answer = answer,
        right = { t.readRight(it, target.text) },
        wrong = { t.tryAgain },
    ) { solved ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Hard shows only small letters; easier levels show capitals too.
            if (d != Difficulty.Hard) {
                Text(target.text.uppercase(), fontSize = 76.sp, fontWeight = FontWeight.Black, color = Color(0xFF219EBC))
            }
            Text(target.text, fontSize = if (d == Difficulty.Hard) 76.sp else 52.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(if (solved) target.emoji else " ", fontSize = 80.sp)
        }
    }
}

fun whichWordRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = readingWords(language, d)
    val target = fresh("whichWord", pool, rnd)
    val (choices, answer) = options(target, pool, choices(d), rnd)
    return PickRound(
        ask = t.whichWordAsk(target.text),
        choices = choices.mapIndexed { i, w ->
            val text = if (d == Difficulty.Hard) w.text else w.text.uppercase()
            Choice(text, FunColors[(i * 3) % FunColors.size], if (w.text.length > 4) 30.sp else 38.sp)
        },
        answer = answer,
        right = { t.whichWordRight(it, target.text) },
        wrong = { t.tryAgain },
    ) { _ -> Text(target.emoji, fontSize = 170.sp) }
}

// ---------------------------------------------------------------- Math

/** Biggest number in sums: Easy 5, Medium 10, Hard 20. */
private fun mathMax(d: Difficulty) = d.pick(5, 10, 20)

/** Wrong answers close to the right one, so children can't guess by size. */
private fun nearby(answer: Int, max: Int) = ((answer - 3)..(answer + 3)).filter { it in 0..max }

fun addRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val max = mathMax(d)
    val a = rnd.nextInt(1, max)
    val b = rnd.nextInt(1, max - a + 1)
    val thing = CountingThings.random(rnd)
    val (choices, answer) = options(a + b, nearby(a + b, max), choices(d) + 1, rnd)
    return PickRound(
        ask = t.addAsk(a, b),
        choices = choices.map { Choice("$it", numberColor(it), if (it >= 10) 72.sp else 88.sp) },
        answer = answer,
        right = { t.addRight(it, a, b) },
        wrong = { t.mathWrong },
    ) { solved -> Sum(thing, a, b, "+", if (solved) "${a + b}" else "?", crossed = false, showThings = a + b <= 10) }
}

fun subtractRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val max = mathMax(d)
    val a = rnd.nextInt(2, max + 1)
    val b = rnd.nextInt(1, a)
    val thing = CountingThings.random(rnd)
    val (choices, answer) = options(a - b, nearby(a - b, max), choices(d) + 1, rnd)
    return PickRound(
        ask = t.subAsk(a, b),
        choices = choices.map { Choice("$it", numberColor(it), if (it >= 10) 72.sp else 88.sp) },
        answer = answer,
        right = { t.subRight(it, a, b) },
        wrong = { t.mathWrong },
    ) { solved -> Sum(thing, a, b, "−", if (solved) "${a - b}" else "?", crossed = true, showThings = a <= 10) }
}

fun compareRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val max = d.pick(5, 9, 10)
    val x = rnd.nextInt(1, max + 1)
    // Hard: the two groups differ by only 1 or 2, so the child really has to count.
    val y = generateSequence {
        if (d == Difficulty.Hard) x + listOf(-2, -1, 1, 2).random(rnd) else rnd.nextInt(1, max + 1)
    }.first { it != x && it in 1..max }
    // Easy only asks "more"; other levels also ask "fewer".
    val askMore = d == Difficulty.Easy || rnd.nextBoolean()
    val thing = CountingThings.random(rnd)
    val answer = if ((x > y) == askMore) 0 else 1
    return PickRound(
        ask = if (askMore) t.compareMore else t.compareFewer,
        choices = listOf(
            Choice(objects(thing, x), Pastels[0], 26.sp),
            Choice(objects(thing, y), Pastels[2], 26.sp),
        ),
        answer = answer,
        right = { t.compareRight(it, askMore) },
        wrong = { t.compareWrong },
    ) { solved ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (askMore) "⬆️" else "⬇️", fontSize = 110.sp)
            Text(if (solved) "$x  ·  $y" else "⚖️", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Ink)
        }
    }
}

/**
 * "🍎🍎 + 🍎 = ?" with the numbers under each group. Subtraction crosses out the taken
 * ones. With big numbers (Hard) the pictures are left out and only digits are shown.
 */
@Composable
private fun Sum(thing: String, a: Int, b: Int, sign: String, result: String, crossed: Boolean, showThings: Boolean) {
    val emojiSize = if (a + b > 6) 34.sp else 46.sp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!showThings) {
            Symbol("$a", numberColor(a))
            Symbol(sign)
            Symbol("$b", numberColor(b))
        } else if (crossed) {
            Group(objects(thing, a - b), a, emojiSize, extra = objects("❌", b))
            Symbol(sign)
            Symbol("$b", numberColor(b))
        } else {
            Group(objects(thing, a), a, emojiSize)
            Symbol(sign)
            Group(objects(thing, b), b, emojiSize)
        }
        Symbol("=")
        Symbol(result, Color(0xFF43AA8B))
    }
}

@Composable
private fun Group(things: String, n: Int, size: TextUnit, extra: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(things, fontSize = size, textAlign = TextAlign.Center, lineHeight = size * 1.2f)
        if (extra != null) Text(extra, fontSize = size, textAlign = TextAlign.Center, lineHeight = size * 1.2f)
        Text("$n", fontSize = 56.sp, fontWeight = FontWeight.Black, color = numberColor(n))
    }
}

@Composable
private fun Symbol(text: String, color: Color = Ink) {
    Text(text, fontSize = 64.sp, fontWeight = FontWeight.Black, color = color)
}

// ---------------------------------------------------------------- Colours & shapes

fun colorRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    // Easy: the six main colours. Medium adds brown and black, Hard adds white.
    val pool = ArtColors.indices.toList().take(d.pick(6, 8, ArtColors.size))
    val target = fresh("color", pool, rnd)
    val (choices, answer) = options(target, pool, choices(d), rnd)
    return PickRound(
        ask = t.findColor(t.colorNames[target]),
        choices = choices.map { Choice("", ArtColors[it].color) },
        answer = answer,
        right = { t.rightColor(it, t.colorNames[target]) },
        wrong = { t.wrongColor(t.colorNames[choices[it]]) },
    ) { solved -> Text(if (solved) ArtColors[target].example else "🎨", fontSize = 160.sp) }
}

fun shapeRound(language: Language, d: Difficulty, rnd: Random = Random.Default): PickRound {
    val t = language.strings
    val pool = Shapes.indices.toList().take(d.pick(4, 5, Shapes.size))
    val target = fresh("shape", pool, rnd)
    val (choices, answer) = options(target, pool, choices(d), rnd)
    // Different colours on each tile, so the child looks at the shape, not the colour.
    val tileColors = FunColors.shuffled(rnd)
    return PickRound(
        ask = t.shapeFind[target],
        choices = choices.mapIndexed { i, s -> Choice(Shapes[s].glyph, tileColors[i % tileColors.size], 110.sp) },
        answer = answer,
        right = { "$it ${t.shapeIs[target]}" },
        wrong = { "${t.shapeIs[choices[it]]} ${t.tryAgainShort}" },
    ) { solved -> Text(if (solved) Shapes[target].example else "👀", fontSize = 160.sp) }
}
