package com.cojocari.alphabet.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Cartoon animal sounds, synthesised in code so the app ships no third-party recordings.
 * Each animal is a small recipe: a tone (or noise) with a pitch curve, shaped by vowel-like
 * resonances ("formants"), an envelope and a few effects (wobble, growl, echo).
 */
internal object AnimalSynth {
    const val Rate = 22050
    private const val TwoPi = 2 * PI
    private val rnd = kotlin.random.Random(7)

    fun render(key: String): ShortArray = toPcm(
        when (key) {
            "cow" -> cow()
            "pig" -> pig()
            "sheep" -> bleat(1.0, Curve(0, 330, 0.2, 360, 1, 300), 0.07, 8.0, 700.0, 1500.0, 9.0)
            "goat" -> bleat(0.85, Curve(0, 430, 0.3, 450, 1, 380), 0.09, 10.0, 600.0, 1800.0, 11.0)
            "horse" -> horse()
            "hen" -> hen()
            "rooster" -> rooster()
            "chick" -> chirps(4, 0.22, 0.11, Curve(0, 3000, 0.4, 4200, 1, 3300))
            "duck" -> duck()
            "turkey" -> turkey()
            "dog" -> dog()
            "cat" -> cat()
            "mouse" -> chirps(3, 0.16, 0.09, Curve(0, 3200, 0.5, 4000, 1, 3400))
            "lion" -> growl(1.8, Curve(0, 85, 0.3, 120, 0.7, 100, 1, 70), 650.0, 1150.0, 28.0, 0.35, roar = true)
            "tiger" -> growl(1.1, Curve(0, 70, 0.5, 90, 1, 65), 420.0, 900.0, 24.0, 0.6)
            "bear" -> growl(1.4, Curve(0, 55, 0.4, 70, 1, 50), 330.0, 720.0, 18.0, 0.6)
            "elephant" -> elephant()
            "monkey" -> monkey()
            "wolf" -> wolf()
            "owl" -> owl()
            "snake" -> snake()
            "frog" -> frog()
            "bee" -> bee()
            "bird" -> bird()
            "cricket" -> cricket()
            "parrot" -> parrot()
            "whale" -> whale()
            "dolphin" -> dolphin()
            else -> chirps(2, 0.2, 0.1, c(1000))
        }
    )

    // ---------------------------------------------------------------- recipes

    private fun cow() = voice(
        1.5, Curve(0, 105, 0.25, 122, 0.8, 112, 1, 92),
        f1 = Curve(0, 260, 0.3, 340, 1, 300), f2 = Curve(0, 850, 0.35, 780, 1, 700), f3 = c(2400),
        amp = Curve(0, 0, 0.12, 1, 0.8, 0.85, 1, 0), vib = 0.01, vibRate = 4.0, jitter = 0.01, breath = 0.05, q = 6.0,
    ).let { echo(it, 0.09, 0.25, 0.25) }

    private fun pig(): DoubleArray {
        fun grunt(up: Double) = voice(
            0.24, Curve(0, 140 * up, 1, 200 * up), f1 = c(520.0), f2 = c(1350.0), f3 = c(2400.0),
            amp = Curve(0, 0, 0.08, 1, 0.6, 0.8, 1, 0), jitter = 0.08, breath = 0.35, q = 3.0,
        ).tremolo(38.0, 0.5).drive(2.0)
        return seq(1.0, 0.0 to grunt(1.0), 0.3 to grunt(1.1), 0.62 to grunt(0.95))
    }

    private fun bleat(sec: Double, pitch: Curve, vib: Double, vibRate: Double, f1: Double, f2: Double, trem: Double) =
        voice(
            sec, pitch, f1 = c(f1), f2 = c(f2), f3 = c(2700.0),
            amp = Curve(0, 0, 0.05, 1, 0.7, 0.8, 1, 0), vib = vib, vibRate = vibRate, breath = 0.15, q = 4.0,
        ).tremolo(trem, 0.6)

