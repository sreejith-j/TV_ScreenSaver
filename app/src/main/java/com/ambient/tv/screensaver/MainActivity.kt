package com.ambient.tv.screensaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ambient.tv.screensaver.data.AccentTheme
import com.ambient.tv.screensaver.data.DisplayMode
import com.ambient.tv.screensaver.data.ParticleMode
import com.ambient.tv.screensaver.data.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager

    private lateinit var cardPreview: View
    private lateinit var cardDreamSettings: View
    private lateinit var cardDisplayMode: View
    private lateinit var cardAccentTheme: View
    private lateinit var cardAuraGlow: View
    private lateinit var cardParticles: View
    private lateinit var cardWeatherDetails: View
    private lateinit var cardNightDimmer: View
    private lateinit var cardTimeFormat: View
    private lateinit var cardTempUnit: View
    private lateinit var cardBurnIn: View

    private lateinit var tvDreamSettingsValue: TextView
    private lateinit var tvDisplayModeValue: TextView
    private lateinit var tvAccentThemeValue: TextView
    private lateinit var tvAuraGlowValue: TextView
    private lateinit var tvParticlesValue: TextView
    private lateinit var tvWeatherDetailsValue: TextView
    private lateinit var tvNightDimmerValue: TextView
    private lateinit var tvTimeFormatValue: TextView
    private lateinit var tvTempUnitValue: TextView
    private lateinit var tvBurnInValue: TextView

    companion object {
        private const val DREAM_COMPONENT = "com.ambient.tv.screensaver/.AmbientDreamService"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferencesManager(this)
        initViews()
        updateCardSummaries()
        setupListeners()

        cardPreview.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        updateCardSummaries()
    }

    private fun initViews() {
        cardPreview = findViewById(R.id.cardPreview)
        cardDreamSettings = findViewById(R.id.cardDreamSettings)
        cardDisplayMode = findViewById(R.id.cardDisplayMode)
        cardAccentTheme = findViewById(R.id.cardAccentTheme)
        cardAuraGlow = findViewById(R.id.cardAuraGlow)
        cardParticles = findViewById(R.id.cardParticles)
        cardWeatherDetails = findViewById(R.id.cardWeatherDetails)
        cardNightDimmer = findViewById(R.id.cardNightDimmer)
        cardTimeFormat = findViewById(R.id.cardTimeFormat)
        cardTempUnit = findViewById(R.id.cardTempUnit)
        cardBurnIn = findViewById(R.id.cardBurnIn)

        tvDreamSettingsValue = findViewById(R.id.tvDreamSettingsValue)
        tvDisplayModeValue = findViewById(R.id.tvDisplayModeValue)
        tvAccentThemeValue = findViewById(R.id.tvAccentThemeValue)
        tvAuraGlowValue = findViewById(R.id.tvAuraGlowValue)
        tvParticlesValue = findViewById(R.id.tvParticlesValue)
        tvWeatherDetailsValue = findViewById(R.id.tvWeatherDetailsValue)
        tvNightDimmerValue = findViewById(R.id.tvNightDimmerValue)
        tvTimeFormatValue = findViewById(R.id.tvTimeFormatValue)
        tvTempUnitValue = findViewById(R.id.tvTempUnitValue)
        tvBurnInValue = findViewById(R.id.tvBurnInValue)
    }

    private fun isCurrentlyDefaultDream(): Boolean {
        return try {
            val current = Settings.Secure.getString(contentResolver, "screensaver_components")
            current?.contains("com.ambient.tv.screensaver") == true
        } catch (e: Exception) {
            false
        }
    }

    private fun updateCardSummaries() {
        // Screensaver active status
        if (isCurrentlyDefaultDream()) {
            tvDreamSettingsValue.text = "Active Screensaver (Runs automatically when TV is idle)"
            tvDreamSettingsValue.setTextColor(resources.getColor(R.color.text_accent))
        } else {
            tvDreamSettingsValue.text = "Click to activate as default TV screensaver"
            tvDreamSettingsValue.setTextColor(resources.getColor(R.color.text_secondary))
        }

        // Display mode
        val mode = prefs.displayMode
        tvDisplayModeValue.text = "${mode.displayName} — ${mode.subtitle}"

        // Accent Theme
        val theme = prefs.accentTheme
        tvAccentThemeValue.text = if (theme == AccentTheme.CIRCADIAN) {
            "Dynamic Circadian (Auto-adjusts with time of day)"
        } else {
            theme.displayName
        }

        // Aura Glow
        tvAuraGlowValue.text = if (prefs.isAuraEnabled) {
            "Breathing Ambient Aura (Soft glow matching theme)"
        } else {
            "Pure OLED Black (Deep zero-light background)"
        }

        // Particle Effects
        val p = prefs.particleMode
        tvParticlesValue.text = "${p.displayName} — ${p.subtitle}"

        // Weather Details
        tvWeatherDetailsValue.text = if (prefs.isDetailedWeather) {
            "Detailed (Feels Like • High / Low • Humidity)"
        } else {
            "Compact (Current temperature & condition only)"
        }

        // Night Dimmer
        tvNightDimmerValue.text = if (prefs.isNightDimmerEnabled) {
            "Auto-dim at night (10 PM – 7 AM)"
        } else {
            "Always Full Brightness"
        }

        // Time format
        tvTimeFormatValue.text = if (prefs.is12HourFormat) {
            "12-Hour format (e.g. 02:45 PM)"
        } else {
            "24-Hour format (e.g. 14:45)"
        }

        // Temp unit
        tvTempUnitValue.text = if (prefs.isCelsius) {
            "Celsius (°C)"
        } else {
            "Fahrenheit (°F)"
        }

        // Burn In
        tvBurnInValue.text = if (prefs.isBurnInProtectionEnabled) {
            "Smooth pixel shift every 2 mins (Anti-Burn-In)"
        } else {
            "Fixed center position"
        }
    }

    private fun setupListeners() {
        cardPreview.setOnClickListener {
            val intent = Intent(this, PreviewActivity::class.java)
            startActivity(intent)
        }

        cardDreamSettings.setOnClickListener {
            try {
                Settings.Secure.putString(contentResolver, "screensaver_components", DREAM_COMPONENT)
                Settings.Secure.putInt(contentResolver, "screensaver_enabled", 1)
                Toast.makeText(this, "Ambient TV is active as your TV screensaver!", Toast.LENGTH_LONG).show()
                updateCardSummaries()
            } catch (e: SecurityException) {
                // If permission not yet granted
                Toast.makeText(this, "Permission required: Grant WRITE_SECURE_SETTINGS via ADB", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Could not set screensaver: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        cardDisplayMode.setOnClickListener {
            prefs.displayMode = DisplayMode.next(prefs.displayMode)
            updateCardSummaries()
        }

        cardAccentTheme.setOnClickListener {
            prefs.accentTheme = AccentTheme.next(prefs.accentTheme)
            updateCardSummaries()
        }

        cardAuraGlow.setOnClickListener {
            prefs.isAuraEnabled = !prefs.isAuraEnabled
            updateCardSummaries()
        }

        cardParticles.setOnClickListener {
            prefs.particleMode = ParticleMode.next(prefs.particleMode)
            updateCardSummaries()
        }

        cardWeatherDetails.setOnClickListener {
            prefs.isDetailedWeather = !prefs.isDetailedWeather
            updateCardSummaries()
        }

        cardNightDimmer.setOnClickListener {
            prefs.isNightDimmerEnabled = !prefs.isNightDimmerEnabled
            updateCardSummaries()
        }

        cardTimeFormat.setOnClickListener {
            prefs.is12HourFormat = !prefs.is12HourFormat
            updateCardSummaries()
        }

        cardTempUnit.setOnClickListener {
            prefs.isCelsius = !prefs.isCelsius
            updateCardSummaries()
        }

        cardBurnIn.setOnClickListener {
            prefs.isBurnInProtectionEnabled = !prefs.isBurnInProtectionEnabled
            updateCardSummaries()
        }
    }
}
