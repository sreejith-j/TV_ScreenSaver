package com.ambient.tv.screensaver

import android.service.dreams.DreamService
import com.ambient.tv.screensaver.ui.AmbientDisplayView

class AmbientDreamService : DreamService() {

    private var ambientView: AmbientDisplayView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Standard screensaver setup: non-interactive dismisses on any key press
        isInteractive = false
        isFullscreen = true
        isScreenBright = true

        ambientView = AmbientDisplayView(this)
        setContentView(ambientView)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        ambientView?.startAmbient()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        ambientView?.stopAmbient()
    }

    override fun onDetachedFromWindow() {
        ambientView?.destroy()
        ambientView = null
        super.onDetachedFromWindow()
    }
}

