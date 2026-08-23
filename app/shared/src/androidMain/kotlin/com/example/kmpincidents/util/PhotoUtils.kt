package com.example.kmpincidents.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object PhotoUtils {

    fun createImageUri(context: Context): Uri? {
        return try {
            val timeStamp = createTimestamp()
            val imageFileName = "INCIDENT_${timeStamp}.jpg"
            val storageDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(storageDir, imageFileName)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val timeStamp = createTimestamp()
            val fileName = "incident_image_$timeStamp.jpg"
            val outputFile = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createTimestamp(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "%04d%02d%02d_%02d%02d%02d".format(
            now.year, now.month.number, now.day,
            now.hour, now.minute, now.second
        )
    }
}
