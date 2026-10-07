package com.ranierimattos.afinador

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class PitchDetectorTest {
    private val rate = 44100
    private val detector = PitchDetector(rate)

    /** Corda dedilhada simplificada: fundamental forte + harmônicos que decaem. */
    private fun pluck(freq: Double, size: Int = 4096): FloatArray = FloatArray(size) { i ->
        val t = i.toDouble() / rate
        val harmonics = listOf(1.0 to 1.0, 2.0 to 0.6, 3.0 to 0.4, 4.0 to 0.2, 5.0 to 0.1)
        (harmonics.sumOf { (h, a) -> a * sin(2 * PI * freq * h * t) } * 0.3 * exp(-t * 2)).toFloat()
    }

    @Test
    fun detectsOpenStrings() {
        for (note in Tuning.STRINGS) {
            val f = detector.detect(pluck(note.frequency))
            assertNotNull("sem leitura para ${note.label}", f)
            assertEquals("frequência de ${note.label}", note.frequency, f!!, 0.5)
        }
    }

    @Test
    fun detectsPureSineOutOfTune() {
        val freq = 112.0 // A2 desafinado
        val samples = FloatArray(4096) { (0.5 * sin(2 * PI * freq * it / rate)).toFloat() }
        assertEquals(freq, detector.detect(samples)!!, 0.5)
    }

    @Test
    fun silenceGivesNull() {
        assertNull(detector.detect(FloatArray(4096)))
    }

    @Test
    fun noiseGivesNull() {
        val rnd = java.util.Random(1)
        val noise = FloatArray(4096) { (rnd.nextGaussian() * 0.3).toFloat() }
        assertNull(detector.detect(noise))
    }
}
