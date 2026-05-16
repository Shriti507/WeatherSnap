# WeatherSnap 

**WeatherSnap** is a modern, production-style Android application that allows users to search for real-time weather data and create evidence-based weather reports using a custom camera and local persistence.

## Features
- **Smart City Search**: Real-time autocomplete suggestions using Open-Meteo Geocoding API with in-memory caching and search debouncing.
- **Detailed Weather**: View temperature, conditions, humidity, wind speed, and pressure.
- **Custom CameraX**: Fullscreen custom camera interface for capturing weather evidence photos.
- **Image Optimization**: Automatic sub-sampling and JPEG compression to reduce storage footprint while maintaining quality.
- **Local Persistence**: Reports are stored permanently in a Room Database, including notes and image metadata.


## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **DI**: Dagger Hilt
- **Networking**: Retrofit & OkHttp (with Logging Interceptor)
- **Database**: Room
- **Camera**: CameraX (Preview, Capture, Lifecycle)
- **Navigation**: Compose Navigation
- **Image Loading**: Coil
- **JSON**: Gson

## Architecture
The project follows clean architecture principles with a modular package structure:
- `data`: Remote (Retrofit) and Local (Room) data sources.
- `di`: Hilt modules for dependency injection.
- `ui`: Composable screens, ViewModels, and State management.
- `navigation`: Type-safe NavHost implementation.
- `utils`: Image processing and file management utilities.

## Setup Instructions
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/Shriti507/WeatherSnap.git
    ```
2.  **Open in Android Studio**: Launch Android Studio and open the project folder.
3.  **Sync Gradle**: Allow Android Studio to download dependencies and sync the build files.
4.  **Run**: Click the **Run** button to deploy the app to an emulator (API 30+) or a physical device.


