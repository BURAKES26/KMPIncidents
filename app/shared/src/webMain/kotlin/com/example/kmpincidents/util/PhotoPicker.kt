package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import web.dom.document
import web.events.EventHandler
import web.file.FileReader
import web.html.HTMLInputElement

actual class PhotoPicker internal constructor(
    private val openPicker: () -> Unit,
) {
    actual fun pickFromCamera() {
        // Browsers cannot reliably open the device camera via a generic API without
        // getUserMedia + MediaCapture; fall back to the file picker (often offers camera on mobile).
        openPicker()
    }

    actual fun pickFromGallery() {
        openPicker()
    }
}

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker {
    return remember(onPhotoPicked) {
        val input = document.createElement("input") as HTMLInputElement
        input.setAttribute("type", "file")
        input.accept = "image/*"
        input.style.display = "none"
        document.body?.appendChild(input)

        input.onchange = EventHandler {
            val file = input.files?.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = EventHandler {
                    val result = reader.result
                    val dataUrl = result?.toString()
                    if (!dataUrl.isNullOrBlank()) {
                        onPhotoPicked(dataUrl)
                    }
                }
                reader.readAsDataURL(file)
            }
            // Allow selecting the same file again later
            input.value = ""
        }

        PhotoPicker(openPicker = {
            input.click()
        })
    }
}
