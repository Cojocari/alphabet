package com.cojocari.alphabet.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.data.ArtColors
import com.cojocari.alphabet.data.CountingThings
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.data.FunColors
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.data.Shapes
import com.cojocari.alphabet.data.Word
import com.cojocari.alphabet.data.numberColor
import com.cojocari.alphabet.data.objects
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.ui.games.Choice
import com.cojocari.alphabet.ui.games.CountingField
import com.cojocari.alphabet.ui.games.Sum
import com.cojocari.alphabet.ui.theme.Ink
import kotlin.random.Random

private val Pastels = listOf(
    Color(0xFFFFD6E0), Color(0xFFD7F2E3), Color(0xFFD6E8FF), Color(0xFFFFF1C1), Color(0xFFEBDDFF),
)

/**
 * A question about one thing to learn: what to say, the tiles to choose from and what each
 * is called (Hupi uses the names when he guesses), which one is right, and the picture.
 */
class Quest(
    val ask: String,
    val choices: List<Choice>,
    val names: List<String>,
    val answer: Int,
    val right: (praise: String) -> String,
    val wrong: (picked: Int) -> String,
    val prompt: @Composable (solved: Boolean) -> Unit,
)

fun Item.letter(language: Language): Letter? =
    if (kind != Kind.Letter) null else language.alphabet.firstOrNull { it.char == key.substringAfter('/').first() }

fun Item.word(language: Language): Word? =
    if (kind != Kind.Word) null else language.words.firstOrNull { it.text == key.substringAfter('/') }

private fun Item.int() = key.toIntOrNull() ?: 0

/** "the letter M", "the number five", "red" / "culoarea roșie", "the circle", "the word cat". */
fun Item.name(language: Language): String {
    val t = language.strings
    val s = language.story
    return when (kind) {
        Kind.Letter -> letter(language)?.let { s.letterName(it) }.orEmpty()
        Kind.Number -> s.numberName(t.num(int()))
        Kind.Color -> s.colorName(t.colorNames[int()])
        Kind.Shape -> s.shapeNames[int()]
        Kind.Word -> s.wordName(word(language)?.text.orEmpty())
        Kind.Math -> s.mathName(key)
    }
}

/** Short spoken form: "M", "five", "red", "circle", "cat". */
fun Item.short(language: Language): String {
    val t = language.strings
    return when (kind) {
        Kind.Letter -> letter(language)?.name.orEmpty()
        Kind.Number -> t.num(int())
        Kind.Color -> t.colorNames[int()]
        Kind.Shape -> language.story.shapeShort[int()]
        Kind.Word -> word(language)?.text.orEmpty()
        Kind.Math -> ""
    }
}

/** What is said when the child first meets this. */
fun Item.meetLine(language: Language): String {
    val t = language.strings
    val s = language.story
    return when (kind) {
        Kind.Letter -> letter(language)?.let { t.letterCard(it) }.orEmpty()
        Kind.Number -> s.meetNumber(t.num(int()))
        Kind.Color -> s.meetColor(t.colorNames[int()])
        Kind.Shape -> t.shapeIs[int()]
        Kind.Word -> s.meetWord(word(language)?.text.orEmpty())
        Kind.Math -> s.meetMath(key.substringBefore(':'))
    }
}

private fun <T> options(answer: T, pool: List<T>, n: Int, rnd: Random): Pair<List<T>, Int> {
    val list = (listOf(answer) + pool.filter { it != answer }.distinct().shuffled(rnd).take(n - 1)).shuffled(rnd)
    return list to list.indexOf(answer)
}

/**
 * Builds a question about [item] with [count] tiles. [firstLetter]: show a picture and ask
 * which letter it starts with (the reading chapter for little ones).
 */
