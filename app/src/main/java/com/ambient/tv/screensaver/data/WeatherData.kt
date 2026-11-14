package com.ambient.tv.screensaver.data

import com.ambient.tv.screensaver.R

data class WeatherData(
    val temperatureCelsius: Float,
    val feelsLikeCelsius: Float,
    val tempMaxCelsius: Float,
    val tempMinCelsius: Float,
    val humidityPercent: Int,
    val weatherCode: Int,
    val location: String
) {
    fun getFormattedTemp(isCelsius: Boolean): String {
        return formatTemp(temperatureCelsius, isCelsius)
    }

    fun getFormattedFeelsLike(isCelsius: Boolean): String {
        return "Feels ${formatTemp(feelsLikeCelsius, isCelsius)}"
    }

    fun getFormattedHighLow(isCelsius: Boolean): String {
        return "H: ${formatTemp(tempMaxCelsius, isCelsius)}  L: ${formatTemp(tempMinCelsius, isCelsius)}"
    }

    private fun formatTemp(tempC: Float, isCelsius: Boolean): String {
        return if (isCelsius) {
            "${tempC.toInt()}°C"
        } else {
            val f = (tempC * 9f / 5f) + 32f
            "${f.toInt()}°F"
        }
    }

    fun getConditionDescription(): String {
        return when (weatherCode) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Heavy Thunderstorm"
            else -> "Partly Cloudy"
        }
    }

    fun getIconResId(): Int {
        return when (weatherCode) {
            0 -> R.drawable.ic_weather_clear
            1, 2, 3 -> R.drawable.ic_weather_clouds
            45, 48 -> R.drawable.ic_weather_fog
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> R.drawable.ic_weather_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_weather_snow
            95, 96, 99 -> R.drawable.ic_weather_thunder
            else -> R.drawable.ic_weather_clouds
        }
    }
}
