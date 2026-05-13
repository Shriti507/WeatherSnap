package com.example.weathersnap.data.model

data class WeatherDomainModel(
    val cityName: String,
    val temperature: String,
    val condition: String,
    val humidity: String,
    val windSpeed: String,
    val pressure: String,
    val weatherCode: Int
)