fun questFor(item: Item, language: Language, level: Difficulty, count: Int, rnd: Random, firstLetter: Boolean = false): Quest {
    val t = language.strings
    val s = language.story
    return when (item.kind) {
        Kind.Letter -> {
            val l = item.letter(language) ?: language.alphabet.first()
            val pool = language.alphabet.filter { !firstLetter || !it.inside }
            val (letters, answer) = options(l, pool, count, rnd)
            Quest(
                ask = if (firstLetter) t.pictureAsk(l) else t.findLetter(l),
                choices = letters.map { Choice(it.char.toString(), it.color) },
                names = letters.map { s.letterName(it) },
                answer = answer,
                right = { if (firstLetter) t.pictureRight(it, l) else t.correct(it, l, false) },
                wrong = { if (firstLetter) t.pictureWrong(letters[it]) else t.tryAgain },
            ) { solved ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(l.emoji, fontSize = 120.sp)
                    Text(if (solved) l.word else " ", fontSize = 40.sp, fontWeight = FontWeight.Black, color = l.color)
                }
            }
        }
        Kind.Number -> {
            val n = item.int()
            val pool = (0..level.pick(5, 10, 20)).toList()
            val (numbers, answer) = options(n, pool, count, rnd)
            Quest(
                ask = t.findNumber(n),
                choices = numbers.map { Choice("$it", numberColor(it), if (it >= 10) 72.sp else 96.sp) },
                names = numbers.map { s.numberName(t.num(it)) },
                answer = answer,
                right = { t.rightNumber(it, n) },
                wrong = { t.wrongNumber(numbers[it]) },
            ) { solved ->
                if (solved && n in 1..10) {
                    Text(objects(CountingThings[n % CountingThings.size], n), fontSize = 50.sp, textAlign = TextAlign.Center, lineHeight = 62.sp)
                } else {
                    Text(if (solved) "$n" else "👂", fontSize = 130.sp, fontWeight = FontWeight.Black, color = numberColor(n))
                }
            }
        }
        Kind.Color -> {
            val i = item.int()
            val pool = ArtColors.indices.take(level.pick(6, 8, ArtColors.size)) + i
            val (colors, answer) = options(i, pool, count, rnd)
            Quest(
                ask = t.findColor(t.colorNames[i]),
                choices = colors.map { Choice("", ArtColors[it].color) },
                names = colors.map { s.colorName(t.colorNames[it]) },
                answer = answer,
                right = { t.rightColor(it, t.colorNames[i]) },
                wrong = { t.wrongColor(t.colorNames[colors[it]]) },
            ) { solved -> Text(if (solved) ArtColors[i].example else "🎨", fontSize = 140.sp) }
        }
        Kind.Shape -> {
            val i = item.int()
            val (shapes, answer) = options(i, Shapes.indices.toList(), count, rnd)
            val tileColors = FunColors.shuffled(rnd)
            Quest(
                ask = t.shapeFind[i],
                choices = shapes.mapIndexed { k, sh -> Choice(Shapes[sh].glyph, tileColors[k % tileColors.size], 100.sp) },
                names = shapes.map { s.shapeNames[it] },
                answer = answer,
                right = { "$it ${t.shapeIs[i]}" },
                wrong = { "${t.shapeIs[shapes[it]]} ${t.tryAgainShort}" },
            ) { solved -> Text(if (solved) Shapes[i].example else "👀", fontSize = 140.sp) }
        }
        Kind.Word -> {
            val w = item.word(language) ?: language.words.first()
            val (words, answer) = options(w, language.words, count, rnd)
            val readIt = rnd.nextBoolean()
            Quest(
                ask = if (readIt) t.readAsk else t.whichWordAsk(w.text),
                choices = if (readIt) words.mapIndexed { k, x -> Choice(x.emoji, Pastels[k % Pastels.size], 76.sp) }
                else words.mapIndexed { k, x -> Choice(x.text, FunColors[(k * 3) % FunColors.size], if (x.text.length > 4) 30.sp else 38.sp) },
                names = words.map { s.wordName(it.text) },
                answer = answer,
                right = { if (readIt) t.readRight(it, w.text) else t.whichWordRight(it, w.text) },
                wrong = { t.tryAgain },
            ) { solved ->
                if (readIt) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(w.text, fontSize = 72.sp, fontWeight = FontWeight.Black, color = Ink)
                        Text(if (solved) w.emoji else " ", fontSize = 80.sp)
                    }
                } else {
                    Text(w.emoji, fontSize = 150.sp)
                }
            }
        }
        Kind.Math -> mathQuest(item.key, language, count, rnd)
    }
}

private fun mathQuest(key: String, language: Language, count: Int, rnd: Random): Quest {
    val t = language.strings
    val s = language.story
    val kind = key.substringBefore(':')
    val max = key.substringAfter(':').toIntOrNull() ?: 5
    val thing = CountingThings.random(rnd)
    fun numbers(answer: Int, near: List<Int>) = options(answer, near.filter { it >= 0 }, count, rnd)
    return when (kind) {
        "count" -> {
            val n = rnd.nextInt(1, max + 1)
            val (numbers, answer) = numbers(n, (1..maxOf(max, 4)).toList())
            Quest(
                ask = t.countAsk,
                choices = numbers.map { Choice("$it", numberColor(it)) },
                names = numbers.map { s.numberName(t.num(it)) },
                answer = answer,
                right = { t.countRight(it, n) },
                wrong = { t.countWrong(numbers[it]) },
            ) { _ -> CountingField(thing, n) }
        }
        "add", "sub" -> {
            val add = kind == "add"
            val a = if (add) rnd.nextInt(1, max) else rnd.nextInt(2, max + 1)
            val b = if (add) rnd.nextInt(1, max - a + 1) else rnd.nextInt(1, a)
            val result = if (add) a + b else a - b
            val (numbers, answer) = numbers(result, ((result - 3)..(result + 3)).filter { it in 0..max })
            Quest(
                ask = if (add) t.addAsk(a, b) else t.subAsk(a, b),
                choices = numbers.map { Choice("$it", numberColor(it), if (it >= 10) 72.sp else 88.sp) },
                names = numbers.map { s.numberName(t.num(it)) },
                answer = answer,
                right = { if (add) t.addRight(it, a, b) else t.subRight(it, a, b) },
                wrong = { t.mathWrong },
            ) { solved ->
                Sum(thing, a, b, if (add) "+" else "−", if (solved) "$result" else "?", crossed = !add, showThings = (if (add) a + b else a) <= 10)
            }
        }
        else -> {
            val x = rnd.nextInt(1, max + 1)
            val y = generateSequence { rnd.nextInt(1, max + 1) }.first { it != x }
            val askMore = kind == "more" || rnd.nextBoolean()
            val answer = if ((x > y) == askMore) 0 else 1
            Quest(
                ask = if (askMore) t.compareMore else t.compareFewer,
                choices = listOf(Choice(objects(thing, x), Pastels[0], 26.sp), Choice(objects(thing, y), Pastels[2], 26.sp)),
                names = listOf("", ""),
                answer = answer,
                right = { t.compareRight(it, askMore) },
                wrong = { t.compareWrong },
            ) { solved ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (askMore) "⬆️" else "⬇️", fontSize = 100.sp)
                    Text(if (solved) "$x  ·  $y" else "⚖️", fontSize = 60.sp, fontWeight = FontWeight.Black, color = Ink)
                }
            }
        }
    }
}

