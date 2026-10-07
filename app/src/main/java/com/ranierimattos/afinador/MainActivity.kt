package com.ranierimattos.afinador

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity() {
    private lateinit var noteText: TextView
    private lateinit var freqText: TextView
    private lateinit var hintText: TextView
    private lateinit var statsText: TextView
    private lateinit var tuner: TunerView
    private lateinit var bassSwitch: Switch
    private lateinit var presetSpinner: Spinner
    private lateinit var stringsRow: LinearLayout
    private var strings = emptyList<Note>()
    private var stringViews = emptyList<TextView>()

    // Contador: uma corda conta como afinada depois de ~0,5 s no tom (uma vez por corda).
    private var inTuneFrames = 0
    private var lastCounted = -1
    private var visits: Long? = null
    private var tuned = 0L

    private val audio = AudioInput(::show)
    private val prefs by lazy { getPreferences(MODE_PRIVATE) }
    private val presets get() = if (bassSwitch.isChecked) Tuning.BASS else Tuning.GUITAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        noteText = findViewById(R.id.note)
        freqText = findViewById(R.id.frequency)
        hintText = findViewById(R.id.hint)
        statsText = findViewById(R.id.stats)
        tuner = findViewById(R.id.tuner)
        bassSwitch = findViewById(R.id.bass)
        presetSpinner = findViewById(R.id.preset)
        stringsRow = findViewById(R.id.strings)

        bassSwitch.isChecked = prefs.getBoolean("bass", false)
        bassSwitch.setOnCheckedChangeListener { _, _ -> fillPresets(0) }
        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = setPreset(position)
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        fillPresets(prefs.getInt("preset", 0))

        findViewById<TextView>(R.id.credit).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.repo_url))))
        }
        if (savedInstanceState == null) Counter.count("visitas", hit = true) { visits = it; showStats() }
        Counter.count("afinacoes", hit = false) { tuned = it; showStats() }
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

    /** Preenche a lista de afinações do instrumento escolhido. */
    private fun fillPresets(selected: Int) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presets.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        presetSpinner.adapter = adapter
        presetSpinner.setSelection(selected.coerceIn(presets.indices))
        setPreset(presetSpinner.selectedItemPosition)
    }

    private fun setPreset(index: Int) {
        strings = presets[index].strings
        audio.minFreq = strings[0].frequency * 0.7
        lastCounted = -1
        inTuneFrames = 0
        prefs.edit().putBoolean("bass", bassSwitch.isChecked).putInt("preset", index).apply()

        stringsRow.removeAllViews()
        stringViews = strings.map { note ->
            TextView(this).apply {
                text = note.label
                textSize = 18f
                gravity = Gravity.CENTER
                stringsRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
        }
        show(null)
    }

    private fun showStats() {
        val format = NumberFormat.getInstance(Locale.forLanguageTag("pt-BR"))
        visits?.let { statsText.text = getString(R.string.stats_format, format.format(it), format.format(tuned)) }
    }

    private fun show(frequency: Double?) {
        val dim = getColor(R.color.dim)
        val r = frequency?.let { Tuning.read(it, strings) }
        noteText.text = r?.note?.label ?: "–"
        freqText.text = r?.let { getString(R.string.frequency_format, it.frequency, it.cents) } ?: ""
        tuner.cents = r?.cents
        stringViews.forEachIndexed { i, v -> v.setTextColor(if (i == r?.string) TunerView.GREEN else dim) }
        hintText.setText(
            when {
                r == null -> if (hasMic()) R.string.play_string else R.string.no_permission
                abs(r.cents) <= TunerView.IN_TUNE_CENTS -> R.string.in_tune
                r.cents < 0 -> R.string.tighten
                else -> R.string.loosen
            }
        )

        val inTune = r != null && abs(r.cents) <= TunerView.IN_TUNE_CENTS && r.note.label == strings[r.string].label
        inTuneFrames = if (inTune) inTuneFrames + 1 else 0
        if (inTuneFrames == 10 && r!!.string != lastCounted) {
            lastCounted = r.string
            Counter.count("afinacoes", hit = true) { tuned = it; showStats() }
        }
    }
}