    private fun horse(): DoubleArray {
        val whinny = voice(
            1.4, Curve(0, 700, 0.1, 950, 0.6, 700, 1, 420),
            f1 = Curve(0, 850, 1, 600), f2 = c(1600.0), f3 = c(2800.0),
            amp = Curve(0, 0, 0.05, 1, 0.7, 0.7, 1, 0), vib = 0.08, vibRate = 11.0, breath = 0.2, q = 4.0,
        )
        val snort = noise(0.3).filtered(Filter.Band, 600.0, 1.0).shape(Curve(0, 0, 0.1, 1, 1, 0))
        return echo(seq(1.8, 0.0 to whinny, 1.45 to scale(snort, 0.6)), 0.08, 0.2, 0.2)
    }

    private fun hen(): DoubleArray {
        fun cluck() = voice(
            0.08, Curve(0, 380, 1, 330), f1 = c(900.0), f2 = c(1700.0), f3 = c(2700.0),
            amp = Curve(0, 0, 0.1, 1, 1, 0), breath = 0.2, q = 4.0,
        )
        val daac = voice(
            0.35, Curve(0, 420, 0.5, 620, 1, 560), f1 = c(800.0), f2 = c(1500.0), f3 = c(2600.0),
            amp = Curve(0, 0, 0.1, 1, 0.7, 0.8, 1, 0), breath = 0.2, q = 4.0,
        )
        return seq(1.0, 0.0 to cluck(), 0.14 to cluck(), 0.28 to cluck(), 0.42 to cluck(), 0.6 to daac)
    }

    private fun rooster(): DoubleArray {
        fun part(sec: Double, pitch: Curve, vib: Double = 0.0) = voice(
            sec, pitch, f1 = c(850.0), f2 = c(1600.0), f3 = c(2700.0),
            amp = Curve(0, 0, 0.1, 1, 0.75, 0.9, 1, 0), vib = vib, vibRate = 7.0, jitter = 0.02, breath = 0.2, q = 4.0,
        )
        return seq(
            1.4,
            0.0 to part(0.16, Curve(0, 480, 1, 560)),
            0.2 to part(0.12, Curve(0, 620, 1, 600)),
            0.36 to part(0.12, Curve(0, 650, 1, 630)),
            0.52 to part(0.8, Curve(0, 700, 0.2, 770, 1, 620), vib = 0.03),
        ).drive(1.5)
    }

    private fun duck(): DoubleArray {
        fun quack(up: Double) = voice(
            0.22, Curve(0, 260 * up, 1, 215 * up), f1 = c(1100.0), f2 = c(1900.0), f3 = c(2900.0),
            amp = Curve(0, 0, 0.05, 1, 0.5, 0.8, 1, 0), jitter = 0.03, breath = 0.1, q = 9.0,
        ).drive(2.0)
        return seq(0.9, 0.0 to quack(1.0), 0.3 to quack(1.05), 0.6 to quack(0.95))
    }

    private fun turkey() = voice(
        0.9, Curve(0, 650, 1, 520), f1 = c(700.0), f2 = c(1400.0), f3 = c(2600.0),
        amp = Curve(0, 0, 0.05, 1, 0.8, 0.8, 1, 0), vib = 0.15, vibRate = 16.0, breath = 0.15, q = 4.0,
    ).tremolo(16.0, 0.8)

    private fun dog(): DoubleArray {
        fun bark() = voice(
            0.2, Curve(0, 320, 0.25, 380, 1, 200), f1 = Curve(0, 620, 1, 450), f2 = c(1300.0), f3 = c(2600.0),
            amp = Curve(0, 0, 0.04, 1, 0.3, 0.6, 1, 0), jitter = 0.03, breath = 0.3, q = 3.0,
        ).drive(2.0)
        return echo(seq(0.6, 0.0 to bark(), 0.32 to bark()), 0.06, 0.2, 0.2)
    }

    private fun cat() = voice(
        1.0, Curve(0, 520, 0.35, 780, 0.7, 700, 1, 480),
        f1 = Curve(0, 350, 0.4, 900, 1, 500), f2 = Curve(0, 2300, 0.4, 1500, 1, 1000), f3 = c(3000.0),
        amp = Curve(0, 0, 0.1, 0.9, 0.5, 1, 0.85, 0.7, 1, 0), vib = 0.015, vibRate = 6.0, breath = 0.08, q = 6.0,
    )

