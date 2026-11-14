package com.ambient.tv.screensaver.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ambient.tv.screensaver.data.ParticleMode
import java.util.Calendar
import kotlin.math.sin
import kotlin.random.Random

class WeatherParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentMode = ParticleMode.AUTO
    private var weatherCode: Int = 0
    private var isNight: Boolean = false

    private val maxParticles = 60
    private val particles = Array(maxParticles) { Particle() }
    private var isRunning = false

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // Flash for distant lightning during thunderstorms
    private var lightningAlpha = 0f

    private val frameCallback = object : android.view.Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            updateParticles()
            invalidate()
            android.view.Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private enum class ActiveEffect {
        RAIN,
        THUNDER,
        SNOW,
        STARS,
        SUN_MOTES,
        NONE
    }

    private var activeEffect = ActiveEffect.NONE

    class Particle {
        var x = 0f
        var y = 0f
        var vx = 0f
        var vy = 0f
        var length = 0f
        var radius = 0f
        var alpha = 0f
        var maxAlpha = 0.5f
        var phase = 0f
        var phaseSpeed = 0.02f
    }

    fun setParticleMode(mode: ParticleMode) {
        this.currentMode = mode
        resolveActiveEffect()
        initParticles()
        invalidate()
    }

    fun setWeather(code: Int) {
        this.weatherCode = code
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        this.isNight = hour >= 19 || hour < 6
        resolveActiveEffect()
        initParticles()
        invalidate()
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initParticles()
    }

    private fun resolveActiveEffect() {
        activeEffect = when (currentMode) {
            ParticleMode.OFF -> ActiveEffect.NONE
            ParticleMode.RAIN -> ActiveEffect.RAIN
            ParticleMode.SNOW -> ActiveEffect.SNOW
            ParticleMode.STARS -> ActiveEffect.STARS
            ParticleMode.SUN_MOTES -> ActiveEffect.SUN_MOTES
            ParticleMode.AUTO -> {
                when (weatherCode) {
                    95, 96, 99 -> ActiveEffect.THUNDER
                    51, 53, 55, 61, 63, 65, 80, 81, 82 -> ActiveEffect.RAIN
                    71, 73, 75, 77, 85, 86 -> ActiveEffect.SNOW
                    0, 1, 2 -> if (isNight) ActiveEffect.STARS else ActiveEffect.SUN_MOTES
                    else -> if (isNight) ActiveEffect.STARS else ActiveEffect.SUN_MOTES
                }
            }
        }
    }

    private fun initParticles() {
        val w = width.toFloat().coerceAtLeast(100f)
        val h = height.toFloat().coerceAtLeast(100f)

        for (p in particles) {
            p.x = Random.nextFloat() * w
            p.y = Random.nextFloat() * h
            p.phase = Random.nextFloat() * 6.28f
            p.phaseSpeed = 0.015f + Random.nextFloat() * 0.03f

            when (activeEffect) {
                ActiveEffect.RAIN, ActiveEffect.THUNDER -> {
                    p.vx = -1.5f - Random.nextFloat() * 2f
                    p.vy = 14f + Random.nextFloat() * 16f
                    p.length = 20f + Random.nextFloat() * 30f
                    p.maxAlpha = 0.15f + Random.nextFloat() * 0.25f
                    p.alpha = p.maxAlpha
                }
                ActiveEffect.SNOW -> {
                    p.vx = (Random.nextFloat() * 2f - 1f) * 0.6f
                    p.vy = 1.2f + Random.nextFloat() * 2.2f
                    p.radius = 2.5f + Random.nextFloat() * 4.5f
                    p.maxAlpha = 0.25f + Random.nextFloat() * 0.40f
                    p.alpha = p.maxAlpha
                }
                ActiveEffect.STARS -> {
                    p.radius = 1.2f + Random.nextFloat() * 2.5f
                    p.maxAlpha = 0.2f + Random.nextFloat() * 0.6f
                    p.alpha = p.maxAlpha * 0.5f
                }
                ActiveEffect.SUN_MOTES -> {
                    p.vx = (Random.nextFloat() * 2f - 1f) * 0.5f
                    p.vy = -0.6f - Random.nextFloat() * 1.2f
                    p.radius = 2.0f + Random.nextFloat() * 3.5f
                    p.maxAlpha = 0.12f + Random.nextFloat() * 0.25f
                    p.alpha = p.maxAlpha
                }
                ActiveEffect.NONE -> {}
            }
        }
    }

    private fun updateParticles() {
        if (activeEffect == ActiveEffect.NONE) return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Distant lightning flash decay
        if (lightningAlpha > 0f) {
            lightningAlpha = (lightningAlpha - 0.02f).coerceAtLeast(0f)
        } else if (activeEffect == ActiveEffect.THUNDER && Random.nextInt(450) == 0) {
            // Very occasional subtle sheet flash
            lightningAlpha = 0.09f
        }

        for (p in particles) {
            when (activeEffect) {
                ActiveEffect.RAIN, ActiveEffect.THUNDER -> {
                    p.x += p.vx
                    p.y += p.vy
                    if (p.y > h || p.x < 0) {
                        p.y = -p.length
                        p.x = Random.nextFloat() * (w + 100f)
                    }
                }
                ActiveEffect.SNOW -> {
                    p.phase += p.phaseSpeed
                    p.x += sin(p.phase) * 1.0f + p.vx
                    p.y += p.vy
                    if (p.y > h) {
                        p.y = -p.radius * 2f
                        p.x = Random.nextFloat() * w
                    }
                }
                ActiveEffect.STARS -> {
                    p.phase += p.phaseSpeed
                    // Subtle twinkling brightness oscillation
                    val sinVal = (sin(p.phase) + 1f) * 0.5f
                    p.alpha = 0.1f + sinVal * (p.maxAlpha - 0.1f)
                }
                ActiveEffect.SUN_MOTES -> {
                    p.phase += p.phaseSpeed
                    p.x += sin(p.phase) * 0.6f + p.vx
                    p.y += p.vy
                    if (p.y < -10f) {
                        p.y = h + 10f
                        p.x = Random.nextFloat() * w
                    }
                }
                ActiveEffect.NONE -> {}
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (activeEffect == ActiveEffect.NONE) return

        // Draw lightning sheet if active
        if (lightningAlpha > 0f) {
            val flashColor = Color.argb((lightningAlpha * 255).toInt(), 200, 220, 255)
            canvas.drawColor(flashColor)
        }

        when (activeEffect) {
            ActiveEffect.RAIN, ActiveEffect.THUNDER -> {
                rainPaint.strokeWidth = 2f
                for (p in particles) {
                    val alphaInt = (p.alpha * 255).toInt().coerceIn(0, 255)
                    rainPaint.color = Color.argb(alphaInt, 180, 215, 255)
                    canvas.drawLine(p.x, p.y, p.x + p.vx * 2f, p.y + p.length, rainPaint)
                }
            }
            ActiveEffect.SNOW -> {
                for (p in particles) {
                    val alphaInt = (p.alpha * 255).toInt().coerceIn(0, 255)
                    particlePaint.color = Color.argb(alphaInt, 240, 245, 255)
                    canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
                }
            }
            ActiveEffect.STARS -> {
                for (p in particles) {
                    val alphaInt = (p.alpha * 255).toInt().coerceIn(0, 255)
                    particlePaint.color = Color.argb(alphaInt, 230, 240, 255)
                    canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
                }
            }
            ActiveEffect.SUN_MOTES -> {
                for (p in particles) {
                    val alphaInt = (p.alpha * 255).toInt().coerceIn(0, 255)
                    particlePaint.color = Color.argb(alphaInt, 255, 225, 150)
                    canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
                }
            }
            ActiveEffect.NONE -> {}
        }
    }
}
