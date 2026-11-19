package com.cojocari.alphabet.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.data.Animals
import com.cojocari.alphabet.data.CountingThings
import com.cojocari.alphabet.data.Habitat
import com.cojocari.alphabet.ui.animals.AnimalsScreen
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

    AnimalsFarm("🐄", { it.gameFarmAnimals }),
    AnimalsWild("🦁", { it.gameWildAnimals }),
    AnimalsAll("🐾", { it.gameAllAnimals }),
}

/** The learning worlds on the home screen. */
enum class World(
    val symbol: String,
    val emoji: String,
    val colors: List<Color>,
    val label: (Strings) -> String,
    val games: List<GameId>,
) {
    Letters(
        "ABC", "🦁", listOf(Color(0xFFFF6B6B), Color(0xFFFFA94D)), { it.worldLetters },
        listOf(GameId.LetterCards, GameId.LetterBalloons, GameId.FindLetter, GameId.Memory, GameId.FirstLetter),
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
        listOf(GameId.FreeDraw, GameId.Colors, GameId.Shapes),
    ),
    Animals(
        "🐾", "🐮", listOf(Color(0xFFFAB005), Color(0xFF82C91E)), { it.worldAnimals },
        listOf(GameId.AnimalsFarm, GameId.AnimalsWild, GameId.AnimalsAll),
    ),
}

/**
 * Free without the purchase (8 of 24): one "hook" per world, plus Balloons, the most fun
 * letter game. They're free at Easy and Medium; ⭐⭐⭐ Hard needs the unlock.
 * Every other game can be tried as a short demo.
 */
val FreeGames = setOf(
    GameId.LetterCards, GameId.LetterBalloons, GameId.NumberCards, GameId.ReadPicture,
    GameId.TraceUpper, GameId.Add, GameId.FreeDraw, GameId.AnimalsFarm,
)

val GameId.isFree: Boolean get() = this in FreeGames

val GameId.world: World get() = World.entries.first { this in it.games }

/** The world's own story path with Hupi (see [com.cojocari.alphabet.story.Paths]); Animals has none. */
val World.storyPath: List<Int>?
    get() = when (this) {
        World.Letters -> com.cojocari.alphabet.story.Paths.Letters
        World.Numbers -> com.cojocari.alphabet.story.Paths.Numbers
        World.Reading -> com.cojocari.alphabet.story.Paths.Reading
        World.Writing -> com.cojocari.alphabet.story.Paths.Writing
        World.Math -> com.cojocari.alphabet.story.Paths.Math
        World.Art -> com.cojocari.alphabet.story.Paths.Art
        World.Animals -> null
    }

/** Starts a game in the current language. */
@Composable
fun GameContent(id: GameId, onBack: () -> Unit, onUnlock: () -> Unit) {
    val language = LocalLanguage.current
    val t = language.strings
    val progress = com.cojocari.alphabet.data.LocalProgress.current
    val premium = progress.premium
    val hardLocked = !premium && id.isFree
    val demo = !premium && !id.isFree
    val chosen = progress.difficulty
    val d = if (hardLocked && chosen == com.cojocari.alphabet.data.Difficulty.Hard) com.cojocari.alphabet.data.Difficulty.Medium else chosen
    // Bumped by "play again" at the end of a demo, to start it over.
    var demoRun by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    // Changing the level (or buying) restarts the game with freshly generated rounds.
    key(language, id, d, premium, demoRun) {
    val access = remember {
        com.cojocari.alphabet.ui.components.GameAccess(
            demo = demo,
            hardLocked = hardLocked,
            level = d,
            // A demo always plays the same rounds: a fixed seed per game and level.
            random = if (demo) kotlin.random.Random(id.ordinal * 7 + d.ordinal) else kotlin.random.Random.Default,
            replay = { demoRun++ },
            unlock = onUnlock,
        )
    }
    val rnd = access.random
    androidx.compose.runtime.CompositionLocalProvider(
        com.cojocari.alphabet.ui.components.LocalShowDifficulty provides id.hasLevels,
        com.cojocari.alphabet.ui.components.LocalGameAccess provides access,
    ) {
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
            GameId.FirstLetter -> PickGame(onBack) { firstLetterRound(language, d, rnd) }

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
            GameId.FindNumber -> PickGame(onBack) { findNumberRound(language, d, rnd) }
            GameId.HowMany -> PickGame(onBack) { howManyRound(language, d, rnd) }
            GameId.NumberBalloons -> {
                val items = remember {
                    (0..10).map { BalloonItem("$it", t.balloonsNumber(it), t.wrongNumber(it)) }
                }
                BalloonsScreen(onBack, items)
            }

            GameId.ReadPicture -> PickGame(onBack) { readPictureRound(language, d, rnd) }
            GameId.WhichWord -> PickGame(onBack) { whichWordRound(language, d, rnd) }

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

            GameId.Add -> PickGame(onBack) { addRound(language, d, rnd) }
            GameId.Subtract -> PickGame(onBack) { subtractRound(language, d, rnd) }
            GameId.Compare -> PickGame(onBack) { compareRound(language, d, rnd) }

            GameId.Colors -> PickGame(onBack) { colorRound(language, d, rnd) }
            GameId.Shapes -> PickGame(onBack) { shapeRound(language, d, rnd) }
            GameId.FreeDraw -> FreeDrawScreen(onBack)

            GameId.AnimalsFarm -> AnimalsScreen(onBack, Animals.filter { it.habitat == Habitat.Farm }, t.gameFarmAnimals, id.emoji)
            GameId.AnimalsWild -> AnimalsScreen(onBack, Animals.filter { it.habitat == Habitat.Wild }, t.gameWildAnimals, id.emoji)
            GameId.AnimalsAll -> AnimalsScreen(onBack, Animals, t.worldAnimals, id.emoji)
        }
    }
}
}

/** Games with Easy / Medium / Hard (flash cards, free drawing and animals have none). */
val GameId.hasLevels: Boolean
    get() = this !in setOf(
        GameId.LetterCards, GameId.NumberCards, GameId.FreeDraw,
        GameId.AnimalsFarm, GameId.AnimalsWild, GameId.AnimalsAll,
    )
