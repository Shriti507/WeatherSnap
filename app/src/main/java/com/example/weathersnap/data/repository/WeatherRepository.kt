package com.example.weathersnap.data.repository

import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.data.remote.OpenMeteoApi
import com.example.weathersnap.data.remote.dto.CityDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: OpenMeteoApi
) {
    private val suggestionsCache = mutableMapOf<String, List<CityDto>>()

    suspend fun searchCity(query: String): List<CityDto> {
        if (query.length <= 2) return emptyList()
        
        suggestionsCache[query.lowercase()]?.let { return it }

        return try {
            val response = api.searchCity(query)
            val results = response.results ?: emptyList()
            suggestionsCache[query.lowercase()] = results
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeather(city: CityDto): WeatherDomainModel {
        val response = api.getWeather(city.latitude, city.longitude)
        val current = response.current
        
        return WeatherDomainModel(
            cityName = city.name,
            temperature = "${current.temperature}°C",
            condition = getWeatherCondition(current.weatherCode),
            humidity = "${current.humidity}%",
            windSpeed = "${current.windSpeed} km/h",
            pressure = "${current.pressure} hPa",
            weatherCode = current.weatherCode
        )
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}
