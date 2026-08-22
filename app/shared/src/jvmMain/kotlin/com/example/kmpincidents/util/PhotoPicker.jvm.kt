package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import javax.swing.SwingUtilities

actual class PhotoPicker internal constructor(
    private val openFilePicker: () -> Unit,
) {
    actual fun pickFromCamera() {
        // Desktop has no camera capture API in this app; fall back to file picker.
        openFilePicker()
    }

    actual fun pickFromGallery() {
        openFilePicker()
    }
}

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker {
    return remember(onPhotoPicked) {
        PhotoPicker(
            openFilePicker = {
                SwingUtilities.invokeLater {
                    val dialog = FileDialog(null as Frame?, "Select photo", FileDialog.LOAD).apply {
                        filenameFilter = FilenameFilter { _, name ->
                            val lower = name.lowercase()
                            lower.endsWith(".png") ||
                                lower.endsWith(".jpg") ||
                                lower.endsWith(".jpeg") ||
                                lower.endsWith(".gif") ||
                                lower.endsWith(".webp") ||
                                lower.endsWith(".bmp")
                        }
                        isVisible = true
                    }
                    val directory = dialog.directory
                    val file = dialog.file
                    if (!directory.isNullOrBlank() && !file.isNullOrBlank()) {
                        onPhotoPicked(java.io.File(directory, file).absolutePath)
                    }
                }
            }
        )
    }
}
