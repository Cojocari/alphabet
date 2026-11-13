package com.cojocari.alphabet.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.data.CountingThings
import com.cojocari.alphabet.data.numberColor
import com.cojocari.alphabet.data.objects
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.i18n.Strings
import com.cojocari.alphabet.ui.draw.DrawScreen
import com.cojocari.alphabet.ui.draw.TraceItem
import com.cojocari.alphabet.ui.games.BalloonItem
import com.cojocari.alphabet.ui.games.BalloonsScreen
import com.cojocari.alphabet.ui.games.MemoryScreen
import com.cojocari.alphabet.ui.games.PickGame
import com.cojocari.alphabet.ui.games.SpellScreen
import com.cojocari.alphabet.ui.games.addRound
import com.cojocari.alphabet.ui.games.compareRound
import com.cojocari.alphabet.ui.games.colorRound
import com.cojocari.alphabet.ui.games.shapeRound
import com.cojocari.alphabet.ui.games.FreeDrawScreen
import com.cojocari.alphabet.ui.games.findNumberRound
import com.cojocari.alphabet.ui.games.firstLetterRound
import com.cojocari.alphabet.ui.games.howManyRound
import com.cojocari.alphabet.ui.games.readPictureRound
import com.cojocari.alphabet.ui.games.subtractRound
import com.cojocari.alphabet.ui.games.whichWordRound
import com.cojocari.alphabet.ui.letters.CardItem
import com.cojocari.alphabet.ui.letters.LettersScreen
import com.cojocari.alphabet.ui.quiz.QuizScreen

/** Every mini-game, with its tile emoji and name. */
enum class GameId(val emoji: String, val label: (Strings) -> String) {
    LetterCards("🔤", { it.gameCards }),
    FindLetter("❓", { it.gameFindLetter }),
    LetterBalloons("🎈", { it.gameBalloons }),
    Memory("🃏", { it.gameMemory }),
    FirstLetter("🍎", { it.gameFirstLetter }),

    NumberCards("🔢", { it.gameNumberCards }),
    FindNumber("👂", { it.gameFindNumber }),
    HowMany("🐞", { it.gameHowMany }),
    NumberBalloons("🎈", { it.gameBalloons }),

    ReadPicture("📖", { it.gameReadPicture }),
    WhichWord("🔍", { it.gameWhichWord }),

    TraceUpper("✏️", { it.gameTraceUpper }),
    TraceLower("🖍️", { it.gameTraceLower }),
    TraceNumbers("✍️", { it.gameTraceNumbers }),
    Spell("🧩", { it.gameSpell }),

    Add("➕", { it.gameAdd }),
    Subtract("➖", { it.gameSubtract }),
    Compare("⚖️", { it.gameCompare }),

    Colors("🎨", { it.gameColors }),
    Shapes("🔷", { it.gameShapes }),
    FreeDraw("🖌️", { it.gameFreeDraw }),
}

/** The five learning worlds on the home screen. */
enum class World(
    val symbol: String,
    val emoji: String,
    val colors: List<Color>,
    val label: (Strings) -> String,
    val games: List<GameId>,
) {
    Letters(
        "ABC", "🦁", listOf(Color(0xFFFF6B6B), Color(0xFFFFA94D)), { it.worldLetters },
        listOf(GameId.LetterCards, GameId.FindLetter, GameId.LetterBalloons, GameId.Memory, GameId.FirstLetter),
    ),
    Numbers(
        "123", "🐞", listOf(Color(0xFF4DABF7), Color(0xFF3BC9DB)), { it.worldNumbers },
        listOf(GameId.NumberCards, GameId.HowMany, GameId.FindNumber, GameId.NumberBalloons),
    ),
    Reading(
        "📖", "🐱", listOf(Color(0xFF9775FA), Color(0xFFDA77F2)), { it.worldReading },
        listOf(GameId.ReadPicture, GameId.WhichWord),
    ),
    Writing(
        "✏️", "🖍️", listOf(Color(0xFF51CF66), Color(0xFF94D82D)), { it.worldWriting },
        listOf(GameId.TraceUpper, GameId.TraceLower, GameId.TraceNumbers, GameId.Spell),
    ),
    Math(
        "+ −", "🍎", listOf(Color(0xFFFF8787), Color(0xFFF783AC)), { it.worldMath },
        listOf(GameId.Add, GameId.Subtract, GameId.Compare),
    ),
    Art(
        "●▲■", "🦄", listOf(Color(0xFF20C997), Color(0xFF15AABF)), { it.worldArt },
        listOf(GameId.Colors, GameId.Shapes, GameId.FreeDraw),
    ),
}

