package com.example.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ImageUtils {

    /**
     * Processes the raw image from CameraX:
     * 1. Decodes with sub-sampling to prevent OOM.
     * 2. Compresses to JPEG with 70% quality.
     * 3. Saves to PERMANENT internal storage (/files/reports/).
     * 4. Returns the permanent absolute path.
     */
    suspend fun processAndSavePermanentImage(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            
            // 1. Check dimensions for sub-sampling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 2. Calculate inSampleSize (target max 1024px)
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            // 3. Decode sub-sampled bitmap
            val inputStreamScaled = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStreamScaled, null, options)
            inputStreamScaled?.close()

            // 4. Create PERMANENT directory
            val reportsDir = File(context.filesDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            
            val filename = "WS_REPORT_${System.currentTimeMillis()}.jpg"
            val permanentFile = File(reportsDir, filename)
            
            // 5. Save compressed bitmap
            bitmap?.let {
                val outputStream = FileOutputStream(permanentFile)
                it.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                outputStream.flush()
                outputStream.close()
                it.recycle() 
                
                Log.d("ImageUtils", "Image saved permanently at: ${permanentFile.absolutePath}")
                return@withContext permanentFile.absolutePath
            }
            
            throw Exception("Failed to decode bitmap")
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error processing image", e)
            throw e
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getReadableFileSize(file: File): String {
        if (!file.exists()) return "0 B"
        val bytes = file.length()
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format(Locale.ROOT, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }
}
