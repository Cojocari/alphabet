package com.cojocari.alphabet.i18n

import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.Letter

/**
 * Everything said or shown in Hupi's Adventure (the story mode), plus the age and learning
 * diary labels for grown-ups. Hupi is a boy owl; the magpie (coțofana) is a girl.
 * Romanian sentences avoid adjectives that depend on an animal's gender.
 */
interface StoryStrings {
    val title: String
    fun chapterTitle(chapter: Int): String
    fun chapterLabel(chapter: Int): String

    // ---- Opening ----
    val hatchAsk: String
    val hatchCracks: List<String>
    val hatchHello: String
    val magpieStory: String

    // ---- Chapters ----
    fun chapterIntro(chapter: Int): String
    /** "The cow has lost the colours…": what the lesson's animal friend needs (four per chapter, by lesson). */
    fun friendAsk(chapter: Int, lesson: Int, friend: String): String
    fun friendThanks(friend: String): String
    fun friendName(a: Animal): String
    fun cliffhanger(chapter: Int): String
    val finale: String
    fun diploma(name: String): String
    val diplomaTitle: String

    // ---- Names of things to learn, as said in a sentence ----
    fun letterName(l: Letter): String
    fun numberName(word: String): String
    fun colorName(name: String): String
    val shapeNames: List<String>
    val shapeShort: List<String>
    fun wordName(word: String): String

    // ---- Learning steps ----
    fun meetNumber(word: String): String
    fun meetColor(name: String): String
    fun meetWord(word: String): String
    fun meetMath(kind: String): String
    fun together(thing: String): String
    fun forgot(thing: String): String
    fun teachAsk(thing: String): String
    val teachYesRight: String
    val teachCaught: String
    fun teachMissedWrong(thing: String): String
    fun teachMissedRight(thing: String): String
    fun teachThanks(thing: String): String
    /** Hupi's happy reactions, added to one right answer in three. */
    val hupiCheers: List<String>
    fun catchAsk(kind: String, short: String): String
    val catchDone: String
    /** The magpie swoops in once a chapter: tap her until she drops what she took. */
    val magpieAppears: String
    val magpieHits: List<String>
    val magpieGone: String
    /** The treasure chest at the end of a lesson. */
    val chestAsk: String
    val treasureFound: String
    val showIntro: String
    val showApplause: String
    fun gotItem(stage: Int): String
    val paywall: String
    val notYet: String
    val mapHello: String
    val warmupIntro: String
    val warmupDone: String
    val sceneReplay: String

    // ---- A world's own story path ----
    val pathBanner: String
    fun pathIntro(chapter: Int): String
    val pathDone: String
    val pathLocked: String

    // ---- Hupi's house ----
    val houseTitle: String
    val shelfLetters: String
    val shelfNumbers: String
    val shelfColors: String
    val shelfShapes: String
    val shelfWords: String
    val shelfMath: String
    val shelfTreasures: String
    val legend: String
    val scarf: String
    /** What Hupi says when tapped; grows as he learns ([stage] = chapters finished). */
    fun hupiLines(stage: Int): List<String>

    // ---- Grown-ups ----
    val ageTitle: String
    val ageHint: String
    val agePrompt: String
    val years: String
    val diaryTitle: String
    fun storyAt(chapter: Int): String
    val storyFinished: String
    val learnedLabel: String
    val practisingLabel: String
    val diaryNote: String
    fun mathName(key: String): String
}

object EnglishStory : StoryStrings {
    override val title = "Hupi's Adventure"
    private val chapters = listOf("The Nest", "The Farm", "The River", "The Library", "The Market", "The Mountain", "The School")
    override fun chapterTitle(chapter: Int) = chapters[chapter]
    override fun chapterLabel(chapter: Int) = "Chapter ${chapter + 1}: ${chapters[chapter]}"

    override val hatchAsk = "Look, an egg! Tap it to help the chick hatch!"
    override val hatchCracks = listOf("Crack!", "Crack, crack!", "Almost there!")
    override val hatchHello = "Hello! I'm Hupi, a little owl. I don't know anything yet. Will you teach me?"
    override val magpieStory =
        "Oh no! The naughty magpie took the colours, the numbers and the letters of the forest! Let's get them back together!"

