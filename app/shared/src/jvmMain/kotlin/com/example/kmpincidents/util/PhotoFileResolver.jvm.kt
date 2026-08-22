package com.example.kmpincidents.util

import java.io.File

actual class PhotoFileResolver actual constructor() {
    actual fun resolveToFile(uriString: String): PlatformFile? {
        val file = File(uriString)
        if (!file.exists() || !file.isFile) return null
        return PlatformFile(file)
    }
}
