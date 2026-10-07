package com.ranierimattos.afinador

import kotlin.math.sqrt

/**
 * Detecção de frequência fundamental pelo algoritmo YIN.
 * Kotlin puro (sem Android) para poder ser testado na JVM.
 */
class PitchDetector(
    private val sampleRate: Int,
    private val minFreq: Double = 60.0,
    private val maxFreq: Double = 1000.0,
    private val threshold: Double = 0.15,
    private val minRms: Double = 0.01,
) {
    /** Devolve a frequência em Hz, ou null em silêncio / sem tom claro. */
    fun detect(samples: FloatArray): Double? {
        val minLag = (sampleRate / maxFreq).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / minFreq).toInt()
        val n = samples.size - maxLag
        if (n <= 0) return null

        var sumSq = 0.0
        for (s in samples) sumSq += s * s
        if (sqrt(sumSq / samples.size) < minRms) return null

        // Função diferença d(tau)
        val diff = DoubleArray(maxLag + 1)
        for (tau in 1..maxLag) {
            var sum = 0.0
            for (i in 0 until n) {
                val d = samples[i] - samples[i + tau]
                sum += d * d
            }
            diff[tau] = sum
        }

        // Média cumulativa normalizada d'(tau)
        val cmnd = DoubleArray(maxLag + 1)
        cmnd[0] = 1.0
        var running = 0.0
        for (tau in 1..maxLag) {
            running += diff[tau]
            cmnd[tau] = if (running == 0.0) 1.0 else diff[tau] * tau / running
        }

        // Primeiro mínimo abaixo do limiar
        var tau = minLag
        while (tau < maxLag) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 < maxLag && cmnd[tau + 1] < cmnd[tau]) tau++
                break
            }
            tau++
        }
        if (tau >= maxLag) return null

        // Interpolação parabólica para precisão abaixo de uma amostra
        val better = if (tau > 1 && tau < maxLag) {
            val s0 = cmnd[tau - 1]
            val s1 = cmnd[tau]
            val s2 = cmnd[tau + 1]
            val denom = 2 * (2 * s1 - s2 - s0)
            if (denom != 0.0) tau + (s2 - s0) / denom else tau.toDouble()
        } else {
            tau.toDouble()
        }
        return sampleRate / better
    }
}
