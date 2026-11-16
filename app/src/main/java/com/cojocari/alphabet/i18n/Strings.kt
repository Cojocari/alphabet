package com.cojocari.alphabet.i18n

import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.Letter

/** Every phrase the app shows or says, so each language can word them naturally. */
interface Strings {
    // ---- Home & worlds ----
    val greeting: String
    val worldLetters: String
    val worldNumbers: String
    val worldReading: String
    val worldWriting: String
    val worldMath: String
    val askGrownUp: String
    val newSticker: String

    // ---- Game names (tiles) ----
    val gameCards: String
    val gameFindLetter: String
    val gameBalloons: String
    val gameMemory: String
    val gameFirstLetter: String
    val gameNumberCards: String
    val gameFindNumber: String
    val gameHowMany: String
    val gameReadPicture: String
    val gameWhichWord: String
    val gameTraceUpper: String
    val gameTraceLower: String
    val gameTraceNumbers: String
    val gameSpell: String
    val gameAdd: String
    val gameSubtract: String
    val gameCompare: String

    val praise: List<String>

    // ---- Numbers ----
    /** Number words from zero to twenty. */
    val numbers: List<String>
    fun num(n: Int): String = numbers[n]
    fun numberCard(n: Int): String = num(n).replaceFirstChar { it.uppercase() } + "!"
    fun findNumber(n: Int): String
    fun rightNumber(praise: String, n: Int): String
    fun wrongNumber(n: Int): String
    fun balloonsNumber(n: Int): String
    val countAsk: String
    fun countRight(praise: String, n: Int): String =
        (1..n).joinToString(", ") { num(it) }.replaceFirstChar { it.uppercase() } + "! " + praise
    fun countWrong(n: Int): String

    // ---- Letters ----
    /** "A is for Apple!" */
    fun isFor(l: Letter): String
    fun letterCard(l: Letter): String = "${l.name}. ${isFor(l)}"
    fun findLetter(l: Letter): String
    val findLetterLabel: String
    fun correct(praise: String, l: Letter, superstar: Boolean): String
    fun wrong(picked: Letter, target: Letter): String
    fun balloonsAsk(l: Letter): String
    fun thatIs(l: Letter): String
    val balloonsDone: String
    val memoryStart: String
    val memoryDone: String
    fun pictureAsk(l: Letter): String
    fun pictureRight(praise: String, l: Letter): String
    fun pictureWrong(picked: Letter): String

    // ---- Writing ----
    fun drawStart(l: Letter): String
    fun drawDone(l: Letter): String
    fun drawLowerStart(l: Letter): String
    fun drawLowerDone(l: Letter): String
    fun drawNumberStart(n: Int): String
    fun drawNumberDone(n: Int): String
    fun spellAsk(word: String): String
    fun spellDone(praise: String, word: String): String

    // ---- Reading ----
    val readAsk: String
    fun readRight(praise: String, word: String): String
    val tryAgain: String
    fun whichWordAsk(word: String): String
    fun whichWordRight(praise: String, word: String): String

    // ---- Math ----
    fun addAsk(a: Int, b: Int): String
    fun subAsk(a: Int, b: Int): String
    fun addRight(praise: String, a: Int, b: Int): String
    fun subRight(praise: String, a: Int, b: Int): String
    val mathWrong: String
    val compareMore: String
    val compareFewer: String
    fun compareRight(praise: String, more: Boolean): String
    val compareWrong: String

    // ---- Stickers ----
    fun stickerBook(count: Int): String

    // ---- Grown-ups ----
    fun gateQuestion(a: Int, b: Int): String
    val parentTitle: String
    val unlockTitle: String
    val unlockBody: String
    fun unlockButton(price: String?): String
    val unlockUnavailable: String
    val restore: String
    val unlocked: String
    val voice: String
    val soundEffects: String
    val progressTitle: String
    fun lettersLearned(n: Int, total: Int): String
    val reset: String
    val resetConfirm: String
    val privacy: String
    val devUnlock: String

