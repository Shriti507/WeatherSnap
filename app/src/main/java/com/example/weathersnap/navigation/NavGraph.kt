package com.example.weathersnap.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.example.weathersnap.data.model.WeatherDomainModel
import com.example.weathersnap.ui.camera.CameraScreen
import com.example.weathersnap.ui.report.CreateReportScreen
import com.example.weathersnap.ui.saved.SavedReportsScreen
import com.example.weathersnap.ui.weather.WeatherScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(navController: NavHostController, gson: Gson) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateToSaved = {
                    navController.navigate(Screen.SavedReports.route)
                },
                onNavigateToCreateReport = { weather ->
                    try {
                        val weatherJson = gson.toJson(weather)
                        val encodedWeather = URLEncoder.encode(weatherJson, StandardCharsets.UTF_8.toString())
                        navController.navigate("create_report/$encodedWeather")
                    } catch (e: Exception) {
                        Log.e("NavGraph", "Navigation encoding failed", e)
                    }
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("capturedImageUri", uri.toString())
                    navController.popBackStack()
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "create_report/{weatherJson}",
            arguments = listOf(
                navArgument("weatherJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val weatherJson = backStackEntry.arguments?.getString("weatherJson") ?: ""
            
            val weather: WeatherDomainModel? = remember(weatherJson) {
                try {
                    gson.fromJson(weatherJson, WeatherDomainModel::class.java)
                } catch (e: Exception) {
                    Log.e("NavGraph", "JSON parsing failed", e)
                    null
                }
            }

            val capturedImageUriString by backStackEntry.savedStateHandle
                .getStateFlow<String?>("capturedImageUri", null)
                .collectAsState()

            val capturedImageUri = remember(capturedImageUriString) {
                capturedImageUriString?.let { Uri.parse(it) }
            }

            if (weather != null) {
                CreateReportScreen(
                    weather = weather,
                    capturedImageUri = capturedImageUri,
                    onNavigateToCamera = {
                        navController.navigate(Screen.Camera.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        backStackEntry.savedStateHandle.remove<String>("capturedImageUri")
                        navController.navigate(Screen.SavedReports.route) {
                            popUpTo(Screen.Weather.route)
                        }
                    }
                )
            }
        }

        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
