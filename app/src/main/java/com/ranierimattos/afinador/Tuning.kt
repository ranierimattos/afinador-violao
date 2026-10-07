package com.ranierimattos.afinador

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class Note(val name: String, val octave: Int, val frequency: Double) {
    val label get() = "$name$octave"
}

data class Reading(val frequency: Double, val note: Note, val cents: Double, val string: Int)

/** Uma afinação: cordas soltas (em notas MIDI) da mais grave para a mais aguda. */
class Preset(val name: String, vararg midi: Int) {
    val strings = midi.map(Tuning::noteFromMidi)
}

object Tuning {
    private const val A4 = 440.0
    private val NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    val GUITAR = listOf(
        Preset("Padrão", 40, 45, 50, 55, 59, 64), // E A D G B E
        Preset("Drop D", 38, 45, 50, 55, 59, 64), // D A D G B E
        Preset("Meio tom abaixo", 39, 44, 49, 54, 58, 63),
        Preset("Open G", 38, 43, 50, 55, 59, 62), // D G D G B D
        Preset("DADGAD", 38, 45, 50, 55, 57, 62),
    )

    val BASS = listOf(
        Preset("Padrão", 28, 33, 38, 43), // E A D G
        Preset("Drop D", 26, 33, 38, 43), // D A D G
        Preset("Meio tom abaixo", 27, 32, 37, 42),
    )

    fun noteFromMidi(midi: Int) = Note(
        name = NAMES[midi % 12],
        octave = midi / 12 - 1,
        frequency = A4 * 2.0.pow((midi - 69) / 12.0),
    )

    fun read(frequency: Double, strings: List<Note>): Reading {
        val midi = (69 + 12 * log2(frequency / A4)).roundToInt()
        val note = noteFromMidi(midi)
        val cents = 1200 * log2(frequency / note.frequency)
        val string = strings.indices.minBy { abs(log2(frequency / strings[it].frequency)) }
        return Reading(frequency, note, cents, string)
    }
}