    /** Shown on the home screen when the device has no voice for this language. */
    val missingVoice: String

    // ---- Personal touches ----
    /** Greeting with the child's name when the parent has entered one. */
    fun greet(name: String): String
    fun newStickerFor(name: String): String
    val mascotLines: List<String>
    val giftOpened: String
    fun streak(days: Int): String

    // ---- Colours & shapes ----
    val worldArt: String
    val gameColors: String
    val gameShapes: String
    val gameFreeDraw: String

    // ---- Animals ----
    val worldAnimals: String
    val gameFarmAnimals: String
    val gameWildAnimals: String
    val gameAllAnimals: String
    val animalsHint: String
    fun animalName(a: Animal): String
    /** The sound in the speech bubble: "Moo!" */
    fun animalSound(a: Animal): String
    /** "The cow says moo!" */
    fun animalSays(a: Animal): String
    /** Names of [com.cojocari.alphabet.data.ArtColors], in the same order. */
    val colorNames: List<String>
    fun findColor(name: String): String
    fun rightColor(praise: String, name: String): String
    fun wrongColor(name: String): String
    /** "Find the circle!" for each of [com.cojocari.alphabet.data.Shapes]. */
    val shapeFind: List<String>
    /** "That's a circle!" for each shape. */
    val shapeIs: List<String>
    val tryAgainShort: String
    val freeDrawStart: String
    val drawingSaved: String
    val sceneStart: String

    // ---- Play-time limit ----
    val restTitle: String
    fun restSay(name: String): String
    val restMore: String
    val childNameLabel: String
    val dailyLimit: String
    val noLimit: String
    fun playedToday(minutes: Int): String
    fun unlockBodyFor(games: Int): String

    /** Said when the level is switched: Easy, Medium, Hard. */
    val difficultyNames: List<String>

    // ---- Demo & purchase ----
    val demoEnd: String
    val demoEndTitle: String
    val demoEndBody: String
    /** Lines on the purchase screen, each starting with an emoji. */
    val paywallFeatures: List<String>
    val paywallOnce: String
    val familyNote: String
    val appName: String
    val licenses: String
    val licensesIntro: String
    val soundCredits: String
    val backHome: String

    // ---- Contact (grown-ups' corner) ----
    val contactTitle: String
    val contactBody: String
    val reportBug: String
    val suggestIdea: String
    val rateApp: String
    val bugSubject: String
    val ideaSubject: String
    val emailIntro: String
    val noEmailApp: String
}

object EnglishStrings : Strings {
    override val greeting = "Hi friend! What do you want to learn today?"
    override val worldLetters = "Letters"
    override val worldNumbers = "Numbers"
    override val worldReading = "Reading"
    override val worldWriting = "Writing"
    override val worldMath = "Math"
    override val askGrownUp = "Ask a grown-up to unlock this game!"
    override val newSticker = "Wow! You won a new sticker!"

    override val gameCards = "Cards"
    override val gameFindLetter = "Find it"
    override val gameBalloons = "Balloons"
    override val gameMemory = "Memory"
    override val gameFirstLetter = "First letter"
    override val gameNumberCards = "Cards"
    override val gameFindNumber = "Find it"
    override val gameHowMany = "How many?"
    override val gameReadPicture = "Read it"
    override val gameWhichWord = "Which word?"
    override val gameTraceUpper = "ABC"
    override val gameTraceLower = "abc"
    override val gameTraceNumbers = "123"
    override val gameSpell = "Spell it"
    override val gameAdd = "Add"
    override val gameSubtract = "Take away"
    override val gameCompare = "More or less"

    override val praise = listOf("Yes!", "Great job!", "Super!", "You got it!", "Hooray!", "Well done!")