    override fun chapterIntro(chapter: Int) = listOf(
        "In the nest everything is grey. The magpie stole the colours!",
        "Hupi flies to the farm. The animals can't count any more: the magpie took the numbers!",
        "At the river, letters are floating everywhere. The magpie dropped them!",
        "The library is a mess! The books have lost their letters.",
        "At the market, nobody can add up the fruit. Let's help!",
        "On the mountain, the magpie hid the letters in the snow. Let's write them again!",
        "At last, the forest school! Hupi wants to read his first words. And look... the magpie is here!",
    )[chapter]

    override fun friendAsk(chapter: Int, lesson: Int, friend: String) = listOf(
        listOf(
            "$friend has lost the colours of the forest. Let's help Hupi find them!",
            "$friend wants to paint a rainbow, but the paint pots are empty!",
            "$friend has flowers that turned grey. Let's bring their colours back!",
            "$friend sees only grey all around. Let's bring the colours back!",
        ),
        listOf(
            "$friend wants to count the animals, but the numbers are gone!",
            "$friend can't remember how many eggs are in the basket. Let's count!",
            "$friend has lost the numbers from the barn door. Let's find them!",
            "$friend wants to play hide and seek, but can't count. Let's help!",
        ),
        listOf(
            "$friend found letters floating on the water. Let's see which ones!",
            "$friend is fishing, and look: letters on the hook! Which ones?",
            "$friend saw letters jumping like little fish. Let's catch them!",
            "$friend found a bottle with letters inside. Let's open it!",
        ),
        listOf(
            "$friend has a book with missing letters. Let's put them back!",
            "$friend wants to read a story, but the letters ran away!",
            "$friend found letters under the shelves. Let's tidy them up!",
            "$friend is looking after the library today and needs help with the letters!",
        ),
        listOf(
            "$friend is selling fruit at the market. Let's count together!",
            "$friend has a basket full of apples. Let's do the sums!",
            "$friend wants to share the fruit with friends. Let's help!",
            "$friend is buying carrots and needs a little help with the sums!",
        ),
        listOf(
            "$friend wants to write in the snow. Let's write together!",
            "$friend is making a sign for the mountain hut. Let's write on it!",
            "$friend found a frosty window. Let's write on it with a finger!",
            "$friend is writing a letter to the magpie. Let's help!",
        ),
        listOf(
            "$friend is the teacher today. Let's read together!",
            "$friend brought a new picture book. Let's read the words!",
            "$friend wrote words on the blackboard. Can you read them?",
            "$friend is playing a word game. Let's play too!",
        ),
    )[chapter][lesson % 4]
    override fun friendThanks(friend: String) = "$friend says thank you!"
    override fun friendName(a: Animal) = "The ${a.en}"

    override fun cliffhanger(chapter: Int) = listOf(
        "Look, a black and white feather! The magpie flew to the farm!",
        "Another feather by the fence! The magpie went to the river!",
        "Something shiny in the water... the library key! Let's go to the library!",
        "A note from the magpie: catch me at the market!",
        "The magpie flew up the mountain, with a bag of shiny things!",
        "From the top of the mountain we can see the forest school! Is the magpie there?",
        "",
    )[chapter]

    override val finale =
        "The magpie says sorry and gives everything back. Now she wants to go to school too! " +
            "Hupi is the wisest owl in the forest, thanks to you!"
    override val diplomaTitle = "Diploma"
    override fun diploma(name: String) =
        if (name.isBlank()) "You taught Hupi colours, numbers, letters and his first words!"
        else "$name taught Hupi colours, numbers, letters and his first words!"

    override fun letterName(l: Letter) = "the letter ${l.name}"
    override fun numberName(word: String) = "the number $word"
    override fun colorName(name: String) = name
    override val shapeNames = listOf("the circle", "the square", "the triangle", "the star", "the heart", "the diamond")
    override val shapeShort = listOf("circle", "square", "triangle", "star", "heart", "diamond")
    override fun wordName(word: String) = "the word $word"

