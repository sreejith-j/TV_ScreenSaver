package com.ambient.tv.screensaver.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ambient_tv_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DISPLAY_MODE = "key_display_mode"
        const val KEY_ACCENT_THEME = "key_accent_theme"
        const val KEY_12_HOUR = "key_12_hour"
        const val KEY_IS_CELSIUS = "key_is_celsius"
        const val KEY_BURN_IN = "key_burn_in"
        const val KEY_AURA = "key_aura"
        const val KEY_PARTICLE_MODE = "key_particle_mode"
        const val KEY_DETAILED_WEATHER = "key_detailed_weather"
        const val KEY_NIGHT_DIMMER = "key_night_dimmer"

        // Weather Cache Keys
        const val KEY_CACHED_TEMP = "key_cached_temp"
        const val KEY_CACHED_FEELS_LIKE = "key_cached_feels_like"
        const val KEY_CACHED_TEMP_MAX = "key_cached_temp_max"
        const val KEY_CACHED_TEMP_MIN = "key_cached_temp_min"
        const val KEY_CACHED_HUMIDITY = "key_cached_humidity"
        const val KEY_CACHED_CODE = "key_cached_code"
        const val KEY_CACHED_LOCATION = "key_cached_location"
        const val KEY_CACHED_LAT = "key_cached_lat"
        const val KEY_CACHED_LON = "key_cached_lon"
        const val KEY_LAST_WEATHER_TIME = "key_last_weather_time"
    }

    var displayMode: DisplayMode
        get() = DisplayMode.fromId(prefs.getString(KEY_DISPLAY_MODE, DisplayMode.HORIZONTAL.id) ?: DisplayMode.HORIZONTAL.id)
        set(value) = prefs.edit().putString(KEY_DISPLAY_MODE, value.id).apply()

    var accentTheme: AccentTheme
        get() = AccentTheme.fromId(prefs.getString(KEY_ACCENT_THEME, AccentTheme.GOOGLE_BLUE.id) ?: AccentTheme.GOOGLE_BLUE.id)
        set(value) = prefs.edit().putString(KEY_ACCENT_THEME, value.id).apply()

    var is12HourFormat: Boolean
        get() = prefs.getBoolean(KEY_12_HOUR, true)
        set(value) = prefs.edit().putBoolean(KEY_12_HOUR, value).apply()

    var isCelsius: Boolean
        get() = prefs.getBoolean(KEY_IS_CELSIUS, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_CELSIUS, value).apply()

    var isBurnInProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_BURN_IN, true)
        set(value) = prefs.edit().putBoolean(KEY_BURN_IN, value).apply()

    var isAuraEnabled: Boolean
        get() = prefs.getBoolean(KEY_AURA, true)
        set(value) = prefs.edit().putBoolean(KEY_AURA, value).apply()

    var particleMode: ParticleMode
        get() = ParticleMode.fromId(prefs.getString(KEY_PARTICLE_MODE, ParticleMode.AUTO.id) ?: ParticleMode.AUTO.id)
        set(value) = prefs.edit().putString(KEY_PARTICLE_MODE, value.id).apply()

    var isDetailedWeather: Boolean
        get() = prefs.getBoolean(KEY_DETAILED_WEATHER, true)
        set(value) = prefs.edit().putBoolean(KEY_DETAILED_WEATHER, value).apply()

    var isNightDimmerEnabled: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_DIMMER, true)
        set(value) = prefs.edit().putBoolean(KEY_NIGHT_DIMMER, value).apply()

    fun getCachedWeather(): WeatherData? {
        val temp = prefs.getFloat(KEY_CACHED_TEMP, -999f)
        if (temp == -999f) return null
        val feels = prefs.getFloat(KEY_CACHED_FEELS_LIKE, temp)
        val max = prefs.getFloat(KEY_CACHED_TEMP_MAX, temp + 4f)
        val min = prefs.getFloat(KEY_CACHED_TEMP_MIN, temp - 4f)
        val humidity = prefs.getInt(KEY_CACHED_HUMIDITY, 50)
        val code = prefs.getInt(KEY_CACHED_CODE, 0)
        val loc = prefs.getString(KEY_CACHED_LOCATION, "") ?: ""
        return WeatherData(temp, feels, max, min, humidity, code, loc)
    }

    fun saveWeather(data: WeatherData) {
        prefs.edit()
            .putFloat(KEY_CACHED_TEMP, data.temperatureCelsius)
            .putFloat(KEY_CACHED_FEELS_LIKE, data.feelsLikeCelsius)
            .putFloat(KEY_CACHED_TEMP_MAX, data.tempMaxCelsius)
            .putFloat(KEY_CACHED_TEMP_MIN, data.tempMinCelsius)
            .putInt(KEY_CACHED_HUMIDITY, data.humidityPercent)
            .putInt(KEY_CACHED_CODE, data.weatherCode)
            .putString(KEY_CACHED_LOCATION, data.location)
            .putLong(KEY_LAST_WEATHER_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getCachedCoordinates(): Pair<Double, Double>? {
        val lat = prefs.getString(KEY_CACHED_LAT, null)?.toDoubleOrNull()
        val lon = prefs.getString(KEY_CACHED_LON, null)?.toDoubleOrNull()
        return if (lat != null && lon != null) Pair(lat, lon) else null
    }

    fun saveCoordinates(lat: Double, lon: Double, city: String) {
        prefs.edit()
            .putString(KEY_CACHED_LAT, lat.toString())
            .putString(KEY_CACHED_LON, lon.toString())
            .putString(KEY_CACHED_LOCATION, city)
            .apply()
    }

    fun getCachedCity(): String {
        return prefs.getString(KEY_CACHED_LOCATION, "") ?: ""
    }

    fun shouldRefreshWeather(): Boolean {
        val last = prefs.getLong(KEY_LAST_WEATHER_TIME, 0L)
        // Refresh every 30 minutes
        return (System.currentTimeMillis() - last) > 30 * 60 * 1000
    }
}
