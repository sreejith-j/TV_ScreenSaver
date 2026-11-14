package com.ambient.tv.screensaver.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ambient.tv.screensaver.R
import com.ambient.tv.screensaver.data.DisplayMode
import com.ambient.tv.screensaver.data.PreferencesManager
import com.ambient.tv.screensaver.data.WeatherData
import com.ambient.tv.screensaver.data.WeatherRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AmbientDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val prefs = PreferencesManager(context)
    private val weatherRepo = WeatherRepository(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val handler = Handler(Looper.getMainLooper())
    private var burnInProtector: BurnInProtector? = null

    // Views
    private lateinit var contentContainer: View
    private lateinit var auraView: AmbientAuraView
    private lateinit var particleView: WeatherParticleView

    private lateinit var layoutHorizontal: View
    private lateinit var layoutStacked: View
    private lateinit var layoutAnalog: View
    private lateinit var layoutDotMatrix: View

    private lateinit var tvClockHorizontal: TextView
    private lateinit var tvAmPm: TextView
    private lateinit var tvClockHours: TextView
    private lateinit var tvClockMinutes: TextView
    private lateinit var analogClockView: AmbientAnalogClockView
    private lateinit var tvClockDotMatrix: TextView
    private lateinit var tvAmPmDotMatrix: TextView

    private lateinit var tvDate: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherSubtitle: TextView
    private lateinit var tvWeatherDetails: TextView

    private val tickerRunnable = object : Runnable {
        override fun run() {
            updateDisplay()
            val now = System.currentTimeMillis()
            // If dot matrix shows seconds, tick every second; else tick on minute boundaries
            val interval = if (prefs.displayMode == DisplayMode.DOT_MATRIX) {
                1000L - (now % 1000L)
            } else {
                60_000L - (now % 60_000L)
            }
            handler.postDelayed(this, interval)
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_ambient_display, this, true)
        initViews()
    }

    private fun initViews() {
        contentContainer = findViewById(R.id.ambientContentContainer)
        auraView = findViewById(R.id.ambientAuraView)
        particleView = findViewById(R.id.weatherParticleView)

        layoutHorizontal = findViewById(R.id.layoutHorizontalClock)
        layoutStacked = findViewById(R.id.layoutStackedClock)
        layoutAnalog = findViewById(R.id.layoutAnalogClock)
        layoutDotMatrix = findViewById(R.id.layoutDotMatrixClock)

        tvClockHorizontal = findViewById(R.id.tvClockHorizontal)
        tvAmPm = findViewById(R.id.tvAmPm)
        tvClockHours = findViewById(R.id.tvClockHours)
        tvClockMinutes = findViewById(R.id.tvClockMinutes)
        analogClockView = findViewById(R.id.analogClockView)
        tvClockDotMatrix = findViewById(R.id.tvClockDotMatrix)
        tvAmPmDotMatrix = findViewById(R.id.tvAmPmDotMatrix)

        tvDate = findViewById(R.id.tvDate)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWeatherSubtitle = findViewById(R.id.tvWeatherSubtitle)
        tvWeatherDetails = findViewById(R.id.tvWeatherDetails)

        burnInProtector = BurnInProtector(contentContainer)
    }

    fun startAmbient() {
        applyThemeAndMode()
        updateDisplay()

        particleView.start()

        handler.removeCallbacks(tickerRunnable)
        tickerRunnable.run()

        if (prefs.isBurnInProtectionEnabled) {
            burnInProtector?.start()
        }

        val cached = prefs.getCachedWeather()
        if (cached != null) {
            displayWeather(cached)
        }

        fetchWeather()
    }

    fun stopAmbient() {
        handler.removeCallbacks(tickerRunnable)
        burnInProtector?.stop()
        particleView.stop()
        coroutineScope.coroutineContext.cancelChildren()
    }

    fun destroy() {
        stopAmbient()
        coroutineScope.cancel()
    }

    private fun applyThemeAndMode() {
        val accentColor = prefs.accentTheme.getColor()

        // Apply colors
        tvAmPm.setTextColor(accentColor)
        tvClockMinutes.setTextColor(accentColor)
        tvClockDotMatrix.setTextColor(accentColor)
        analogClockView.setAccentColor(accentColor)

        // Aura & Particles
        auraView.setAuraColor(accentColor)
        auraView.setAuraEnabled(prefs.isAuraEnabled)
        particleView.setParticleMode(prefs.particleMode)

        // Display Mode Visibility
        layoutHorizontal.visibility = if (prefs.displayMode == DisplayMode.HORIZONTAL) View.VISIBLE else View.GONE
        layoutStacked.visibility = if (prefs.displayMode == DisplayMode.STACKED) View.VISIBLE else View.GONE
        layoutAnalog.visibility = if (prefs.displayMode == DisplayMode.ANALOG) View.VISIBLE else View.GONE
        layoutDotMatrix.visibility = if (prefs.displayMode == DisplayMode.DOT_MATRIX) View.VISIBLE else View.GONE
    }

    private fun updateDisplay() {
        val calendar = Calendar.getInstance()
        val is12Hour = prefs.is12HourFormat

        // Night Dimmer check (between 22:00 and 07:00)
        if (prefs.isNightDimmerEnabled) {
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val isNight = hourOfDay >= 22 || hourOfDay < 7
            contentContainer.alpha = if (isNight) 0.42f else 1.0f
        } else {
            contentContainer.alpha = 1.0f
        }

        // Date: "Saturday, Nov 14"
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        when (prefs.displayMode) {
            DisplayMode.HORIZONTAL -> {
                val timePattern = if (is12Hour) "h:mm" else "HH:mm"
                val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
                tvClockHorizontal.text = timeFormat.format(calendar.time)

                if (is12Hour) {
                    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
                    tvAmPm.text = amPmFormat.format(calendar.time).uppercase(Locale.getDefault())
                    tvAmPm.visibility = View.VISIBLE
                } else {
                    tvAmPm.visibility = View.GONE
                }
            }
            DisplayMode.STACKED -> {
                val hourPattern = if (is12Hour) "hh" else "HH"
                val hourFormat = SimpleDateFormat(hourPattern, Locale.getDefault())
                val minFormat = SimpleDateFormat("mm", Locale.getDefault())

                var hourStr = hourFormat.format(calendar.time)
                if (is12Hour && hourStr.startsWith("0")) {
                    hourStr = hourStr.substring(1)
                }
                tvClockHours.text = hourStr
                tvClockMinutes.text = minFormat.format(calendar.time)
            }
            DisplayMode.ANALOG -> {
                analogClockView.updateTime()
            }
            DisplayMode.DOT_MATRIX -> {
                val dotPattern = if (is12Hour) "hh:mm:ss" else "HH:mm:ss"
                val dotFormat = SimpleDateFormat(dotPattern, Locale.getDefault())
                tvClockDotMatrix.text = dotFormat.format(calendar.time)

                if (is12Hour) {
                    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
                    tvAmPmDotMatrix.text = amPmFormat.format(calendar.time).uppercase(Locale.getDefault())
                    tvAmPmDotMatrix.visibility = View.VISIBLE
                } else {
                    tvAmPmDotMatrix.visibility = View.GONE
                }
            }
        }
    }

    private fun fetchWeather() {
        coroutineScope.launch {
            val weather = weatherRepo.getWeather(forceRefresh = false)
            if (weather != null) {
                displayWeather(weather)
            }
        }
    }

    private fun displayWeather(weather: WeatherData) {
        val isC = prefs.isCelsius
        tvTemperature.text = weather.getFormattedTemp(isC)
        ivWeatherIcon.setImageResource(weather.getIconResId())

        particleView.setWeather(weather.weatherCode)

        val subtitle = if (weather.location.isNotEmpty()) {
            "${weather.getConditionDescription()} • ${weather.location}"
        } else {
            weather.getConditionDescription()
        }
        tvWeatherSubtitle.text = subtitle
        tvWeatherSubtitle.visibility = View.VISIBLE

        if (prefs.isDetailedWeather) {
            val details = "${weather.getFormattedFeelsLike(isC)}   •   ${weather.getFormattedHighLow(isC)}   •   💧 ${weather.humidityPercent}%"
            tvWeatherDetails.text = details
            tvWeatherDetails.visibility = View.VISIBLE
        } else {
            tvWeatherDetails.visibility = View.GONE
        }
    }
}