    override fun meetNumber(word: String) = "This is the number $word!"
    override fun meetColor(name: String) = "This colour is $name!"
    override fun meetWord(word: String) = "This word says $word."
    override fun meetMath(kind: String) = when (kind) {
        "count" -> "Let's count!"
        "add" -> "Let's add! We put things together."
        "sub" -> "Let's take away!"
        else -> "Where are there more?"
    }
    override fun together(thing: String) = "Let's find $thing together. Look where Hupi points!"
    override fun forgot(thing: String) = "Oh no, Hupi forgot $thing! Can you help him remember?"
    override fun teachAsk(thing: String) = "Hupi thinks this is $thing. Is he right?"
    override val teachYesRight = "Yes! Hupi got it right, thanks to you!"
    override val teachCaught = "Well spotted! Hupi made a mistake. Show him the right one!"
    override fun teachMissedWrong(thing: String) = "Hmm, let's look again. That's not $thing. Let's show Hupi the right one!"
    override fun teachMissedRight(thing: String) = "This time Hupi was right! That is $thing."
    override fun teachThanks(thing: String) = "Thank you! Now Hupi knows $thing!"
    override val hupiCheers = listOf(
        "Hupi dances with joy!", "Hupi does a somersault!", "Hupi flaps his wings: hooray!",
        "Hupi hoots with joy: hoo-hoo!", "Hupi gives you a high five!",
    )
    override fun catchAsk(kind: String, short: String) = when (kind) {
        "letter" -> "Tap every letter $short!"
        "number" -> "Tap every number $short!"
        else -> "Tap all the $short dots!"
    }
    override val catchDone = "You caught them all!"
    override val magpieAppears = "Look! The magpie is back, with something shiny in her beak! Tap her to make her drop it!"
    override val magpieHits = listOf("Caw, caw!", "Hey, that tickles!", "Oh no, I'm dropping it!")
    override val magpieGone = "Hooray! The magpie dropped it and flew away!"
    override val chestAsk = "A treasure chest! Tap it to open it!"
    override val treasureFound = "Wow, a treasure! It goes in Hupi's house."
    override val showIntro = "It's Hupi's show! Let's see what he learned with you!"
    override val showApplause = "Well done, Hupi! And well done you, for teaching him!"
    private val items = listOf(
        "new feathers and a scarf", "a backpack", "glasses", "a book", "a little purse", "a pencil", "a graduation cap",
    )
    override fun gotItem(stage: Int) = "Hupi got ${items[(stage - 1).coerceIn(0, items.lastIndex)]}!"
    override val paywall = "To continue Hupi's adventure, ask a grown-up to help."
    override val notYet = "Not yet! First let's finish this one."
    override val mapHello = "Where shall we go today?"
    override val warmupIntro = "Good morning! Hupi is still sleepy. Wake him up with three answers!"
    override val warmupDone = "Hupi is wide awake! Thank you!"
    override val sceneReplay = "Let's play it again!"

    override val pathBanner = "Learn with Hupi"
    override fun pathIntro(chapter: Int) = listOf(
        "Let's learn colours and shapes with Hupi!",
        "Let's learn numbers with Hupi!",
        "Let's learn letters with Hupi!",
        "More letters! Let's learn them with Hupi!",
        "Let's count and do sums with Hupi!",
        "Let's write with Hupi!",
        "Let's read with Hupi!",
    )[chapter]
    override val pathDone = "Well done! You finished the whole path. Hupi learned so much with you!"
    override val pathLocked = "To learn more with Hupi, ask a grown-up to help."

    override val houseTitle = "Hupi's house"
    override val shelfLetters = "Letters"
    override val shelfNumbers = "Numbers"
    override val shelfColors = "Colours"
    override val shelfShapes = "Shapes"
    override val shelfWords = "Words"
    override val shelfMath = "Math"
    override val shelfTreasures = "Treasures"
    override val legend = "✨ learned   ·   🕸️ practising   ·   ▫️ not yet"
    override val scarf = "Hupi's scarf"
    override fun hupiLines(stage: Int) = buildList {
        add("Hoo hoo!")
        add("Hic! Oops, I have hiccups!")
        add("I'm Hupi!")
        if (stage >= 1) add("I know my colours!")
        if (stage >= 2) add("One, two, three, four, five!")
        if (stage >= 3) add("A, B, C!")
        if (stage >= 4) add("I can read a little!")
        if (stage >= 5) add("Two and two make four!")
        if (stage >= 6) add("I can write my name: H, U, P, I!")
        if (stage >= 7) add("I'm the wisest owl in the forest, thanks to you!")
    }