    override val numbers = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
        "nineteen", "twenty",
    )
    override fun findNumber(n: Int) = "Find the number ${num(n)}!"
    override fun rightNumber(praise: String, n: Int) = "$praise That's ${num(n)}!"
    override fun wrongNumber(n: Int) = "That's ${num(n)}. Try again!"
    override fun balloonsNumber(n: Int) = "Pop the balloons with the number ${num(n)}!"
    override val countAsk = "How many are there? Tap them to count!"
    override fun countWrong(n: Int) = "That's ${num(n)}. Let's count again!"

    override fun isFor(l: Letter) = "${l.name} is for ${l.word}!"
    override fun findLetter(l: Letter) = "Find the letter ${l.name}!"
    override val findLetterLabel = "Find the letter!"
    override fun correct(praise: String, l: Letter, superstar: Boolean) =
        "$praise That's ${l.name}, for ${l.word}!" + if (superstar) " You are a superstar!" else ""
    override fun wrong(picked: Letter, target: Letter) =
        "That's ${picked.name}. Try again! Find ${target.name}."
    override fun balloonsAsk(l: Letter) = "Pop the balloons with the letter ${l.name}!"
    override fun thatIs(l: Letter) = "That's ${l.name}."
    override val balloonsDone = "Hooray! You popped them all!"
    override val memoryStart = "Find the pairs! Match each letter with its picture."
    override val memoryDone = "Well done! You found all the pairs!"
    override fun pictureAsk(l: Letter) = "What letter does ${l.word.lowercase()} start with?"
    override fun pictureRight(praise: String, l: Letter) = "$praise ${l.word} starts with ${l.name}!"
    override fun pictureWrong(picked: Letter) = "That's ${picked.name}. Try again!"

    override fun drawStart(l: Letter) = "Let's write the letter ${l.name}. ${isFor(l)}"
    override fun drawDone(l: Letter) = "Wow! You wrote the letter ${l.name}! Great job!"
    override fun drawLowerStart(l: Letter) = "Let's write the small letter ${l.name}!"
    override fun drawLowerDone(l: Letter) = "Great! You wrote the small letter ${l.name}!"
    override fun drawNumberStart(n: Int) = "Let's write the number ${num(n)}!"
    override fun drawNumberDone(n: Int) = "Wow! You wrote the number ${num(n)}!"
    override fun spellAsk(word: String) = "Spell the word $word!"
    override fun spellDone(praise: String, word: String) = "$praise You spelled $word!"

    override val readAsk = "Read the word. Then find its picture!"
    override fun readRight(praise: String, word: String) = "$praise It says $word!"
    override val tryAgain = "Not this one. Try again!"
    override fun whichWordAsk(word: String) = "Find the word $word!"
    override fun whichWordRight(praise: String, word: String) = "$praise That says $word!"

    override fun addAsk(a: Int, b: Int) = "${num(a)} plus ${num(b)}. How many altogether?"
    override fun subAsk(a: Int, b: Int) = "${num(a)} take away ${num(b)}. How many are left?"
    override fun addRight(praise: String, a: Int, b: Int) = "$praise ${num(a)} plus ${num(b)} equals ${num(a + b)}!"
    override fun subRight(praise: String, a: Int, b: Int) = "$praise ${num(a)} take away ${num(b)} equals ${num(a - b)}!"
    override val mathWrong = "Not quite. Count them again!"
    override val compareMore = "Which side has more?"
    override val compareFewer = "Which side has fewer?"
    override fun compareRight(praise: String, more: Boolean) =
        if (more) "$praise This side has more!" else "$praise This side has fewer!"
    override val compareWrong = "Count again and look carefully!"

    override fun stickerBook(count: Int) = when (count) {
        0 -> "Your sticker book! Win stars in the games to get stickers."
        1 -> "Your sticker book! You have one sticker."
        else -> "Your sticker book! You have $count stickers."
    }

    override fun gateQuestion(a: Int, b: Int) = "For grown-ups: what is $a + $b?"
    override val parentTitle = "Grown-ups' corner"
    override val unlockTitle = "Unlock all games"
    override val unlockBody =
        "All 18 games in Letters, Numbers, Reading, Writing and Math, plus every future game " +
            "and update. One-time purchase: no ads, no subscriptions, ever."
    override fun unlockButton(price: String?) = if (price != null) "Unlock for $price" else "Unlock"
    override val unlockUnavailable = "Google Play isn't reachable right now. Please try again later."
    override val restore = "Restore purchase"
    override val unlocked = "✅ All games are unlocked. Thank you for your support!"
    override val voice = "Voice"
    override val soundEffects = "Sound effects"
    override val progressTitle = "Progress"
    override fun lettersLearned(n: Int, total: Int) = "Letters found in games: $n of $total"
    override val reset = "Reset progress"
    override val resetConfirm = "Tap again to erase all stars and stickers"
    override val privacy = "This app collects no data, needs no account and shows no ads."
    override val devUnlock = "Developer: unlock without paying"
    override val missingVoice = "🔇 No English voice. Settings → Text-to-speech"

    override fun greet(name: String) =
        if (name.isBlank()) greeting else "Hi $name! What do you want to learn today?"
    override fun newStickerFor(name: String) =
        if (name.isBlank()) newSticker else "Wow, $name! You won a new sticker!"
    override val mascotLines = listOf(
        "Hoo hoo! Let's learn together!",
        "You are doing great!",
        "I love playing with you!",
        "Which world shall we visit?",
        "Hoo hoo! You are so clever!",
    )
    override val giftOpened = "Surprise! A present for you!"
    override fun streak(days: Int) =
        if (days <= 1) "You came to play today! See you tomorrow!" else "You played $days days in a row! Amazing!"

    override val worldArt = "Colors & Shapes"
    override val gameColors = "Colors"
    override val gameShapes = "Shapes"
    override val gameFreeDraw = "Free draw"
    override val worldAnimals = "Animals"
    override val gameFarmAnimals = "Farm & pets"
    override val gameWildAnimals = "Wild"
    override val gameAllAnimals = "All"
    override val animalsHint = "Tap an animal to hear it!"
    override fun animalName(a: Animal) = a.en.replaceFirstChar { it.uppercase() }
    override fun animalSound(a: Animal) = a.enSound
    override fun animalSays(a: Animal) = "The ${a.en} says ${a.enSound.lowercase()}"
    override val colorNames = listOf("red", "orange", "yellow", "green", "blue", "purple", "pink", "brown", "black", "white")
    override fun findColor(name: String) = "Find the color $name!"
    override fun rightColor(praise: String, name: String) = "$praise That's $name!"
    override fun wrongColor(name: String) = "That's $name. Try again!"
    override val shapeFind = listOf(
        "Find the circle!", "Find the square!", "Find the triangle!",
        "Find the star!", "Find the heart!", "Find the diamond!",
    )
    override val shapeIs = listOf(
        "That's a circle!", "That's a square!", "That's a triangle!",
        "That's a star!", "That's a heart!", "That's a diamond!",
    )
    override val tryAgainShort = "Try again!"
    override val freeDrawStart = "Let's draw anything you like!"
    override val drawingSaved = "Your picture is saved!"
    override val sceneStart = "Put your stickers in the picture!"

    override val restTitle = "Time to rest!"
    override fun restSay(name: String) =
        "Great playing today${if (name.isBlank()) "" else ", $name"}! Time to rest your eyes. See you tomorrow!"
    override val restMore = "Grown-ups: 15 more minutes"
    override val childNameLabel = "Child's name"
    override val dailyLimit = "Daily play time"
    override val noLimit = "No limit"
    override fun playedToday(minutes: Int) = "Played today: $minutes min"
    override val difficultyNames = listOf("Easy!", "Medium!", "Hard!")

    override val demoEnd = "Well done! Did you like it? Ask a grown-up to unlock all the games!"
    override val demoEndTitle = "Well done! 🌟"
    override val demoEndBody = "That was the free try. Ask a grown-up to unlock this game and all the others."
    override val paywallFeatures = listOf(
        "🎮  All 24 games in 7 worlds, with 28 animal sounds",
        "⭐  Easy, Medium and Hard levels in every game",
        "🇷🇴🇬🇧  Romanian and English",
        "🎁  Every future game and update, free",
        "🚫  No ads, no subscriptions",
    )
    override val paywallOnce = "Pay once, play forever"
    override val familyNote = "Works on all your family's devices with Google Play Family Library."
    override val appName = "Clever Owl"
    override val licenses = "Open-source licences"
    override val licensesIntro = "This app is built with these libraries. Thank you to their authors!"
    override val soundCredits = "Animal sounds: real recordings under free licences (trimmed)."
    override val backHome = "Back to the games"

    override val contactTitle = "Contact us"
    override val contactBody = "Found a problem or have an idea? We read every message."
    override val reportBug = "🐞  Report a problem"
    override val suggestIdea = "💡  Suggest an idea"
    override val rateApp = "⭐  Rate the app"
    override val bugSubject = "Problem report"
    override val ideaSubject = "Idea"
    override val emailIntro = "Please describe what happened or your idea here:"
    override val noEmailApp = "No email app found. Write to us at:"
    override fun unlockBodyFor(games: Int) =
        "All $games games in Letters, Numbers, Reading, Writing, Math and Colors & Shapes, plus every " +
            "future game and update. One-time purchase: no ads, no subscriptions, ever."
}

