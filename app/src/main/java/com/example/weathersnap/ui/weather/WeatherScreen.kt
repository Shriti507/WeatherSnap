package com.example.weathersnap.ui.weather

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersnap.data.model.WeatherDomainModel

@Composable
fun WeatherScreen(
    onNavigateToSaved: () -> Unit,
    onNavigateToCreateReport: (WeatherDomainModel) -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "WeatherSnap", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onNavigateToSaved) {
                Text("Saved Reports")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            label = { Text("Search City") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            supportingText = {
                Text("Enter more than 2 letters to start city suggestions.")
            }
        )

        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(suggestions) { city ->
                        ListItem(
                            headlineContent = { Text(city.name) },
                            supportingContent = { Text("${city.admin1 ?: ""}, ${city.country ?: ""}") },
                            modifier = Modifier.clickable { viewModel.selectCity(city) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> CircularProgressIndicator()
                is WeatherUiState.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                is WeatherUiState.Success -> {
                    WeatherDetailCard(
                        weather = state.weather,
                        onCreateReport = { onNavigateToCreateReport(state.weather) }
                    )
                }
                is WeatherUiState.Idle -> {
                    Text("Search for a city to see weather", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(
    weather: WeatherDomainModel,
    onCreateReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = weather.cityName, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = weather.temperature, fontSize = 48.sp, fontWeight = FontWeight.Medium)
            Text(text = weather.condition, fontSize = 20.sp)
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                WeatherInfoItem("Humidity", weather.humidity)
                WeatherInfoItem("Wind", weather.windSpeed)
                WeatherInfoItem("Pressure", weather.pressure)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateReport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Report")
            }
        }
    }
}

@Composable
fun WeatherInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