/** The first game of every world is free, so children can try each world before buying. */
val GameId.isFree: Boolean get() = World.entries.any { it.games.first() == this }

val GameId.world: World get() = World.entries.first { this in it.games }

/** Starts a game in the current language. */
@Composable
fun GameContent(id: GameId, onBack: () -> Unit) {
    val language = LocalLanguage.current
    val t = language.strings
    key(language, id) {
        when (id) {
            GameId.LetterCards -> {
                val items = remember {
                    language.alphabet.map {
                        CardItem(
                            "${it.char}${it.char.lowercaseChar()}", it.emoji, 110.sp, it.word,
                            t.letterCard(it), it.color, it.char.toString(),
                        )
                    }
                }
                LettersScreen(onBack, items)
            }
            GameId.FindLetter -> QuizScreen(onBack)
            GameId.LetterBalloons -> {
                val items = remember {
                    language.alphabet.map { BalloonItem(it.char.toString(), t.balloonsAsk(it), t.thatIs(it), it.char) }
                }
                BalloonsScreen(onBack, items)
            }
            GameId.Memory -> MemoryScreen(onBack)
            GameId.FirstLetter -> PickGame(onBack) { firstLetterRound(language, it) }

            GameId.NumberCards -> {
                val items = remember {
                    (1..20).map { n ->
                        val size = when {
                            n <= 5 -> 80.sp
                            n <= 10 -> 56.sp
                            else -> 38.sp
                        }
                        CardItem(
                            "$n", objects(CountingThings[n % CountingThings.size], n), size,
                            t.num(n).replaceFirstChar { it.uppercase() }, t.numberCard(n), numberColor(n), "$n",
                        )
                    }
                }
                LettersScreen(onBack, items)
            }
            GameId.FindNumber -> PickGame(onBack) { findNumberRound(language, it) }
            GameId.HowMany -> PickGame(onBack) { howManyRound(language, it) }
            GameId.NumberBalloons -> {
                val items = remember {
                    (0..10).map { BalloonItem("$it", t.balloonsNumber(it), t.wrongNumber(it)) }
                }
                BalloonsScreen(onBack, items)
            }

            GameId.ReadPicture -> PickGame(onBack) { readPictureRound(language, it) }
            GameId.WhichWord -> PickGame(onBack) { whichWordRound(language, it) }

            GameId.TraceUpper -> {
                val items = remember {
                    language.alphabet.map { TraceItem("${it.char}", it.color, it.emoji, t.drawStart(it), t.drawDone(it)) }
                }
                DrawScreen(onBack, items)
            }
            GameId.TraceLower -> {
                val items = remember {
                    language.alphabet.map {
                        TraceItem("${it.char.lowercaseChar()}", it.color, it.emoji, t.drawLowerStart(it), t.drawLowerDone(it))
                    }
                }
                DrawScreen(onBack, items)
            }
            GameId.TraceNumbers -> {
                val items = remember {
                    (0..10).map { n ->
                        TraceItem("$n", numberColor(n), CountingThings[n % CountingThings.size], t.drawNumberStart(n), t.drawNumberDone(n))
                    }
                }
                DrawScreen(onBack, items)
            }
            GameId.Spell -> SpellScreen(onBack)

            GameId.Add -> PickGame(onBack) { addRound(language, it) }
            GameId.Subtract -> PickGame(onBack) { subtractRound(language, it) }
            GameId.Compare -> PickGame(onBack) { compareRound(language, it) }

            GameId.Colors -> PickGame(onBack) { colorRound(language, it) }
            GameId.Shapes -> PickGame(onBack) { shapeRound(language, it) }
            GameId.FreeDraw -> FreeDrawScreen(onBack)
        }
    }
}
