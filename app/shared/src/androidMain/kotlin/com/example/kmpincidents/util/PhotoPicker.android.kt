package com.example.kmpincidents.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

actual class PhotoPicker(
    private val launchCamera: (Uri) -> Unit,
    private val launchGallery: () -> Unit,
    private val pendingCameraUri: () -> Uri?
) {
    actual fun pickFromCamera() {
        pendingCameraUri()?.let { launchCamera(it) }
    }

    actual fun pickFromGallery() {
        launchGallery()
    }
}

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingUri?.let { onPhotoPicked(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onPhotoPicked(it.toString()) }
    }

    return remember {
        PhotoPicker(
            launchCamera = { uri -> pendingUri = uri; cameraLauncher.launch(uri) },
            launchGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            pendingCameraUri = { PhotoUtils.createImageUri(context)?.also { pendingUri = it } }
        )
    }
}