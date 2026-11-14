package com.ambient.tv.screensaver.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class AmbientAuraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var auraColor: Int = Color.parseColor("#8AB4F8")
    private var isAuraEnabled: Boolean = true

    private var currentAlpha = 0.12f
    private var currentScale = 1.0f

    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null

    init {
        startBreathing()
    }

    fun setAuraColor(color: Int) {
        this.auraColor = color
        invalidate()
    }

    fun setAuraEnabled(enabled: Boolean) {
        this.isAuraEnabled = enabled
        visibility = if (enabled) VISIBLE else GONE
        if (enabled) {
            startBreathing()
        } else {
            animator?.cancel()
        }
        invalidate()
    }

    private fun startBreathing() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 8000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                val fraction = va.animatedFraction
                currentAlpha = 0.08f + (fraction * 0.08f) // 8% to 16% opacity
                currentScale = 0.92f + (fraction * 0.12f)
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAuraEnabled) return

        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = (Math.min(width, height) * 0.55f) * currentScale
        if (baseRadius <= 0) return

        val r = Color.red(auraColor)
        val g = Color.green(auraColor)
        val b = Color.blue(auraColor)
        val alphaInt = (currentAlpha * 255).toInt().coerceIn(0, 255)

        val centerColor = Color.argb(alphaInt, r, g, b)
        val midColor = Color.argb((alphaInt * 0.4f).toInt(), r, g, b)
        val edgeColor = Color.argb(0, r, g, b)

        val shader = RadialGradient(
            cx, cy, baseRadius,
            intArrayOf(centerColor, midColor, edgeColor),
            floatArrayOf(0.0f, 0.55f, 1.0f),
            Shader.TileMode.CLAMP
        )

        auraPaint.shader = shader
        canvas.drawCircle(cx, cy, baseRadius, auraPaint)
    }
}
