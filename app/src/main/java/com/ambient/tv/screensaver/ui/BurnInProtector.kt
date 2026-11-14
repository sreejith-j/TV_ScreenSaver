package com.ambient.tv.screensaver.ui

import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.random.Random

class BurnInProtector(private val targetView: View) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    // Shift interval: 120 seconds
    private val shiftIntervalMs = 120_000L

    // Safe bounds in DP
    private val maxShiftXdp = 38f
    private val maxShiftYdp = 28f

    private val shiftRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            performSubtleShift()
            handler.postDelayed(this, shiftIntervalMs)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        // First subtle shift after 30 seconds
        handler.postDelayed(shiftRunnable, 30_000L)
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(shiftRunnable)
        // Reset to center smoothly
        targetView.animate()
            .translationX(0f)
            .translationY(0f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun performSubtleShift() {
        val dm = targetView.resources.displayMetrics
        val maxPxX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxShiftXdp, dm)
        val maxPxY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxShiftYdp, dm)

        // Generate random target within [-max, +max]
        val targetX = (Random.nextFloat() * 2f - 1f) * maxPxX
        val targetY = (Random.nextFloat() * 2f - 1f) * maxPxY

        targetView.animate()
            .translationX(targetX)
            .translationY(targetY)
            .setDuration(2500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
}