    override val ageTitle = "Child's age"
    override val ageHint = "Sets the starting level: 2–3 years Easy, 4–5 Medium, 6 Hard. The level can still be changed at any time."
    override val agePrompt = "For grown-ups: how old is the child?"
    override val years = "years"
    override val diaryTitle = "Learning diary"
    override fun storyAt(chapter: Int) = "Hupi's Adventure: chapter ${chapter + 1} of 7"
    override val storyFinished = "Hupi's Adventure: finished! 🎓"
    override val learnedLabel = "Learned"
    override val practisingLabel = "Practising"
    override val diaryNote = "Something counts as learned when the child gets it right on the first try on 3 different days."
    override fun mathName(key: String): String {
        val (kind, max) = key.split(":").let { it[0] to it.getOrElse(1) { "" } }
        return when (kind) {
            "count" -> "Counting to $max"
            "more" -> "Which has more (to $max)"
            "compare" -> "More or fewer (to $max)"
            "add" -> "Adding (to $max)"
            else -> "Taking away (to $max)"
        }
    }
}

object RomanianStory : StoryStrings {
    override val title = "Aventura lui Hupi"
    private val chapters = listOf("Cuibul", "Ferma", "Râul", "Biblioteca", "Piața", "Muntele", "Școala")
    override fun chapterTitle(chapter: Int) = chapters[chapter]
    override fun chapterLabel(chapter: Int) = "Capitolul ${chapter + 1}: ${chapters[chapter]}"

    override val hatchAsk = "Uite, un ou! Atinge-l ca să iasă puiul!"
    override val hatchCracks = listOf("Cric!", "Cric, crac!", "Aproape!")
    override val hatchHello = "Bună! Eu sunt Hupi, o bufniță mică. Încă nu știu nimic. Vrei să mă înveți tu?"
    override val magpieStory =
        "Vai! Coțofana cea năzdrăvană a luat culorile, cifrele și literele pădurii! Hai să le aducem înapoi împreună!"

    override fun chapterIntro(chapter: Int) = listOf(
        "În cuib totul e gri. Coțofana a furat culorile!",
        "Hupi zboară la fermă. Animalele nu mai știu să numere: coțofana a luat cifrele!",
        "La râu plutesc litere peste tot. Coțofana le-a scăpat!",
        "Biblioteca e vraiște! Cărțile și-au pierdut literele.",
        "La piață nimeni nu mai poate socoti fructele. Hai să ajutăm!",
        "Pe munte, coțofana a ascuns literele în zăpadă. Hai să le scriem din nou!",
        "În sfârșit, școala din pădure! Hupi vrea să citească primele cuvinte. Și uite... coțofana e aici!",
    )[chapter]

    override fun friendAsk(chapter: Int, lesson: Int, friend: String) = listOf(
        listOf(
            "$friend a pierdut culorile pădurii. Hai să-l ajutăm pe Hupi să le găsească!",
            "$friend vrea să picteze un curcubeu, dar borcanele cu vopsea sunt goale!",
            "$friend are niște flori care s-au făcut gri. Hai să le dăm culorile înapoi!",
            "$friend vede numai gri în jur. Hai să aducem culorile înapoi!",
        ),
        listOf(
            "$friend vrea să numere animalele, dar cifrele au dispărut!",
            "$friend nu mai știe câte ouă sunt în coș. Hai să numărăm!",
            "$friend a pierdut numerele de pe ușa grajdului. Hai să le găsim!",
            "$friend vrea să se joace de-a v-ați ascunselea, dar nu știe să numere. Hai să ajutăm!",
        ),
        listOf(
            "$friend a găsit litere care plutesc pe apă. Hai să vedem care sunt!",
            "$friend pescuiește și, uite, litere în cârlig! Care sunt?",
            "$friend a văzut litere sărind ca peștișorii. Hai să le prindem!",
            "$friend a găsit o sticlă cu litere înăuntru. Hai s-o deschidem!",
        ),
        listOf(
            "$friend are o carte căreia îi lipsesc litere. Hai să le punem la loc!",
            "$friend vrea să citească o poveste, dar literele au fugit!",
            "$friend a găsit litere sub rafturi. Hai să le punem în ordine!",
            "$friend are grijă azi de bibliotecă și are nevoie de ajutor cu literele!",
        ),
        listOf(
            "$friend vinde fructe la piață. Hai să socotim împreună!",
            "$friend are un coș plin cu mere. Hai să socotim!",
            "$friend vrea să împartă fructele cu prietenii. Hai să ajutăm!",
            "$friend cumpără morcovi și are nevoie de puțin ajutor la socoteli!",
        ),
        listOf(
            "$friend vrea să scrie pe zăpadă. Hai să scriem împreună!",
            "$friend face o tăbliță pentru cabana de pe munte. Hai să scriem pe ea!",
            "$friend a găsit o fereastră înghețată. Hai să scriem pe ea cu degetul!",
            "$friend îi scrie o scrisoare coțofenei. Hai să ajutăm!",
        ),
        listOf(
            "$friend ține lecția azi. Hai să citim împreună!",
            "$friend a adus o carte nouă cu poze. Hai să citim cuvintele!",
            "$friend a scris cuvinte pe tablă. Le poți citi?",
            "$friend se joacă un joc cu cuvinte. Hai să ne jucăm și noi!",
        ),
    )[chapter][lesson % 4]
    override fun friendThanks(friend: String) = "$friend îți mulțumește!"
    override fun friendName(a: Animal) = a.ro.replaceFirstChar { it.uppercase() }