    private fun growl(
        sec: Double, pitch: Curve, f1: Double, f2: Double, rattle: Double, depth: Double, roar: Boolean = false,
    ): DoubleArray {
        val src = tone(sec, pitch, Wave.Saw, jitter = 0.12)
        val n = noise(sec)
        for (i in src.indices) src[i] = src[i] * 0.6 + n[i] * 0.5
        val body = formants(
            src,
            listOf(
                Formant(if (roar) Curve(0, 450, 0.3, f1, 1, 500) else c(f1), 2.5, 1.0),
                Formant(if (roar) Curve(0, 900, 0.3, f2, 1, 900) else c(f2), 2.5, 0.7),
                Formant(c(2400.0), 2.5, 0.3),
            ),
            direct = 0.15,
        )
        val amp = if (roar) Curve(0, 0, 0.25, 1, 0.6, 0.85, 1, 0) else Curve(0, 0, 0.15, 1, 0.75, 0.9, 1, 0)
        val out = body.tremolo(rattle, depth).drive(2.5).filtered(Filter.Low, 3000.0, 0.7).shape(amp)
        return echo(out, 0.07, 0.3, 0.3)
    }

    private fun elephant(): DoubleArray {
        val sec = 1.3
        val src = tone(sec, Curve(0, 450, 0.15, 700, 0.7, 640, 1, 500), Wave.Saw, vib = 0.03, vibRate = 7.0, jitter = 0.03)
        val n = noise(sec)
        for (i in src.indices) src[i] = src[i] * 0.85 + n[i] * 0.15
        val out = formants(
            src,
            listOf(Formant(c(1100.0), 4.0, 1.0), Formant(c(2300.0), 4.0, 0.8), Formant(c(3200.0), 4.0, 0.4)),
            direct = 0.3,
        ).drive(3.0).shape(Curve(0, 0, 0.08, 1, 0.75, 0.9, 1, 0))
        return echo(out, 0.11, 0.3, 0.3)
    }

    private fun monkey(): DoubleArray {
        fun call(sec: Double, pitch: Curve, f1: Double, f2: Double) = voice(
            sec, pitch, f1 = c(f1), f2 = c(f2), f3 = c(2600.0),
            amp = Curve(0, 0, 0.1, 1, 0.7, 0.8, 1, 0), breath = 0.15, q = 4.0,
        ).drive(1.5)
        return seq(
            1.05,
            0.0 to call(0.18, Curve(0, 450, 1, 650), 350.0, 800.0),
            0.24 to call(0.18, Curve(0, 500, 1, 700), 350.0, 800.0),
            0.5 to call(0.22, Curve(0, 700, 1, 950), 850.0, 1300.0),
            0.8 to call(0.22, Curve(0, 750, 1, 1000), 850.0, 1300.0),
        )
    }

    private fun wolf() = voice(
        2.4, Curve(0, 330, 0.2, 520, 0.75, 545, 1, 400),
        f1 = Curve(0, 350, 0.2, 600, 1, 420), f2 = Curve(0, 800, 0.2, 1100, 1, 850), f3 = c(2500.0),
        amp = Curve(0, 0, 0.18, 1, 0.8, 0.9, 1, 0), vib = 0.015, vibRate = 5.0, breath = 0.1, q = 5.0,
    ).let { echo(it, 0.13, 0.35, 0.4) }

    private fun owl(): DoubleArray {
        fun hoo(sec: Double, pitch: Curve) =
            tone(sec, pitch, Wave.Sine, harmonic = 0.15).shape(Curve(0, 0, 0.2, 1, 0.7, 0.8, 1, 0))
        return echo(
            seq(1.0, 0.0 to hoo(0.3, Curve(0, 420, 1, 400)), 0.45 to hoo(0.55, Curve(0, 430, 0.3, 440, 1, 390))),
            0.15, 0.3, 0.35,
        )
    }

    private fun snake() = noise(2.5)
        .filtered(Filter.High, 3500.0, 0.7)
        .filtered(Filter.Low, 9000.0, 0.7)
        .tremolo(3.0, 0.2)
        .shape(Curve(0, 0, 0.2, 1, 0.8, 0.9, 1, 0))

