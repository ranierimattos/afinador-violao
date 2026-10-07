package com.ranierimattos.afinador

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningTest {
    @Test
    fun standardStrings() {
        assertEquals(listOf("E2", "A2", "D3", "G3", "B3", "E4"), Tuning.STRINGS.map { it.label })
        assertEquals(82.41, Tuning.STRINGS[0].frequency, 0.01)
        assertEquals(329.63, Tuning.STRINGS[5].frequency, 0.01)
    }

    @Test
    fun readsNoteAndCents() {
        val r = Tuning.read(440.0)
        assertEquals("A4", r.note.label)
        assertEquals(0.0, r.cents, 1e-9)

        val flat = Tuning.read(110.0 * Math.pow(2.0, -20 / 1200.0))
        assertEquals("A2", flat.note.label)
        assertEquals(-20.0, flat.cents, 1e-6)
        assertEquals(1, flat.string)
    }

    @Test
    fun nearestString() {
        assertEquals(0, Tuning.read(80.0).string)
        assertEquals(4, Tuning.read(250.0).string)
        assertEquals(5, Tuning.read(340.0).string)
    }
}
