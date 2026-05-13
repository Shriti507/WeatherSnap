package com.example.weathersnap.ui.report

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.weathersnap.data.model.WeatherDomainModel

@Composable
fun CreateReportScreen(
    weather: WeatherDomainModel,
    capturedImageUri: Uri?,
    onNavigateToCamera: () -> Unit,
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Weather Report", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = weather.cityName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "${weather.temperature} - ${weather.condition}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(8.dp),
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
                Text("No image captured")
            }
        }

        if (uiState.imagePath != null) {
            Text(
                text = "Original: ${uiState.originalSize} | Compressed: ${uiState.compressedSize}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Button(onClick = onNavigateToCamera) {
            Text(if (uiState.imagePath == null) "Capture Photo" else "Retake Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = { viewModel.onNotesChange(it) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveReport() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.imagePath != null
        ) {
            Text("Save Report")
        }
    }
}
