package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.data.CountingThings
import com.cojocari.alphabet.data.FunColors
import com.cojocari.alphabet.data.numberColor
import com.cojocari.alphabet.data.objects
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.ui.theme.Ink
import kotlin.random.Random

/** Soft tile colours for picture answers, so the emoji stays readable. */
private val Pastels = listOf(
    Color(0xFFFFD6E0), Color(0xFFD7F2E3), Color(0xFFD6E8FF), Color(0xFFFFF1C1), Color(0xFFEBDDFF),
)

/** [n] different values from [pool] including [answer], shuffled; returns them and the answer's index. */
private fun <T> options(answer: T, pool: List<T>, n: Int): Pair<List<T>, Int> {
    val list = (listOf(answer) + pool.filter { it != answer }.distinct().shuffled().take(n - 1)).shuffled()
    return list to list.indexOf(answer)
}

/** 3 choices for beginners, 4 once the child has a few stars in this game. */
private fun choiceCount(level: Int) = if (level < 6) 3 else 4

// ---------------------------------------------------------------- Letters

fun firstLetterRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val pool = language.alphabet.filterNot { it.inside }
    val target = pool.random()
    val (choices, answer) = options(target, pool, choiceCount(level))
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
            Text(
                if (solved) target.word else " ",
                fontSize = 44.sp, fontWeight = FontWeight.Black, color = target.color,
            )
        }
    }
}

// ---------------------------------------------------------------- Numbers

fun findNumberRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val max = if (level < 6) 10 else 20
    val target = Random.nextInt(0, max + 1)
    val (choices, answer) = options(target, (0..max).toList(), choiceCount(level))
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

fun howManyRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val max = (3 + level / 2).coerceAtMost(10)
    val target = Random.nextInt(1, max + 1)
    val thing = CountingThings.random()
    val (choices, answer) = options(target, (1..10).toList(), 3)
    return PickRound(
        ask = t.countAsk,
        choices = choices.map { Choice("$it", numberColor(it)) },
        answer = answer,
        right = { t.countRight(it, target) },
        wrong = { t.countWrong(choices[it]) },
    ) { _ -> CountingField(thing, target) }
}

// ---------------------------------------------------------------- Reading

fun readPictureRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val target = language.words.random()
    val (choices, answer) = options(target, language.words, choiceCount(level))
    return PickRound(
        ask = t.readAsk,
        choices = choices.mapIndexed { i, w -> Choice(w.emoji, Pastels[i % Pastels.size], 80.sp) },
        answer = answer,
        right = { t.readRight(it, target.text) },
        wrong = { t.tryAgain },
    ) { solved ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(target.text.uppercase(), fontSize = 76.sp, fontWeight = FontWeight.Black, color = Color(0xFF219EBC))
            Text(target.text, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(if (solved) target.emoji else " ", fontSize = 80.sp)
        }
    }
}

fun whichWordRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val target = language.words.random()
    val (choices, answer) = options(target, language.words, choiceCount(level))
    return PickRound(
        ask = t.whichWordAsk(target.text),
        choices = choices.mapIndexed { i, w ->
            Choice(w.text.uppercase(), FunColors[(i * 3) % FunColors.size], if (w.text.length > 4) 30.sp else 38.sp)
        },
        answer = answer,
        right = { t.whichWordRight(it, target.text) },
        wrong = { t.tryAgain },
    ) { _ -> Text(target.emoji, fontSize = 170.sp) }
}

// ---------------------------------------------------------------- Math

/** Up to 5 while learning, then up to 10. */
private fun mathMax(level: Int) = if (level < 6) 5 else 10

fun addRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val max = mathMax(level)
    val a = Random.nextInt(1, max)
    val b = Random.nextInt(1, max - a + 1)
    val thing = CountingThings.random()
    val (choices, answer) = options(a + b, (1..10).toList(), 3)
    return PickRound(
        ask = t.addAsk(a, b),
        choices = choices.map { Choice("$it", numberColor(it)) },
        answer = answer,
        right = { t.addRight(it, a, b) },
        wrong = { t.mathWrong },
    ) { solved -> Sum(thing, a, b, "+", if (solved) "${a + b}" else "?", crossed = false) }
}

