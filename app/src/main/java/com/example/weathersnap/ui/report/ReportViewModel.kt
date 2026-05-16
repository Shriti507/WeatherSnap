package com.example.weathersnap.ui.report

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.local.entity.ReportEntity
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.data.repository.ReportRepository
import com.example.weathersnap.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ReportUiState(
    val weather: WeatherDomainModel? = null,
    val imagePath: String? = null,
    val originalSize: String = "",
    val compressedSize: String = "",
    val notes: String = "",
    val isSaved: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun setWeather(weather: WeatherDomainModel) {
        _uiState.value = _uiState.value.copy(weather = weather)
    }

    fun processCapturedImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val path = uri.path ?: return@launch
                val (imagePath, originalSize, compressedSize) = withContext(Dispatchers.IO) {
                    val originalFile = File(path)
                    val originalSizeStr = if (originalFile.exists()) ImageUtils.getReadableFileSize(originalFile) else "Unknown"
                    
                    val compressedFile = ImageUtils.compressImage(context, uri)
                    val compressedSizeStr = ImageUtils.getReadableFileSize(compressedFile)
                    
                    Triple(compressedFile.absolutePath, originalSizeStr, compressedSizeStr)
                }
                
                _uiState.value = _uiState.value.copy(
                    imagePath = imagePath,
                    originalSize = originalSize,
                    compressedSize = compressedSize
                )
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Failed to process image", e)
            }
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveReport() {
        val state = _uiState.value
        val weather = state.weather ?: return
        val imagePath = state.imagePath ?: return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val entity = ReportEntity(
                    cityName = weather.cityName,
                    temperature = weather.temperature,
                    condition = weather.condition,
                    humidity = weather.humidity,
                    windSpeed = weather.windSpeed,
                    pressure = weather.pressure,
                    notes = state.notes,
                    imagePath = imagePath,
                    originalSize = state.originalSize,
                    compressedSize = state.compressedSize
                )
                repository.saveReport(entity)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
