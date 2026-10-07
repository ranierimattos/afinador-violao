package com.ranierimattos.afinador

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper

/** Lê o microfone numa thread e entrega a frequência (ou null) na thread principal. */
class AudioInput(private val onPitch: (Double?) -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    @Volatile private var running = false
    private var thread: Thread? = null

    @SuppressLint("MissingPermission") // só é chamado com a permissão concedida
    fun start() {
        if (running) return
        running = true
        thread = Thread(::loop).apply { start() }
    }

    fun stop() {
        running = false
        thread?.join(500)
        thread = null
    }

    @SuppressLint("MissingPermission")
    private fun loop() {
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC, RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, WINDOW * 2
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            running = false
            return
        }
        val detector = PitchDetector(RATE)
        val window = FloatArray(WINDOW)
        val chunk = ShortArray(HOP)
        val recent = ArrayDeque<Double>()
        record.startRecording()
        while (running) {
            if (record.read(chunk, 0, HOP) < HOP) continue
            // Janela deslizante: descarta a metade antiga e acrescenta o trecho novo.
            System.arraycopy(window, HOP, window, 0, WINDOW - HOP)
            for (i in 0 until HOP) window[WINDOW - HOP + i] = chunk[i] / 32768f

            val pitch = detector.detect(window)
            if (pitch == null) recent.clear() else recent.addLast(pitch)
            if (recent.size > 5) recent.removeFirst()
            // Mediana das últimas leituras: o ponteiro não treme.
            val smoothed = recent.sorted().getOrNull(recent.size / 2)
            main.post { onPitch(smoothed) }
        }
        record.stop()
        record.release()
    }

    private companion object {
        const val RATE = 44100
        const val WINDOW = 4096
        const val HOP = WINDOW / 2
    }
}
