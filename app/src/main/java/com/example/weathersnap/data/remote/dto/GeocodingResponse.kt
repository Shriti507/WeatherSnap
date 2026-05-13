package com.example.weathersnap.data.remote.dto

data class GeocodingResponse(
    val results: List<CityDto>?
)

data class CityDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)
