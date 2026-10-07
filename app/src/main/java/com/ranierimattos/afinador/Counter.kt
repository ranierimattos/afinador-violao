package com.ranierimattos.afinador

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL

/**
 * Contador global e anônimo (visitas e cordas afinadas), no serviço gratuito Abacus.
 * Se não houver internet, simplesmente não chama [onValue].
 */
object Counter {
    private const val BASE = "https://abacus.jasoncameron.dev"
    private val main = Handler(Looper.getMainLooper())

    fun count(key: String, hit: Boolean, onValue: (Long) -> Unit) = Thread {
        val body = runCatching {
            val conn = URL("$BASE/${if (hit) "hit" else "get"}/afinador-violao/$key").openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull()
        val value = body?.let { Regex("\"value\":(\\d+)").find(it)?.groupValues?.get(1)?.toLongOrNull() }
        if (value != null) main.post { onValue(value) }
    }.start()
}
