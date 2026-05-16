package com.example.weathersnap.ui.saved

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Log
import java.io.File
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.example.weathersnap.R
import com.example.weathersnap.data.local.entity.ReportEntity
import com.example.weathersnap.ui.components.GradientHeaderCard
import com.example.weathersnap.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedReportsScreen(
    onBack: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOlive)
            .padding(horizontal = 16.dp)
    ) {
        GradientHeaderCard(
            title = stringResource(R.string.saved_reports_title),
            subtitle = stringResource(R.string.reports_stored_locally, reports.size),
            actionButton = {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C24).copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.back_button), color = Color.White, fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize().animateContentSize()) {
            if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_reports_yet), color = MutedText)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportItem(report, Modifier.animateItemPlacement())
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: ReportEntity, modifier: Modifier = Modifier) {
    // Safety check: Log if file is missing
    LaunchedEffect(report.imagePath) {
        val file = File(report.imagePath)
        if (!file.exists()) {
            Log.e("SavedReportsScreen", "Image file missing at path: ${report.imagePath}")
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MutedDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(report.imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Weather report image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_report_image),
                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = report.cityName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
                    // Fix: Formatting condition
                    Text(text = report.condition.replace("+", " ").trim(), fontSize = 13.sp, color = MutedText)
                    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(report.timestamp))
                    Text(text = date, fontSize = 11.sp, color = MutedText.copy(alpha = 0.7f))
                }
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A4B1A))
                ) {
                    Text(
                        text = report.temperature,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SizeChip(stringResource(R.string.original_label), report.originalSize, Color(0xFF3D3D34))
                SizeChip(stringResource(R.string.compressed_label), report.compressedSize, Color(0xFF333D2B))
            }
            
            if (report.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Text(
                        text = stringResource(R.string.notes_label, report.notes),
                        fontSize = 13.sp,
                        color = LightText,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SizeChip(label: String, value: String, bgColor: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.height(44.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, fontSize = 9.sp, color = MutedText)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LightText)
        }
    }
}