object RomanianStrings : Strings {
    override val greeting = "Bună! Ce vrei să învățăm azi?"
    override val worldLetters = "Litere"
    override val worldNumbers = "Numere"
    override val worldReading = "Citire"
    override val worldWriting = "Scriere"
    override val worldMath = "Matematică"
    override val askGrownUp = "Roagă un adult să deblocheze acest joc!"
    override val newSticker = "Uau! Ai câștigat un abțibild nou!"

    override val gameCards = "Cartonașe"
    override val gameFindLetter = "Găsește"
    override val gameBalloons = "Baloane"
    override val gameMemory = "Perechi"
    override val gameFirstLetter = "Prima literă"
    override val gameNumberCards = "Cartonașe"
    override val gameFindNumber = "Găsește"
    override val gameHowMany = "Câte sunt?"
    override val gameReadPicture = "Citește"
    override val gameWhichWord = "Ce cuvânt?"
    override val gameTraceUpper = "ABC"
    override val gameTraceLower = "abc"
    override val gameTraceNumbers = "123"
    override val gameSpell = "Formează"
    override val gameAdd = "Adunare"
    override val gameSubtract = "Scădere"
    override val gameCompare = "Mai multe"

    override val praise = listOf("Da!", "Bravo!", "Foarte bine!", "Super!", "Ai reușit!", "Excelent!")

