package com.example.kmpincidents.util

import androidx.core.net.toUri

actual class PhotoFileResolver actual constructor() {
    actual fun resolveToFile(uriString: String): PlatformFile? {
        val file = PhotoUtils.getFileFromUri(AndroidContextHolder.appContext, uriString.toUri())
            ?: return null
        return PlatformFile(file)
    }
}