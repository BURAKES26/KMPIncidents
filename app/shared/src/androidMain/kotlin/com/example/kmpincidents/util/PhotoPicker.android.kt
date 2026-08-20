package com.example.kmpincidents.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class PhotoPicker(
    private val onLaunchGallery: () -> Unit,
    private val onLaunchCamera: () -> Unit
) {
    actual fun pickFromGallery() = onLaunchGallery()
    actual fun pickFromCamera() = onLaunchCamera()
}

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker {
    val context = LocalContext.current
    var cameraUri = remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onPhotoPicked(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri.value?.let { onPhotoPicked(it.toString()) }
        }
    }

    return remember {
        PhotoPicker(
            onLaunchGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onLaunchCamera = {
                val uri = PhotoUtils.createImageUri(context)
                cameraUri.value = uri
                if (uri != null) {
                    cameraLauncher.launch(uri)
                }
            }
        )
    }
}