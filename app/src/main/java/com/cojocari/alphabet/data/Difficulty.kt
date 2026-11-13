package com.cojocari.alphabet.data

/** The level chosen at the top of every game. Each game turns it into its own settings. */
enum class Difficulty {
    Easy, Medium, Hard;

    /** Picks the value for this level, e.g. `d.pick(5, 10, 20)` for the biggest number. */
    fun <T> pick(easy: T, medium: T, hard: T): T = when (this) {
        Easy -> easy
        Medium -> medium
        Hard -> hard
    }
}
