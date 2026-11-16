package com.cojocari.alphabet.audio

import com.cojocari.alphabet.data.Animals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile

/**
 * Renders every animal sound, checks it is audible and of sane length, and writes it to
 * app/build/animal-sounds/<key>.wav so the sounds can be listened to on a computer.
 */
class AnimalSynthTest {
    @Test
    fun everyAnimalHasASound() {
        val dir = File("build/animal-sounds").apply { mkdirs() }
        for (a in Animals) {
            val pcm = AnimalSynth.render(a.key)
            val seconds = pcm.size.toDouble() / AnimalSynth.Rate
            assertTrue("${a.key} length $seconds s", seconds in 0.2..5.0)
            val peak = pcm.maxOf { kotlin.math.abs(it.toInt()) }
            assertTrue("${a.key} is silent", peak > 10_000)
            writeWav(File(dir, "${a.key}.wav"), pcm)
        }
    }

    private fun writeWav(file: File, pcm: ShortArray) {
        RandomAccessFile(file, "rw").use { f ->
            f.setLength(0)
            val data = pcm.size * 2
            fun int(v: Int) = f.write(byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte()))
            fun short(v: Int) = f.write(byteArrayOf(v.toByte(), (v shr 8).toByte()))
            f.writeBytes("RIFF"); int(36 + data); f.writeBytes("WAVE")
            f.writeBytes("fmt "); int(16); short(1); short(1); int(AnimalSynth.Rate); int(AnimalSynth.Rate * 2); short(2); short(16)
            f.writeBytes("data"); int(data)
            val bytes = ByteArray(data)
            pcm.forEachIndexed { i, s -> bytes[2 * i] = s.toInt().toByte(); bytes[2 * i + 1] = (s.toInt() shr 8).toByte() }
            f.write(bytes)
        }
    }
}
