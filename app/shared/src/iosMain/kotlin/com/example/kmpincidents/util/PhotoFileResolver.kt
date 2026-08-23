package com.example.kmpincidents.util

import platform.Foundation.NSURL

actual class PhotoFileResolver actual constructor() {
    actual fun resolveToFile(uriString: String): PlatformFile? {
        if (uriString.isBlank()) return null

        val path = when {
            uriString.startsWith("file://") ->
                NSURL.URLWithString(uriString)?.path
            else -> uriString
        } ?: return null

        val file = PlatformFile(path)
        return if (file.exists()) file else null
    }
}
