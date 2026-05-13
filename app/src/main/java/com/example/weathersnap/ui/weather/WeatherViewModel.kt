package com.example.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.data.remote.dto.CityDto
import com.example.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherDomainModel) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<CityDto>>(emptyList())
    val suggestions: StateFlow<List<CityDto>> = _suggestions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length > 2) {
            viewModelScope.launch {
                _suggestions.value = repository.searchCity(newQuery)
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun selectCity(city: CityDto) {
        _searchQuery.value = city.name
        _suggestions.value = emptyList()
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeather(city)
                _uiState.value = WeatherUiState.Success(weather)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Failed to fetch weather")
            }
        }
    }
}
