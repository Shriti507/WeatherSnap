package com.example.weathersnap.navigation

sealed class Screen(val route: String) {
    object Weather : Screen("weather")
    object Camera : Screen("camera")
    object CreateReport : Screen("create_report/{imagePath}") {
        fun createRoute(imagePath: String) = "create_report/$imagePath"
    }
    object SavedReports : Screen("saved_reports")
}
