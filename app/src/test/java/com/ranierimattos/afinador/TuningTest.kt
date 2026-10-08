package com.ranierimattos.afinador

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningTest {
    private val guitar = Tuning.GUITAR[0].strings
    private val bass = Tuning.BASS[0].strings

    @Test
    fun presets() {
        assertEquals(listOf("E2", "A2", "D3", "G3", "B3", "E4"), guitar.map { it.label })
        assertEquals(listOf("D2", "A2", "D3", "G3", "B3", "E4"), Tuning.GUITAR[1].strings.map { it.label })
        assertEquals(listOf("E1", "A1", "D2", "G2"), bass.map { it.label })
        assertEquals(82.41, guitar[0].frequency, 0.01)
        assertEquals(41.20, bass[0].frequency, 0.01)
    }

    @Test
    fun readsNoteAndCents() {
        val r = Tuning.read(440.0, guitar)
        assertEquals("A4", r.note.label)
        assertEquals(0.0, r.cents, 1e-9)

        val flat = Tuning.read(110.0 * Math.pow(2.0, -20 / 1200.0), guitar)
        assertEquals("A2", flat.note.label)
        assertEquals(-20.0, flat.cents, 1e-6)
        assertEquals(1, flat.string)
    }

    @Test
    fun nearestString() {
        assertEquals(0, Tuning.read(80.0, guitar).string)
        assertEquals(5, Tuning.read(340.0, guitar).string)
        assertEquals(0, Tuning.read(73.0, Tuning.GUITAR[1].strings).string) // Drop D
        assertEquals(3, Tuning.read(98.0, bass).string)
    }
}
