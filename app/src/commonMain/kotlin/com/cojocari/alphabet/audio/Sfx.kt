package com.cojocari.alphabet.audio

import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.platform.playPcm
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private const val Rate = 22050

/** A note: frequency in Hz (0 = rest) and length in milliseconds. */
private class Note(val hz: Float, val ms: Int)

/**
 * Little game sounds (pop, chime, boop, fanfare), synthesised on the fly so the app ships
 * no audio files. [enabled] is checked on every play so the parent setting applies at once.
 */
class Sfx(private val enabled: () -> Boolean) {
    private val pop = render(listOf(Note(700f, 35), Note(1400f, 45)), volume = 0.30f)
    private val tap = render(listOf(Note(1046f, 40)), volume = 0.18f)
    private val ding = render(listOf(Note(784f, 90), Note(1047f, 90), Note(1319f, 220)))
    private val boop = render(listOf(Note(330f, 110), Note(247f, 180)), volume = 0.25f)
    private val flip = render(listOf(Note(600f, 30), Note(900f, 30)), volume = 0.15f)
    private val fanfare = render(
        listOf(Note(523f, 110), Note(659f, 110), Note(784f, 110), Note(0f, 40), Note(659f, 90), Note(1047f, 380))
    )

    fun pop() = play(pop)
    fun tap() = play(tap)
    fun success() = play(ding)
    fun wrong() = play(boop)
    fun flip() = play(flip)
    fun fanfare() = play(fanfare)

    private fun play(samples: ShortArray) {
        if (enabled()) playPcm(samples, Rate)
    }

    /** Soft sine notes with a quick attack and a gentle decay, like a toy xylophone. */
    private fun render(notes: List<Note>, volume: Float = 0.28f): ShortArray {
        val out = ArrayList<Short>()
        for (n in notes) {
            val count = Rate * n.ms / 1000
            for (i in 0 until count) {
                val t = i.toDouble() / Rate
                val attack = (i / (Rate * 0.005)).coerceAtMost(1.0)
                val decay = exp(-3.0 * i / count)
                val wave = if (n.hz == 0f) 0.0 else
                    sin(2 * PI * n.hz * t) + 0.3 * sin(4 * PI * n.hz * t) // a touch of overtone
                out += (wave / 1.3 * attack * decay * volume * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }
}

val LocalSfx = staticCompositionLocalOf<Sfx> { error("No Sfx provided") }