fun subtractRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val max = mathMax(level)
    val a = Random.nextInt(2, max + 1)
    val b = Random.nextInt(1, a)
    val thing = CountingThings.random()
    val (choices, answer) = options(a - b, (0..9).toList(), 3)
    return PickRound(
        ask = t.subAsk(a, b),
        choices = choices.map { Choice("$it", numberColor(it)) },
        answer = answer,
        right = { t.subRight(it, a, b) },
        wrong = { t.mathWrong },
    ) { solved -> Sum(thing, a, b, "−", if (solved) "${a - b}" else "?", crossed = true) }
}

fun compareRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val max = if (level < 6) 5 else 9
    val x = Random.nextInt(1, max + 1)
    val y = generateSequence { Random.nextInt(1, max + 1) }.first { it != x }
    val askMore = level < 4 || Random.nextBoolean()
    val thing = CountingThings.random()
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

/** "🍎🍎 + 🍎 = ?" with the numbers under each group. Subtraction crosses out the taken ones. */
@androidx.compose.runtime.Composable
private fun Sum(thing: String, a: Int, b: Int, sign: String, result: String, crossed: Boolean) {
    val emojiSize = if (a + b > 6) 34.sp else 46.sp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (crossed) {
            // All `a` things; the last `b` are crossed out.
            Group(objects(thing, a - b), a, emojiSize, extra = objects("❌", b))
        } else {
            Group(objects(thing, a), a, emojiSize)
            Symbol(sign)
            Group(objects(thing, b), b, emojiSize)
        }
        if (crossed) {
            Symbol(sign)
            Symbol("$b", numberColor(b))
        }
        Symbol("=")
        Symbol(result, Color(0xFF43AA8B))
    }
}

@androidx.compose.runtime.Composable
private fun Group(things: String, n: Int, size: androidx.compose.ui.unit.TextUnit, extra: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(things, fontSize = size, textAlign = TextAlign.Center, lineHeight = size * 1.2f)
        if (extra != null) Text(extra, fontSize = size, textAlign = TextAlign.Center, lineHeight = size * 1.2f)
        Text("$n", fontSize = 56.sp, fontWeight = FontWeight.Black, color = numberColor(n))
    }
}

@androidx.compose.runtime.Composable
private fun Symbol(text: String, color: Color = Ink) {
    Text(text, fontSize = 64.sp, fontWeight = FontWeight.Black, color = color)
}

// ---------------------------------------------------------------- Colours & shapes

fun colorRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val colors = com.cojocari.alphabet.data.ArtColors
    // Start with the six main colours, then add brown, black and white.
    val pool = colors.indices.toList().take(if (level < 5) 6 else colors.size)
    val target = pool.random()
    val (choices, answer) = options(target, pool, choiceCount(level))
    return PickRound(
        ask = t.findColor(t.colorNames[target]),
        choices = choices.map { Choice("", colors[it].color) },
        answer = answer,
        right = { t.rightColor(it, t.colorNames[target]) },
        wrong = { t.wrongColor(t.colorNames[choices[it]]) },
    ) { solved -> Text(if (solved) colors[target].example else "🎨", fontSize = 160.sp) }
}

fun shapeRound(language: Language, level: Int): PickRound {
    val t = language.strings
    val shapes = com.cojocari.alphabet.data.Shapes
    val pool = shapes.indices.toList().take(if (level < 5) 4 else shapes.size)
    val target = pool.random()
    val (choices, answer) = options(target, pool, choiceCount(level))
    // Different colours on each tile, so the child looks at the shape, not the colour.
    val tileColors = FunColors.shuffled()
    return PickRound(
        ask = t.shapeFind[target],
        choices = choices.mapIndexed { i, s -> Choice(shapes[s].glyph, tileColors[i % tileColors.size], 110.sp) },
        answer = answer,
        right = { "$it ${t.shapeIs[target]}" },
        wrong = { "${t.shapeIs[choices[it]]} ${t.tryAgainShort}" },
    ) { solved -> Text(if (solved) shapes[target].example else "👀", fontSize = 160.sp) }
}
