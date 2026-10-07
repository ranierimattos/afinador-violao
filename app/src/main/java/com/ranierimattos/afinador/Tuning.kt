package com.ranierimattos.afinador

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class Note(val name: String, val octave: Int, val frequency: Double) {
    val label get() = "$name$octave"
}

data class Reading(val frequency: Double, val note: Note, val cents: Double, val string: Int)

object Tuning {
    private const val A4 = 440.0
    private val NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    /** Cordas soltas do violão, da 6ª (mais grave) para a 1ª. */
    val STRINGS: List<Note> = listOf(40, 45, 50, 55, 59, 64).map(::noteFromMidi)

    fun noteFromMidi(midi: Int) = Note(
        name = NAMES[midi % 12],
        octave = midi / 12 - 1,
        frequency = A4 * 2.0.pow((midi - 69) / 12.0),
    )

    fun read(frequency: Double): Reading {
        val midi = (69 + 12 * log2(frequency / A4)).roundToInt()
        val note = noteFromMidi(midi)
        val cents = 1200 * log2(frequency / note.frequency)
        val string = STRINGS.indices.minBy { abs(log2(frequency / STRINGS[it].frequency)) }
        return Reading(frequency, note, cents, string)
    }
}