/** The big card shown when the child meets something new. */
@Composable
fun MeetCard(item: Item, language: Language) {
    val t = language.strings
    when (item.kind) {
        Kind.Letter -> item.letter(language)?.let { l ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${l.char} ${l.char.lowercaseChar()}", fontSize = 120.sp, fontWeight = FontWeight.Black, color = l.color, lineHeight = 124.sp)
                Text(l.emoji, fontSize = 90.sp)
                Text(l.word, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = l.color)
            }
        }
        Kind.Number -> {
            val n = item.int()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$n", fontSize = 140.sp, fontWeight = FontWeight.Black, color = numberColor(n), lineHeight = 144.sp)
                if (n in 1..10) {
                    Text(objects(CountingThings[n % CountingThings.size], n), fontSize = 44.sp, textAlign = TextAlign.Center, lineHeight = 54.sp)
                }
                Text(t.num(n).replaceFirstChar { it.uppercase() }, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = numberColor(n))
            }
        }
        Kind.Color -> {
            val c = ArtColors[item.int()]
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(160.dp).clip(CircleShape).background(c.color).border(3.dp, Color(0xFFDDDDDD), CircleShape))
                Text(c.example, fontSize = 80.sp)
                Text(t.colorNames[item.int()].replaceFirstChar { it.uppercase() }, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Kind.Shape -> {
            val sh = Shapes[item.int()]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(sh.glyph, fontSize = 150.sp, color = FunColors[item.int() % FunColors.size])
                Text(sh.example, fontSize = 80.sp)
                Text(language.story.shapeShort[item.int()].replaceFirstChar { it.uppercase() }, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Kind.Word -> item.word(language)?.let { w ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.emoji, fontSize = 110.sp)
                Text(w.text.uppercase(), fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color(0xFF219EBC))
                Text(w.text, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Kind.Math -> {
            val kind = item.key.substringBefore(':')
            when (kind) {
                "count" -> Text("🍎🍎🍎  →  3", fontSize = 56.sp, fontWeight = FontWeight.Black, color = Ink)
                "add" -> Sum("🍎", 2, 1, "+", "3", crossed = false, showThings = true)
                "sub" -> Sum("🍎", 3, 1, "−", "2", crossed = true, showThings = true)
                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍎🍎🍎", fontSize = 56.sp)
                    Text("  ⚖️  ", fontSize = 56.sp)
                    Text("🍎", fontSize = 56.sp)
                }
            }
        }
    }
}

/** What a floating tile shows in the catch game. */
@Composable
fun CatchFace(item: Item, language: Language, size: Int) {
    when (item.kind) {
        Kind.Letter -> item.letter(language)?.let {
            Text(it.char.toString(), fontSize = (size * 0.55f).sp, fontWeight = FontWeight.Black, color = it.color)
        }
        Kind.Number -> Text(item.key, fontSize = (size * (if (item.key.length > 1) 0.42f else 0.55f)).sp, fontWeight = FontWeight.Black, color = numberColor(item.key.toInt()))
        // A grey ring so white shows on the white tile.
        else -> Box(
            Modifier.size((size * 0.62f).dp).clip(CircleShape).background(ArtColors[item.key.toInt()].color)
                .border(3.dp, Color(0xFFBBBBBB), CircleShape),
        )
    }
}

/** Other things of the same kind, to mix in with the right ones in the catch game. */
fun Item.lookalikes(language: Language, level: Difficulty): List<Item> = when (kind) {
    Kind.Letter -> language.alphabet.filter { it.char != letter(language)?.char }.map { Item(Kind.Letter, "${language.name}/${it.char}") }
    Kind.Number -> (0..level.pick(5, 10, 20)).filter { "$it" != key }.map { Item(Kind.Number, "$it") }
    else -> (0 until level.pick(6, 8, ArtColors.size)).filter { "$it" != key }.map { Item(Kind.Color, "$it") }
}