    override val numbers = listOf(
        "zero", "unu", "doi", "trei", "patru", "cinci", "șase", "șapte", "opt", "nouă", "zece",
        "unsprezece", "doisprezece", "treisprezece", "paisprezece", "cincisprezece", "șaisprezece",
        "șaptesprezece", "optsprezece", "nouăsprezece", "douăzeci",
    )
    override fun findNumber(n: Int) = "Găsește numărul ${num(n)}!"
    override fun rightNumber(praise: String, n: Int) = "$praise Acesta este numărul ${num(n)}!"
    override fun wrongNumber(n: Int) = "Acesta este numărul ${num(n)}. Mai încearcă!"
    override fun balloonsNumber(n: Int) = "Sparge baloanele cu numărul ${num(n)}!"
    override val countAsk = "Câte sunt? Atinge-le ca să le numeri!"
    override fun countWrong(n: Int) = "Acesta este numărul ${num(n)}. Hai să numărăm din nou!"

    private fun cap(s: String) = s.replaceFirstChar { it.uppercase() }

    /**
     * "Be de la banană!". No word starts with Ă or Â, so for those we explain it:
     * "Niciun cuvânt nu începe cu litera ă, dar o găsești în cuvântul rață!"
     */
    override fun isFor(l: Letter) =
        if (l.inside) "Niciun cuvânt nu începe cu litera ${l.name}, dar o găsești în cuvântul ${l.word.lowercase()}!"
        else "${cap(l.name)} de la ${l.word.lowercase()}!"

