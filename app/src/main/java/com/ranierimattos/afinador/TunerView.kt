package com.ranierimattos.afinador

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Escala de -50 a +50 cents com um ponteiro. */
class TunerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    /** Desvio em cents, ou null quando não há som. */
    var cents: Double? = null
        set(value) {
            field = value
            invalidate()
        }

    private val density = resources.displayMetrics.density
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 2 * density
    }
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 4 * density
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 14 * density
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height * 0.9f
        val radius = min(width / 2f, height * 0.8f) * 0.9f
        val maxAngle = Math.toRadians(60.0)

        for (c in -50..50 step 10) {
            val a = c / 50.0 * maxAngle
            val inner = if (c == 0) radius * 0.8f else radius * 0.88f
            tickPaint.color = if (c == 0) GREEN else Color.GRAY
            canvas.drawLine(
                cx + (inner * sin(a)).toFloat(), cy - (inner * cos(a)).toFloat(),
                cx + (radius * sin(a)).toFloat(), cy - (radius * cos(a)).toFloat(),
                tickPaint
            )
        }
        canvas.drawText("-50", cx - radius * 0.87f, cy - radius * 0.38f, textPaint)
        canvas.drawText("+50", cx + radius * 0.87f, cy - radius * 0.38f, textPaint)

        val c = cents ?: return
        val a = c.coerceIn(-50.0, 50.0) / 50.0 * maxAngle
        needlePaint.color = when {
            abs(c) <= IN_TUNE_CENTS -> GREEN
            abs(c) <= 15 -> Color.rgb(0xFF, 0xC1, 0x07)
            else -> Color.rgb(0xF4, 0x43, 0x36)
        }
        canvas.drawLine(
            cx, cy,
            cx + (radius * 0.95f * sin(a)).toFloat(), cy - (radius * 0.95f * cos(a)).toFloat(),
            needlePaint
        )
        canvas.drawCircle(cx, cy, 6 * density, needlePaint)
    }

    companion object {
        const val IN_TUNE_CENTS = 5.0
        val GREEN = Color.rgb(0x4C, 0xAF, 0x50)
    }
}
