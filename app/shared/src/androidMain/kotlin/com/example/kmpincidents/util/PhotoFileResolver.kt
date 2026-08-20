package com.example.kmpincidents.util

import androidx.core.net.toUri
import java.io.File

actual class PhotoFileResolver actual constructor() {
    actual fun resolveToFile(uriString: String): File? {
        return PhotoUtils.getFileFromUri(AndroidContextHolder.appContext, uriString.toUri())
    }
}