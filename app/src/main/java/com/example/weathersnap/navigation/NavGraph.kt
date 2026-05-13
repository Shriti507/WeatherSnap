package com.example.weathersnap.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.ui.camera.CameraScreen
import com.example.weathersnap.ui.report.CreateReportScreen
import com.example.weathersnap.ui.saved.SavedReportsScreen
import com.example.weathersnap.ui.weather.WeatherScreen

@Composable
fun NavGraph(navController: NavHostController) {
    var selectedWeather by remember { mutableStateOf<WeatherDomainModel?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateToSaved = { navController.navigate(Screen.SavedReports.route) },
                onNavigateToCreateReport = { weather ->
                    selectedWeather = weather
                    navController.navigate(Screen.CreateReport.createRoute("none"))
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateReport.route) { backStackEntry ->
            selectedWeather?.let { weather ->
                CreateReportScreen(
                    weather = weather,
                    capturedImageUri = capturedImageUri,
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    onSaveSuccess = {
                        navController.navigate(Screen.SavedReports.route) {
                            popUpTo(Screen.Weather.route)
                        }
                    }
                )
            }
        }

        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