    override fun letterCard(l: Letter) = "Litera ${l.name}. ${isFor(l)}"
    override fun findLetter(l: Letter) = "Găsește litera ${l.name}!"
    override val findLetterLabel = "Găsește litera!"
    override fun correct(praise: String, l: Letter, superstar: Boolean) =
        "$praise Aceasta este litera ${l.name}. " +
            (if (l.inside) "Niciun cuvânt nu începe cu ea, dar o găsești în cuvântul ${l.word.lowercase()}!"
            else "${cap(l.name)} de la ${l.word.lowercase()}!") +
            if (superstar) " Ești un campion!" else ""
    override fun wrong(picked: Letter, target: Letter) =
        "Aceasta este litera ${picked.name}. Mai încearcă! Caută litera ${target.name}."
    override fun balloonsAsk(l: Letter) = "Sparge baloanele cu litera ${l.name}!"
    override fun thatIs(l: Letter) = "Aceasta este litera ${l.name}."
    override val balloonsDone = "Ura! Le-ai spart pe toate!"
    override val memoryStart = "Găsește perechile! Potrivește fiecare literă cu imaginea ei."
    override val memoryDone = "Bravo! Ai găsit toate perechile!"
    override fun pictureAsk(l: Letter) = "Cu ce literă începe cuvântul ${l.word.lowercase()}?"
    override fun pictureRight(praise: String, l: Letter) =
        "$praise Cuvântul ${l.word.lowercase()} începe cu litera ${l.name}!"
    override fun pictureWrong(picked: Letter) = "Aceasta este litera ${picked.name}. Mai încearcă!"

    override fun drawStart(l: Letter) = "Hai să scriem litera ${l.name}! ${isFor(l)}"
    override fun drawDone(l: Letter) = "Ura! Ai scris litera ${l.name}! Bravo!"
    override fun drawLowerStart(l: Letter) = "Hai să scriem litera mică ${l.name}!"
    override fun drawLowerDone(l: Letter) = "Bravo! Ai scris litera mică ${l.name}!"
    override fun drawNumberStart(n: Int) = "Hai să scriem numărul ${num(n)}!"
    override fun drawNumberDone(n: Int) = "Uau! Ai scris numărul ${num(n)}!"
    override fun spellAsk(word: String) = "Formează cuvântul $word!"
    override fun spellDone(praise: String, word: String) = "$praise Ai format cuvântul $word!"

    override val readAsk = "Citește cuvântul și găsește imaginea!"
    override fun readRight(praise: String, word: String) = "$praise Aici scrie $word!"
    override val tryAgain = "Nu este aceasta. Mai încearcă!"
    override fun whichWordAsk(word: String) = "Găsește cuvântul $word!"
    override fun whichWordRight(praise: String, word: String) = "$praise Aici scrie $word!"

    override fun addAsk(a: Int, b: Int) = "${num(a)} plus ${num(b)}. Cât face?"
    override fun subAsk(a: Int, b: Int) = "${num(a)} minus ${num(b)}. Cât face?"
    override fun addRight(praise: String, a: Int, b: Int) = "$praise ${num(a)} plus ${num(b)} egal ${num(a + b)}!"
    override fun subRight(praise: String, a: Int, b: Int) = "$praise ${num(a)} minus ${num(b)} egal ${num(a - b)}!"
    override val mathWrong = "Nu chiar. Mai numără o dată!"
    override val compareMore = "Unde sunt mai multe?"
    override val compareFewer = "Unde sunt mai puține?"
    override fun compareRight(praise: String, more: Boolean) =
        if (more) "$praise Aici sunt mai multe!" else "$praise Aici sunt mai puține!"
    override val compareWrong = "Mai numără o dată și uită-te cu atenție!"