    override fun cliffhanger(chapter: Int) = listOf(
        "Uite, o pană albă cu negru! Coțofana a zburat spre fermă!",
        "Încă o pană lângă gard! Coțofana a plecat spre râu!",
        "Ceva strălucește în apă... cheia bibliotecii! Hai la bibliotecă!",
        "Un bilet de la coțofană: prinde-mă la piață!",
        "Coțofana a zburat spre munte, cu un sac de lucruri strălucitoare!",
        "Din vârful muntelui se vede școala din pădure! Oare coțofana e acolo?",
        "",
    )[chapter]

    override val finale =
        "Coțofana își cere iertare și dă totul înapoi. Acum vrea și ea să meargă la școală! " +
            "Hupi este cea mai isteață bufniță din pădure, datorită ție!"
    override val diplomaTitle = "Diplomă"
    override fun diploma(name: String) =
        if (name.isBlank()) "L-ai învățat pe Hupi culorile, cifrele, literele și primele cuvinte!"
        else "$name l-a învățat pe Hupi culorile, cifrele, literele și primele cuvinte!"

    override fun letterName(l: Letter) = "litera ${l.name}"
    override fun numberName(word: String) = "numărul $word"
    override fun colorName(name: String) = "culoarea $name"
    override val shapeNames = listOf("cercul", "pătratul", "triunghiul", "steaua", "inima", "rombul")
    override val shapeShort = listOf("cerc", "pătrat", "triunghi", "stea", "inimă", "romb")
    override fun wordName(word: String) = "cuvântul $word"

    override fun meetNumber(word: String) = "Acesta este numărul $word!"
    override fun meetColor(name: String) = "Aceasta este culoarea $name!"
    override fun meetWord(word: String) = "Aici scrie $word."
    override fun meetMath(kind: String) = when (kind) {
        "count" -> "Hai să numărăm!"
        "add" -> "Hai să adunăm! Punem lucrurile laolaltă."
        "sub" -> "Hai să scădem!"
        else -> "Unde sunt mai multe?"
    }
    override fun together(thing: String) = "Hai să găsim împreună $thing. Uite unde arată Hupi!"
    override fun forgot(thing: String) = "Vai, Hupi a uitat $thing! Îl ajuți să-și amintească?"
    override fun teachAsk(thing: String) = "Hupi crede că aici este $thing. Are dreptate?"
    override val teachYesRight = "Da! Hupi a răspuns corect, datorită ție!"
    override val teachCaught = "Bine văzut! Hupi a greșit. Arată-i răspunsul corect!"
    override fun teachMissedWrong(thing: String) = "Hmm, hai să ne uităm din nou. Nu este $thing. Hai să-i arătăm lui Hupi răspunsul corect!"
    override fun teachMissedRight(thing: String) = "De data asta Hupi a avut dreptate! Aici este $thing."
    override fun teachThanks(thing: String) = "Mulțumesc! Acum Hupi știe $thing!"
    override val hupiCheers = listOf(
        "Hupi dansează de bucurie!", "Hupi face o tumbă de bucurie!", "Hupi bate din aripi: ura!",
        "Hupi strigă de bucurie: hu-hu!", "Hupi bate palma cu tine!",
    )
    override fun catchAsk(kind: String, short: String) = when (kind) {
        "letter" -> "Atinge toate literele $short!"
        "number" -> "Atinge toate numerele $short!"
        else -> "Atinge toate bulinele de culoare $short!"
    }
    override val catchDone = "Le-ai prins pe toate!"
    override val magpieAppears = "Uite! Coțofana s-a întors, cu ceva strălucitor în cioc! Atinge-o ca să-l scape!"
    override val magpieHits = listOf("Cra, cra!", "Hei, mă gâdili!", "Vai, îl scap!")
    override val magpieGone = "Ura! Coțofana l-a scăpat și a zburat!"
    override val chestAsk = "Un cufăr cu comori! Atinge-l ca să-l deschizi!"
    override val treasureFound = "Ura, o comoară! O ducem în căsuța lui Hupi."
    override val showIntro = "Este spectacolul lui Hupi! Hai să vedem ce a învățat cu tine!"
    override val showApplause = "Bravo, Hupi! Și bravo ție, că l-ai învățat!"
    private val items = listOf(
        "pene noi și un fular", "un ghiozdan", "ochelari", "o carte", "o punguță", "un creion", "o pălărie de absolvent",
    )
    override fun gotItem(stage: Int) = "Hupi a primit ${items[(stage - 1).coerceIn(0, items.lastIndex)]}!"
    override val paywall = "Ca să continui aventura lui Hupi, roagă un adult să te ajute."
    override val notYet = "Încă nu! Mai întâi o terminăm pe aceasta."
    override val mapHello = "Unde mergem azi?"
    override val warmupIntro = "Bună dimineața! Hupi e încă somnoros. Trezește-l cu trei răspunsuri!"
    override val warmupDone = "Hupi s-a trezit de-a binelea! Mulțumesc!"
    override val sceneReplay = "Hai să ne jucăm din nou!"

