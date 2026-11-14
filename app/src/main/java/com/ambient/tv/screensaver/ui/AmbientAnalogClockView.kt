package com.ambient.tv.screensaver.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AmbientAnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var accentColor: Int = Color.parseColor("#8AB4F8")

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#33FFFFFF")
    }

    private val tickMajorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E0E0E0")
    }

    private val tickMinorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#55FFFFFF")
    }

    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        color = accentColor
    }

    private val centerPinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = accentColor
    }

    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    fun setAccentColor(color: Int) {
        this.accentColor = color
        minuteHandPaint.color = color
        centerPinPaint.color = color
        invalidate()
    }

    fun updateTime() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 16f
        if (radius <= 0) return

        // Draw dial outer ring
        canvas.drawCircle(cx, cy, radius, dialPaint)

        // Draw 12 ticks
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val isMajor = (i % 3 == 0) // 12, 3, 6, 9
            val tickLen = if (isMajor) radius * 0.14f else radius * 0.08f
            val startR = radius - 8f
            val endR = startR - tickLen

            val startX = (cx + startR * cos(angle)).toFloat()
            val startY = (cy + startR * sin(angle)).toFloat()
            val endX = (cx + endR * cos(angle)).toFloat()
            val endY = (cy + endR * sin(angle)).toFloat()

            canvas.drawLine(startX, startY, endX, endY, if (isMajor) tickMajorPaint else tickMinorPaint)
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Hour hand angle (smooth transition based on minutes)
        val hourAngle = Math.toRadians(((hour + minute / 60.0) * 30.0) - 90.0)
        val hourLen = radius * 0.52f
        val hourX = (cx + hourLen * cos(hourAngle)).toFloat()
        val hourY = (cy + hourLen * sin(hourAngle)).toFloat()
        canvas.drawLine(cx, cy, hourX, hourY, hourHandPaint)

        // Minute hand angle
        val minAngle = Math.toRadians(((minute + second / 60.0) * 6.0) - 90.0)
        val minLen = radius * 0.75f
        val minX = (cx + minLen * cos(minAngle)).toFloat()
        val minY = (cy + minLen * sin(minAngle)).toFloat()
        canvas.drawLine(cx, cy, minX, minY, minuteHandPaint)

        // Center hub
        canvas.drawCircle(cx, cy, 10f, centerPinPaint)
        canvas.drawCircle(cx, cy, 4f, centerDotPaint)
    }
}