    private fun frog(): DoubleArray {
        fun croak(sec: Double, rate: Double): DoubleArray {
            val src = tone(sec, c(rate), Wave.Pulse)
            val a = src.copyOf().filtered(Filter.Band, 900.0, 3.0)
            val b = src.filtered(Filter.Band, 2000.0, 4.0)
            for (i in a.indices) a[i] += b[i] * 0.6
            return a.shape(Curve(0, 0, 0.1, 1, 0.8, 0.9, 1, 0))
        }
        fun ribbit() = seq(0.26, 0.0 to croak(0.12, 45.0), 0.17 to croak(0.09, 60.0))
        return seq(1.0, 0.0 to ribbit(), 0.6 to ribbit())
    }

    private fun bee(): DoubleArray {
        val src = tone(1.8, Curve(0, 210, 0.5, 245, 1, 200), Wave.Saw, vib = 0.03, vibRate = 3.0)
        val a = src.copyOf().filtered(Filter.Band, 600.0, 1.0)
        val b = src.filtered(Filter.Band, 1800.0, 1.0)
        for (i in a.indices) a[i] += b[i] * 0.7
        return a.tremolo(4.0, 0.4).shape(Curve(0, 0, 0.2, 1, 0.8, 1, 1, 0))
    }

    private fun bird(): DoubleArray {
        fun note(sec: Double, from: Double, to: Double) =
            tone(sec, Curve(0, from, 1, to), Wave.Sine, harmonic = 0.1).shape(Curve(0, 0, 0.15, 1, 0.7, 0.7, 1, 0))
        val parts = mutableListOf(0.0 to note(0.07, 2600.0, 4200.0), 0.1 to note(0.07, 2600.0, 4200.0))
        for (k in 0 until 6) parts += (0.24 + k * 0.06) to note(0.045, 4500.0, 3500.0)
        parts += 0.66 to note(0.16, 3000.0, 5000.0)
        return seq(0.9, *parts.toTypedArray())
    }

    private fun cricket(): DoubleArray {
        val pulse = tone(0.018, c(4400.0), Wave.Sine).shape(Curve(0, 0, 0.2, 1, 0.8, 1, 1, 0))
        val parts = mutableListOf<Pair<Double, DoubleArray>>()
        for (g in 0 until 4) for (p in 0 until 3) parts += (g * 0.35 + p * 0.03) to pulse
        return seq(1.25, *parts.toTypedArray())
    }

    private fun parrot(): DoubleArray {
        fun squawk(sec: Double): DoubleArray {
            val src = tone(sec, Curve(0, 700, 0.3, 1100, 1, 800), Wave.Saw, jitter = 0.1)
            val n = noise(sec)
            for (i in src.indices) src[i] = src[i] * 0.6 + n[i] * 0.4
            return formants(src, listOf(Formant(c(1300.0), 3.0, 1.0), Formant(c(2600.0), 3.0, 0.8)), direct = 0.2)
                .drive(4.0).shape(Curve(0, 0, 0.05, 1, 0.7, 0.8, 1, 0))
        }
        return seq(0.9, 0.0 to squawk(0.45), 0.58 to squawk(0.3))
    }

    private fun whale(): DoubleArray {
        val song = tone(
            2.6, Curve(0, 180, 0.3, 420, 0.55, 380, 0.8, 520, 1, 260), Wave.Sine, vib = 0.02, vibRate = 3.0, harmonic = 0.25,
        ).shape(Curve(0, 0, 0.25, 1, 0.75, 0.8, 1, 0))
        return echo(song, 0.3, 0.45, 0.5)
    }

    private fun dolphin(): DoubleArray {
        val click = tone(0.004, c(7000.0), Wave.Sine).shape(Curve(0, 1, 1, 0))
        val parts = mutableListOf<Pair<Double, DoubleArray>>()
        var t = 0.0
        var gap = 0.06
        repeat(12) { parts += t to click; t += gap; gap *= 0.85 }
        fun whistle(sec: Double, pitch: Curve) =
            tone(sec, pitch, Wave.Sine).shape(Curve(0, 0, 0.1, 1, 0.8, 0.9, 1, 0))
        parts += 0.5 to whistle(0.55, Curve(0, 6000, 0.4, 9000, 0.7, 8000, 1, 9500))
        parts += 1.15 to whistle(0.35, Curve(0, 7000, 0.5, 9500, 1, 8500))
        return seq(1.6, *parts.toTypedArray())
    }

