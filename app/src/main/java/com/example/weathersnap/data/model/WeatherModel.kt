package com.example.weathersnap.data.model

data class WeatherModel(
    val cityName: String,
    val temperature: String,
    val condition: String,
    val icon: String // e.g., "☀️", "☁️", "🌧️"
)
