package com.ambient.tv.screensaver.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository(context: Context) {
    private val prefs = PreferencesManager(context)

    suspend fun getWeather(forceRefresh: Boolean = false): WeatherData? = withContext(Dispatchers.IO) {
        if (!forceRefresh && !prefs.shouldRefreshWeather()) {
            val cached = prefs.getCachedWeather()
            if (cached != null) return@withContext cached
        }

        try {
            var coords = prefs.getCachedCoordinates()
            var city = prefs.getCachedCity()

            if (coords == null || city.isEmpty()) {
                val geo = fetchCoordinatesFromIp()
                if (geo != null) {
                    coords = Pair(geo.first, geo.second)
                    city = geo.third
                    prefs.saveCoordinates(coords.first, coords.second, city)
                }
            }

            if (coords == null) {
                return@withContext prefs.getCachedWeather()
            }

            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=${coords.first}&longitude=${coords.second}&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
            val response = executeGet(weatherUrl, 5000) ?: return@withContext prefs.getCachedWeather()

            val json = JSONObject(response)
            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m").toFloat()
            val feelsLike = current.optDouble("apparent_temperature", temp.toDouble()).toFloat()
            val humidity = current.optInt("relative_humidity_2m", 50)
            val code = current.getInt("weather_code")

            var maxTemp = temp + 3f
            var minTemp = temp - 3f
            if (json.has("daily")) {
                val daily = json.getJSONObject("daily")
                val maxArr = daily.optJSONArray("temperature_2m_max")
                val minArr = daily.optJSONArray("temperature_2m_min")
                if (maxArr != null && maxArr.length() > 0) {
                    maxTemp = maxArr.getDouble(0).toFloat()
                }
                if (minArr != null && minArr.length() > 0) {
                    minTemp = minArr.getDouble(0).toFloat()
                }
            }

            val weatherData = WeatherData(
                temperatureCelsius = temp,
                feelsLikeCelsius = feelsLike,
                tempMaxCelsius = maxTemp,
                tempMinCelsius = minTemp,
                humidityPercent = humidity,
                weatherCode = code,
                location = city
            )

            prefs.saveWeather(weatherData)
            weatherData
        } catch (e: Exception) {
            e.printStackTrace()
            prefs.getCachedWeather()
        }
    }

    private fun fetchCoordinatesFromIp(): Triple<Double, Double, String>? {
        return try {
            val jsonStr = executeGet("https://ipwho.is/", 4000)
            if (jsonStr != null) {
                val json = JSONObject(jsonStr)
                if (json.optBoolean("success", true)) {
                    val lat = json.getDouble("latitude")
                    val lon = json.getDouble("longitude")
                    val city = json.optString("city", "Local")
                    return Triple(lat, lon, city)
                }
            }

            val fallbackStr = executeGet("http://ip-api.com/json/", 4000) ?: return null
            val fallbackJson = JSONObject(fallbackStr)
            val lat = fallbackJson.getDouble("lat")
            val lon = fallbackJson.getDouble("lon")
            val city = fallbackJson.optString("city", "Local")
            Triple(lat, lon, city)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun executeGet(urlStr: String, timeoutMs: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("User-Agent", "AmbientTV/1.0")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = java.lang.StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