    override fun stickerBook(count: Int) = when {
        count == 0 -> "Albumul tău! Câștigă stele în jocuri ca să primești abțibilduri."
        count == 1 -> "Albumul tău! Ai un abțibild."
        count % 100 in 1..19 -> "Albumul tău! Ai $count abțibilduri."
        else -> "Albumul tău! Ai $count de abțibilduri."
    }

    override fun gateQuestion(a: Int, b: Int) = "Pentru adulți: cât face $a + $b?"
    override val parentTitle = "Colțul părinților"
    override val unlockTitle = "Deblochează toate jocurile"
    override val unlockBody =
        "Toate cele 18 jocuri de Litere, Numere, Citire, Scriere și Matematică, plus toate jocurile " +
            "și actualizările viitoare. O singură plată: fără reclame și fără abonamente."
    override fun unlockButton(price: String?) = if (price != null) "Deblochează pentru $price" else "Deblochează"
    override val unlockUnavailable = "Google Play nu este disponibil acum. Încearcă din nou mai târziu."
    override val restore = "Restaurează achiziția"
    override val unlocked = "✅ Toate jocurile sunt deblocate. Mulțumim pentru sprijin!"
    override val voice = "Voce"
    override val soundEffects = "Efecte sonore"
    override val progressTitle = "Progres"
    override fun lettersLearned(n: Int, total: Int) = "Litere găsite în jocuri: $n din $total"
    override val reset = "Resetează progresul"
    override val resetConfirm = "Apasă din nou pentru a șterge stelele și abțibildurile"
    override val privacy = "Aplicația nu colectează date, nu cere cont și nu afișează reclame."
    override val devUnlock = "Dezvoltator: deblochează fără plată"
    override val missingVoice = "🔇 Lipsește vocea română. Setări → Text-to-speech"

    override fun greet(name: String) =
        if (name.isBlank()) greeting else "Bună, $name! Ce vrei să învățăm azi?"
    override fun newStickerFor(name: String) =
        if (name.isBlank()) newSticker else "Uau, $name! Ai câștigat un abțibild nou!"
    override val mascotLines = listOf(
        "Hu-hu! Hai să învățăm împreună!",
        "Te descurci minunat!",
        "Îmi place să mă joc cu tine!",
        "În ce lume mergem azi?",
        "Hu-hu! Ce bine te descurci!",
    )
    override val giftOpened = "Surpriză! Un cadou pentru tine!"
    override fun streak(days: Int) = when {
        days <= 1 -> "Ai venit azi să te joci! Ne vedem mâine!"
        days % 100 in 1..19 -> "Te-ai jucat $days zile la rând! Minunat!"
        else -> "Te-ai jucat $days de zile la rând! Minunat!"
    }

