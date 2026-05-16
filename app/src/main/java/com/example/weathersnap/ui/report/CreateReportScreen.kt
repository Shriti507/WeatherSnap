package com.example.weathersnap.ui.report

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.ui.components.*
import com.example.weathersnap.ui.theme.*

@Composable
fun CreateReportScreen(
    weather: WeatherDomainModel,
    capturedImageUri: Uri?,
    onNavigateToCamera: () -> Unit,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(weather) {
        viewModel.setWeather(weather)
    }

    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let {
            viewModel.processCapturedImage(context, it)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOlive)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeaderCard(
            title = "Create Report",
            subtitle = "Capture, compress, annotate",
            actionButton = {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C24).copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back", color = Color.White, fontSize = 12.sp)
                }
            }
        )

        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weather.cityName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = weather.condition.replace("+", " ").trim(),
                        fontSize = 13.sp,
                        color = MutedText,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A4B1A))
                ) {
                    Text(
                        text = weather.temperature,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGreen,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .widthIn(min = 48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherMetricCard("Humidity", weather.humidity, HumidityColor, Modifier.weight(1f))
                WeatherMetricCard("Wind", weather.windSpeed, WindColor, Modifier.weight(1f))
                WeatherMetricCard("Pressure", weather.pressure.take(5), PressureColor, Modifier.weight(1f))
            }
        }

        SectionCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF33332B)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imagePath != null) {
                    AsyncImage(
                        model = uiState.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Photo preview", color = MutedText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            StyledButton(
                text = if (uiState.imagePath == null) "Capture Photo" else "Retake Photo",
                onClick = onNavigateToCamera,
                containerColor = LimeGreen.copy(alpha = 0.3f),
                contentColor = LimeGreen
            )
            
            if (uiState.imagePath != null) {
                Text(
                    text = "Original: ${uiState.originalSize} | Compressed: ${uiState.compressedSize}",
                    fontSize = 10.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }

        SectionCard {
            Text(text = "Field Notes", fontSize = 14.sp, color = LightText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Notes", color = MutedText.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MutedText,
                    unfocusedBorderColor = MutedText.copy(alpha = 0.3f)
                ),
                minLines = 4
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StyledButton(
            text = "Save Report",
            onClick = { viewModel.saveReport() },
            enabled = uiState.imagePath != null
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
