package com.ranierimattos.afinador

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class MainActivity : Activity() {
    private lateinit var noteText: TextView
    private lateinit var freqText: TextView
    private lateinit var hintText: TextView
    private lateinit var tuner: TunerView
    private lateinit var strings: List<TextView>

    private val audio = AudioInput(::show)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        noteText = findViewById(R.id.note)
        freqText = findViewById(R.id.frequency)
        hintText = findViewById(R.id.hint)
        tuner = findViewById(R.id.tuner)

        val row = findViewById<LinearLayout>(R.id.strings)
        strings = Tuning.STRINGS.map { note ->
            TextView(this).apply {
                text = note.label
                textSize = 18f
                gravity = Gravity.CENTER
                row.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
        }

        findViewById<TextView>(R.id.credit).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.repo_url))))
        }
        show(null)
        // Pede só aqui: pedir no onResume entraria em loop quando o usuário nega.
        if (!hasMic()) requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }

    override fun onResume() {
        super.onResume()
        if (hasMic()) audio.start()
    }

    override fun onPause() {
        super.onPause()
        audio.stop()
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        if (results.firstOrNull() == PackageManager.PERMISSION_GRANTED) audio.start()
        show(null)
    }

    private fun hasMic() =
        checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun show(frequency: Double?) {
        val dim = getColor(R.color.dim)
        val r = frequency?.let(Tuning::read)
        noteText.text = r?.note?.label ?: "–"
        freqText.text = r?.let { getString(R.string.frequency_format, it.frequency, it.cents) } ?: ""
        tuner.cents = r?.cents
        strings.forEachIndexed { i, v -> v.setTextColor(if (i == r?.string) TunerView.GREEN else dim) }
        hintText.setText(
            when {
                r == null -> if (hasMic()) R.string.play_string else R.string.no_permission
                abs(r.cents) <= TunerView.IN_TUNE_CENTS -> R.string.in_tune
                r.cents < 0 -> R.string.tighten
                else -> R.string.loosen
            }
        )
    }
}
