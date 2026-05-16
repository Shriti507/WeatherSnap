package com.example.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.data.remote.dto.CityDto
import com.example.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherDomainModel) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val suggestions: StateFlow<List<CityDto>> = _searchQuery
        .debounce(400L)
        .distinctUntilChanged()
        .mapLatest { query ->
            if (query.length > 2) {
                repository.searchCity(query)
            } else {
                emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectCity(city: CityDto) {
        _searchQuery.value = city.name
        // Clear suggestions by forcing the flow state if needed, 
        // but typically navigating or setting query is enough.
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
