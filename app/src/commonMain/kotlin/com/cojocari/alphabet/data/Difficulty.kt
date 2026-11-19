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

    companion object {
        /** Starting level for a child of [age]: 2–3 Easy, 4–5 Medium, 6 and up Hard. */
        fun forAge(age: Int): Difficulty = when {
            age <= 3 -> Easy
            age <= 5 -> Medium
            else -> Hard
        }
    }
}
