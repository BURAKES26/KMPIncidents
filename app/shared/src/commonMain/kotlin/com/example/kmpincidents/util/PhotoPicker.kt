package com.example.kmpincidents.util

import androidx.compose.runtime.Composable

expect class PhotoPicker {
    fun pickFromCamera()
    fun pickFromGallery()
}

@Composable
expect fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker