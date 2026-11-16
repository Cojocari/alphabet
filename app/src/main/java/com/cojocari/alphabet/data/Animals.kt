package com.cojocari.alphabet.data

import androidx.compose.ui.graphics.Color

enum class Habitat { Farm, Wild }

/**
 * An animal for the Animals world. [key] names its sound: a recording in
 * `res/raw/animal_<key>` is used if present, otherwise it is synthesised
 * (see [com.cojocari.alphabet.audio.AnimalSynth]).
 * Romanian names carry the definite article ("vaca"), as said to children.
 */
class Animal(
    val key: String,
    val emoji: String,
    val habitat: Habitat,
    val color: Color,
    val en: String,
    val enSound: String,
    val ro: String,
    val roSound: String,
)

private val Red = Color(0xFFE63946)
private val Orange = Color(0xFFF77F00)
private val Green = Color(0xFF2A9D8F)
private val Blue = Color(0xFF3A86FF)
private val Purple = Color(0xFF8338EC)
private val Pink = Color(0xFFFF006E)
private val Brown = Color(0xFF9C6644)

val Animals = listOf(
    Animal("cow", "🐄", Habitat.Farm, Brown, "cow", "Moo!", "vaca", "Muu!"),
    Animal("pig", "🐖", Habitat.Farm, Pink, "pig", "Oink oink!", "porcul", "Guiț-guiț!"),
    Animal("sheep", "🐑", Habitat.Farm, Blue, "sheep", "Baa!", "oaia", "Beee!"),
    Animal("goat", "🐐", Habitat.Farm, Green, "goat", "Maa!", "capra", "Meee!"),
    Animal("horse", "🐎", Habitat.Farm, Brown, "horse", "Neigh!", "calul", "Ihahaha!"),
    Animal("hen", "🐔", Habitat.Farm, Red, "hen", "Cluck cluck!", "găina", "Cotcodac!"),
    Animal("rooster", "🐓", Habitat.Farm, Orange, "rooster", "Cock-a-doodle-doo!", "cocoșul", "Cucurigu!"),
    Animal("chick", "🐤", Habitat.Farm, Orange, "chick", "Cheep cheep!", "puiul", "Piu-piu!"),
    Animal("duck", "🦆", Habitat.Farm, Green, "duck", "Quack quack!", "rața", "Mac-mac!"),
    Animal("donkey", "🫏", Habitat.Farm, Brown, "donkey", "Hee-haw!", "măgarul", "I-ha!"),
    Animal("dog", "🐕", Habitat.Farm, Brown, "dog", "Woof woof!", "câinele", "Ham-ham!"),
    Animal("cat", "🐈", Habitat.Farm, Purple, "cat", "Meow!", "pisica", "Miau!"),
    Animal("mouse", "🐁", Habitat.Farm, Blue, "mouse", "Squeak squeak!", "șoricelul", "Chiț-chiț!"),

    Animal("lion", "🦁", Habitat.Wild, Orange, "lion", "Roar!", "leul", "Rrraaa!"),
    Animal("elephant", "🐘", Habitat.Wild, Blue, "elephant", "Toot toot!", "elefantul", "Tuuu!"),
    Animal("monkey", "🐒", Habitat.Wild, Brown, "monkey", "Ooh ooh ah ah!", "maimuța", "U-u-a-a!"),
    Animal("wolf", "🐺", Habitat.Wild, Purple, "wolf", "Awoo!", "lupul", "Auuu!"),
    Animal("bear", "🐻", Habitat.Wild, Brown, "bear", "Grrr!", "ursul", "Mrrr!"),
    Animal("tiger", "🐅", Habitat.Wild, Orange, "tiger", "Grrr!", "tigrul", "Grrr!"),
    Animal("owl", "🦉", Habitat.Wild, Purple, "owl", "Hoo hoo!", "bufnița", "Hu-hu!"),
    Animal("snake", "🐍", Habitat.Wild, Green, "snake", "Hiss!", "șarpele", "Sss!"),
    Animal("frog", "🐸", Habitat.Wild, Green, "frog", "Ribbit!", "broasca", "Oac-oac!"),
    Animal("bee", "🐝", Habitat.Wild, Orange, "bee", "Buzz!", "albina", "Bzzz!"),
    Animal("bird", "🐦", Habitat.Wild, Blue, "bird", "Tweet tweet!", "păsărica", "Cip-cirip!"),
    Animal("cricket", "🦗", Habitat.Wild, Green, "cricket", "Chirp chirp!", "greierele", "Cri-cri!"),
    Animal("parrot", "🦜", Habitat.Wild, Red, "parrot", "Squawk!", "papagalul", "Cra-cra!"),
    Animal("whale", "🐋", Habitat.Wild, Blue, "whale", "Ooooo!", "balena", "Uuuu!"),
    Animal("dolphin", "🐬", Habitat.Wild, Blue, "dolphin", "Eee-eee!", "delfinul", "Ii-ii!"),
)

/** Wild animals open in the free demo; the farm is free entirely. */
val DemoWildAnimals = setOf("lion", "elephant", "monkey", "wolf")