    override val pathBanner = "Învață cu Hupi"
    override fun pathIntro(chapter: Int) = listOf(
        "Hai să învățăm culorile și formele cu Hupi!",
        "Hai să învățăm numerele cu Hupi!",
        "Hai să învățăm literele cu Hupi!",
        "Și mai multe litere! Hai să le învățăm cu Hupi!",
        "Hai să socotim cu Hupi!",
        "Hai să scriem cu Hupi!",
        "Hai să citim cu Hupi!",
    )[chapter]
    override val pathDone = "Bravo! Ai terminat tot drumul. Hupi a învățat atât de multe cu tine!"
    override val pathLocked = "Ca să înveți mai multe cu Hupi, roagă un adult să te ajute."

    override val houseTitle = "Căsuța lui Hupi"
    override val shelfLetters = "Litere"
    override val shelfNumbers = "Numere"
    override val shelfColors = "Culori"
    override val shelfShapes = "Forme"
    override val shelfWords = "Cuvinte"
    override val shelfMath = "Matematică"
    override val shelfTreasures = "Comori"
    override val legend = "✨ învățat   ·   🕸️ în exercițiu   ·   ▫️ încă nu"
    override val scarf = "Fularul lui Hupi"
    override fun hupiLines(stage: Int) = buildList {
        add("Hu-hu!")
        add("Sughiț! Ups, am sughiț!")
        add("Eu sunt Hupi!")
        if (stage >= 1) add("Știu culorile!")
        if (stage >= 2) add("Unu, doi, trei, patru, cinci!")
        if (stage >= 3) add("A, be, ce!")
        if (stage >= 4) add("Știu să citesc puțin!")
        if (stage >= 5) add("Doi plus doi fac patru!")
        if (stage >= 6) add("Știu să-mi scriu numele: haș, u, pe, i!")
        if (stage >= 7) add("Sunt cea mai isteață bufniță din pădure, datorită ție!")
    }

    override val ageTitle = "Vârsta copilului"
    override val ageHint = "Stabilește nivelul de pornire: 2–3 ani Ușor, 4–5 ani Mediu, 6 ani Greu. Nivelul se poate schimba oricând."
    override val agePrompt = "Pentru adulți: câți ani are copilul?"
    override val years = "ani"
    override val diaryTitle = "Jurnalul învățării"
    override fun storyAt(chapter: Int) = "Aventura lui Hupi: capitolul ${chapter + 1} din 7"
    override val storyFinished = "Aventura lui Hupi: terminată! 🎓"
    override val learnedLabel = "Învățate"
    override val practisingLabel = "În exercițiu"
    override val diaryNote = "Un lucru e considerat învățat când copilul răspunde corect din prima încercare în 3 zile diferite."
    override fun mathName(key: String): String {
        val (kind, max) = key.split(":").let { it[0] to it.getOrElse(1) { "" } }
        return when (kind) {
            "count" -> "Numărat până la $max"
            "more" -> "Unde sunt mai multe (până la $max)"
            "compare" -> "Mai multe sau mai puține (până la $max)"
            "add" -> "Adunare (până la $max)"
            else -> "Scădere (până la $max)"
        }
    }
}
