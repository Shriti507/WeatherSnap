package com.example.weathersnap.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.privacysandbox.tools.core.generator.build
import java.io.File
import java.util.concurrent.Executors

@androidx.compose.runtime.Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = androidx.compose.runtime.remember { ContextCompat.getMainExecutor(context) }
    val cameraExecutor = androidx.compose.runtime.remember { Executors.newSingleThreadExecutor() }
    val imageCapture = androidx.compose.runtime.remember { androidx.camera.core.ImageCapture.Builder().build() }

    var hasPermission by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        try {
                            val provider = providerFuture.get()
                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner,
                                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Binding failed", e)
                        }
                    }, mainExecutor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Capture Button
            androidx.compose.material3.Button(
                onClick = {
                    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
                    val options =
                        androidx.camera.core.ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(options, cameraExecutor, object :
                        androidx.camera.core.ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(results: androidx.camera.core.ImageCapture.OutputFileResults) {
                            val uri = Uri.fromFile(file)
                            // THREADING FIX: Switch to main thread for navigation/state updates
                            mainExecutor.execute {
                                onImageCaptured(uri)
                            }
                        }
                        override fun onError(e: androidx.camera.core.ImageCaptureException) {
                            Log.e("CameraScreen", "Capture failed: ${e.message}")
                        }
                    })
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp).size(80.dp),
                shape = CircleShape
            ) {
                androidx.compose.material3.Text("Capture")
            }

            androidx.compose.material3.IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                androidx.compose.material3.Text("Close", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}