    override val worldArt = "Culori și forme"
    override val gameColors = "Culori"
    override val gameShapes = "Forme"
    override val gameFreeDraw = "Desen liber"
    override val worldAnimals = "Animale"
    override val gameFarmAnimals = "Domestice"
    override val gameWildAnimals = "Sălbatice"
    override val gameAllAnimals = "Toate"
    override val animalsHint = "Atinge un animal ca să-l auzi!"
    override fun animalName(a: Animal) = a.ro.replaceFirstChar { it.uppercase() }
    override fun animalSound(a: Animal) = a.roSound
    // The voice reads "ham-ham" as one odd word; a comma gives a clear "ham, ham".
    override fun animalSays(a: Animal) = "${animalName(a)} face ${a.roSound.lowercase().replace("-", ", ")}"
    override val colorNames = listOf("roșie", "portocalie", "galbenă", "verde", "albastră", "mov", "roz", "maro", "neagră", "albă")
    override fun findColor(name: String) = "Găsește culoarea $name!"
    override fun rightColor(praise: String, name: String) = "$praise Aceasta este culoarea $name!"
    override fun wrongColor(name: String) = "Aceasta este culoarea $name. Mai încearcă!"
    override val shapeFind = listOf(
        "Găsește cercul!", "Găsește pătratul!", "Găsește triunghiul!",
        "Găsește steaua!", "Găsește inima!", "Găsește rombul!",
    )
    override val shapeIs = listOf(
        "Acesta este un cerc!", "Acesta este un pătrat!", "Acesta este un triunghi!",
        "Aceasta este o stea!", "Aceasta este o inimă!", "Acesta este un romb!",
    )
    override val tryAgainShort = "Mai încearcă!"
    override val freeDrawStart = "Hai să desenăm ce vrei tu!"
    override val drawingSaved = "Desenul tău a fost salvat!"
    override val sceneStart = "Pune abțibildurile în imagine!"

    override val restTitle = "E timpul pentru o pauză!"
    override fun restSay(name: String) =
        "Te-ai jucat frumos azi${if (name.isBlank()) "" else ", $name"}! E timpul să te odihnești. Ne vedem mâine!"
    override val restMore = "Adulți: încă 15 minute"
    override val childNameLabel = "Numele copilului"
    override val dailyLimit = "Timp de joacă pe zi"
    override val noLimit = "Fără limită"
    override fun playedToday(minutes: Int) = "Azi: $minutes min"
    override val difficultyNames = listOf("Ușor!", "Mediu!", "Greu!")

    override val demoEnd = "Bravo! Ți-a plăcut? Roagă un adult să deblocheze toate jocurile!"
    override val demoEndTitle = "Bravo! 🌟"
    override val demoEndBody = "Aceasta a fost încercarea gratuită. Roagă un adult să deblocheze acest joc și toate celelalte."
    override val paywallFeatures = listOf(
        "🎮  Toate cele 24 de jocuri din 7 lumi, cu 28 de sunete de animale",
        "⭐  Nivel ușor, mediu și greu în fiecare joc",
        "🇷🇴🇬🇧  Română și engleză",
        "🎁  Toate jocurile și actualizările viitoare, gratuit",
        "🚫  Fără reclame, fără abonamente",
    )
    override val paywallOnce = "Plătești o dată, te joci mereu"
    override val familyNote = "Funcționează pe toate dispozitivele familiei prin Biblioteca de familie Google Play."
    override val appName = "Bufnița Isteață"
    override val licenses = "Licențe open-source"
    override val licensesIntro = "Aplicația este construită cu aceste biblioteci. Mulțumim autorilor lor!"
    override val soundCredits = "Sunete de animale: înregistrări reale cu licențe libere (scurtate)."
    override val backHome = "Înapoi la jocuri"

    override val contactTitle = "Contactează-ne"
    override val contactBody = "Ai găsit o problemă sau ai o idee? Citim fiecare mesaj."
    override val reportBug = "🐞  Raportează o problemă"
    override val suggestIdea = "💡  Propune o idee"
    override val rateApp = "⭐  Evaluează aplicația"
    override val bugSubject = "Raport problemă"
    override val ideaSubject = "Idee"
    override val emailIntro = "Descrie aici ce s-a întâmplat sau ideea ta:"
    override val noEmailApp = "Nu am găsit o aplicație de e-mail. Scrie-ne la:"
    override fun unlockBodyFor(games: Int) =
        "Toate cele $games de jocuri de Litere, Numere, Citire, Scriere, Matematică și Culori și forme, plus " +
            "toate jocurile și actualizările viitoare. O singură plată: fără reclame și fără abonamente."
}
