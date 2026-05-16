package com.example.weathersnap.ui.weather

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.ui.components.*
import com.example.weathersnap.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.weathersnap.R

@Composable
fun WeatherScreen(
    onNavigateToSaved: () -> Unit,
    onNavigateToCreateReport: (WeatherDomainModel) -> Unit,
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOlive)
            .padding(horizontal = 16.dp)
    ) {
        GradientHeaderCard(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.app_subtitle),
            actionButton = {
                Button(
                    onClick = onNavigateToSaved,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C24).copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.reports_button), color = Color.White, fontSize = 12.sp)
                }
            }
        )

        SectionCard {
            Text(text = stringResource(R.string.city_label), fontSize = 12.sp, color = MutedText)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = LimeGreen
                    ),
                    placeholder = { Text(stringResource(R.string.search_placeholder), color = MutedText.copy(alpha = 0.5f)) }
                )
                Button(
                    onClick = { /* ViewModel handles it via debounce */ },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.search_button), color = LimeGreen, fontSize = 12.sp)
                }
            }
            Text(
                text = stringResource(R.string.search_helper_text),
                fontSize = 10.sp,
                color = MutedText,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF33332B))
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(suggestions) { city ->
                        ListItem(
                            headlineContent = { Text(city.name, color = LightText) },
                            supportingContent = { Text("${city.admin1 ?: ""}, ${city.country ?: ""}", color = MutedText) },
                            modifier = Modifier.clickable { viewModel.selectCity(city) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LimeGreen)
                    }
                }
                is WeatherUiState.Error -> {
                    Text(
                        text = stringResource(R.string.error_prefix, state.message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is WeatherUiState.Success -> {
                    WeatherDetailCard(
                        weather = state.weather,
                        onCreateReport = { onNavigateToCreateReport(state.weather) }
                    )
                }
                is WeatherUiState.Idle -> { }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(
    weather: WeatherDomainModel,
    onCreateReport: () -> Unit
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = weather.cityName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightText,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = weather.condition.replace("+", " ").trim(),
                    fontSize = 14.sp,
                    color = MutedText,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A4B1A))
            ) {
                Text(
                    text = weather.temperature,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LimeGreen,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherMetricCard(stringResource(R.string.humidity_label), weather.humidity, HumidityColor, Modifier.weight(1f))
            WeatherMetricCard(stringResource(R.string.wind_label), weather.windSpeed, WindColor, Modifier.weight(1f))
            WeatherMetricCard(stringResource(R.string.pressure_label), weather.pressure.take(5), PressureColor, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.report_readiness), color = MutedText, fontSize = 14.sp)
            Text(text = stringResource(R.string.camera_room_enabled), color = LightText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        StyledButton(
            text = stringResource(R.string.create_report_button),
            onClick = onCreateReport
        )
    }
}