    private fun chirps(count: Int, every: Double, sec: Double, pitch: Curve): DoubleArray {
        val one = tone(sec, pitch, Wave.Sine, harmonic = 0.1).shape(Curve(0, 0, 0.15, 1, 0.7, 0.6, 1, 0))
        return seq(every * (count - 1) + sec, *Array(count) { (it * every) to one })
    }

    // ---------------------------------------------------------------- building blocks

    /** A piecewise-linear curve over 0..1: pairs of (position, value). */
    private class Curve(vararg points: Number) {
        private val p = points.map { it.toDouble() }

        operator fun invoke(x: Double): Double {
            if (x <= p[0]) return p[1]
            var i = 0
            while (i + 3 < p.size) {
                val x0 = p[i]
                val x1 = p[i + 2]
                if (x <= x1) return p[i + 1] + (p[i + 3] - p[i + 1]) * (x - x0) / (x1 - x0)
                i += 2
            }
            return p[p.size - 1]
        }
    }

    private fun c(v: Number) = Curve(0, v, 1, v)

    private enum class Wave { Sine, Saw, Pulse }

    /**
     * An oscillator following [pitch] (Hz over the sound's length), with [vib] vibrato
     * (fraction of pitch at [vibRate] Hz) and slow random [jitter] for a rough, living voice.
     */
    private fun tone(
        sec: Double, pitch: Curve, wave: Wave, vib: Double = 0.0, vibRate: Double = 5.0,
        jitter: Double = 0.0, harmonic: Double = 0.0,
    ): DoubleArray {
        val out = DoubleArray((sec * Rate).toInt())
        var ph = 0.0
        var jit = 0.0
        var target = 0.0
        for (i in out.indices) {
            val x = i.toDouble() / out.size
            if (jitter > 0 && i % 96 == 0) target = (rnd.nextDouble() * 2 - 1) * jitter
            jit += (target - jit) * 0.02
            val f = pitch(x) * (1 + vib * sin(TwoPi * vibRate * i / Rate) + jit)
            val dt = f / Rate
            ph += dt
            ph -= floor(ph)
            out[i] = when (wave) {
                Wave.Sine -> sin(TwoPi * ph) + harmonic * sin(2 * TwoPi * ph)
                Wave.Saw -> 2 * ph - 1 - blep(ph, dt)
                Wave.Pulse -> if (ph < 0.08) 1.0 else -0.087
            }
        }
        return out
    }

    /** Smooths the saw's jump to avoid harsh aliasing. */
    private fun blep(t: Double, dt: Double): Double = when {
        t < dt -> { val x = t / dt; x + x - x * x - 1 }
        t > 1 - dt -> { val x = (t - 1) / dt; x * x + x + x + 1 }
        else -> 0.0
    }

    private fun noise(sec: Double) = DoubleArray((sec * Rate).toInt()) { rnd.nextDouble() * 2 - 1 }

    private class Formant(val f: Curve, val q: Double, val gain: Double)

    /** Passes [src] through resonances that can move over time, like a mouth shaping a vowel. */
    private fun formants(src: DoubleArray, fs: List<Formant>, direct: Double = 0.0): DoubleArray {
        val filters = fs.map { Biquad() }
        val out = DoubleArray(src.size)
        for (i in src.indices) {
            if (i % 32 == 0) {
                val x = i.toDouble() / src.size
                fs.forEachIndexed { k, f -> filters[k].set(Filter.Band, f.f(x), f.q) }
            }
            var s = src[i] * direct
            for (k in fs.indices) s += filters[k].process(src[i]) * fs[k].gain
            out[i] = s
        }
        return out
    }

    /** A voiced sound: tone plus a little breath, three formants and an envelope. */
    private fun voice(
        sec: Double, pitch: Curve, f1: Curve, f2: Curve, f3: Curve, amp: Curve,
        vib: Double = 0.0, vibRate: Double = 5.0, jitter: Double = 0.0, breath: Double = 0.0, q: Double = 5.0,
    ): DoubleArray {
        val src = tone(sec, pitch, Wave.Saw, vib, vibRate, jitter)
        if (breath > 0) for (i in src.indices) src[i] = src[i] * (1 - breath) + (rnd.nextDouble() * 2 - 1) * breath
        return formants(src, listOf(Formant(f1, q, 1.0), Formant(f2, q, 0.7), Formant(f3, q, 0.35)), direct = 0.05)
            .shape(amp)
    }

