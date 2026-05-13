package com.example.weathersnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeatherDto
)

data class CurrentWeatherDto(
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("relative_humidity_2m")
    val humidity: Int,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("pressure_msl")
    val pressure: Double,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)
