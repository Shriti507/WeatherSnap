package com.example.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val temperature: String,
    val condition: String,
    val humidity: String,
    val windSpeed: String,
    val pressure: String,
    val notes: String,
    val imagePath: String,
    val originalSize: String,
    val compressedSize: String,
    val timestamp: Long = System.currentTimeMillis()
)