    private enum class Filter { Low, High, Band }

    /** Standard two-pole filter (RBJ audio EQ cookbook). */
    private class Biquad {
        private var b0 = 0.0; private var b1 = 0.0; private var b2 = 0.0
        private var a1 = 0.0; private var a2 = 0.0
        private var z1 = 0.0; private var z2 = 0.0

        fun set(type: Filter, freq: Double, q: Double) {
            val w0 = TwoPi * freq.coerceIn(20.0, Rate * 0.45) / Rate
            val cw = cos(w0)
            val alpha = sin(w0) / (2 * q)
            val a0 = 1 + alpha
            when (type) {
                Filter.Low -> { b0 = (1 - cw) / 2; b1 = 1 - cw; b2 = (1 - cw) / 2 }
                Filter.High -> { b0 = (1 + cw) / 2; b1 = -(1 + cw); b2 = (1 + cw) / 2 }
                Filter.Band -> { b0 = alpha; b1 = 0.0; b2 = -alpha }
            }
            b0 /= a0; b1 /= a0; b2 /= a0
            a1 = -2 * cw / a0
            a2 = (1 - alpha) / a0
        }

        fun process(x: Double): Double {
            val y = b0 * x + z1
            z1 = b1 * x - a1 * y + z2
            z2 = b2 * x - a2 * y
            return y
        }
    }

    private fun DoubleArray.filtered(type: Filter, freq: Double, q: Double): DoubleArray {
        val f = Biquad().apply { set(type, freq, q) }
        for (i in indices) this[i] = f.process(this[i])
        return this
    }

    private fun DoubleArray.shape(env: Curve): DoubleArray {
        for (i in indices) this[i] *= env(i.toDouble() / size)
        return this
    }

    /** Fast loudness wobble: bleating, gobbling, growling. */
    private fun DoubleArray.tremolo(rate: Double, depth: Double): DoubleArray {
        for (i in indices) this[i] *= 1 - depth * (0.5 + 0.5 * sin(TwoPi * rate * i / Rate))
        return this
    }

    /** Soft clipping for grit; the input is normalised first so [k] means the same everywhere. */
    private fun DoubleArray.drive(k: Double): DoubleArray {
        val peak = maxOf(1e-9, maxOf { abs(it) })
        for (i in indices) this[i] = tanh(this[i] / peak * k) / tanh(k)
        return this
    }

    private fun scale(x: DoubleArray, g: Double) = x.also { for (i in it.indices) it[i] *= g }

    /** Places sounds on a timeline (start time in seconds to sound). */
    private fun seq(sec: Double, vararg parts: Pair<Double, DoubleArray>): DoubleArray {
        val end = parts.maxOf { (at, s) -> (at * Rate).toInt() + s.size }
        val out = DoubleArray(maxOf((sec * Rate).toInt(), end))
        for ((at, s) in parts) {
            val o = (at * Rate).toInt()
            for (i in s.indices) out[o + i] += s[i]
        }
        return out
    }

    /** Feedback echo, a little space around the sound (adds a tail). */
    private fun echo(x: DoubleArray, delay: Double, feedback: Double, wet: Double): DoubleArray {
        val d = (delay * Rate).toInt()
        val out = DoubleArray(x.size + d * 5)
        val line = DoubleArray(out.size)
        for (i in out.indices) {
            val dry = if (i < x.size) x[i] else 0.0
            val delayed = if (i >= d) line[i - d] else 0.0
            line[i] = dry + delayed * feedback
            out[i] = dry + delayed * wet
        }
        return out
    }

    /** Normalises to a comfortable level, removes clicks at the ends and converts to 16-bit. */
    private fun toPcm(input: DoubleArray): ShortArray {
        val peak = maxOf(1e-9, input.maxOf { abs(it) })
        // Drop the near-silent end of echo tails.
        val end = input.indexOfLast { abs(it) > peak * 0.01 } + 1
        val x = input.copyOf(maxOf(end, 1))
        val fade = (0.005 * Rate).toInt()
        return ShortArray(x.size) { i ->
            val edge = minOf(1.0, i.toDouble() / fade, (x.size - 1 - i).toDouble() / fade)
            (x[i] / peak * 0.85 * edge * Short.MAX_VALUE).toInt().toShort()
        }
    }
}
