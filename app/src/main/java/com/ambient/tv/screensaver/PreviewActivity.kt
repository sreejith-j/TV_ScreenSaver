package com.ambient.tv.screensaver

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.ambient.tv.screensaver.ui.AmbientDisplayView

class PreviewActivity : AppCompatActivity() {

    private lateinit var ambientView: AmbientDisplayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        ambientView = findViewById(R.id.ambientDisplayView)
    }

    override fun onStart() {
        super.onStart()
        ambientView.startAmbient()
    }

    override fun onStop() {
        ambientView.stopAmbient()
        super.onStop()
    }

    override fun onDestroy() {
        ambientView.destroy()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        finish()
        return true
    }
}

