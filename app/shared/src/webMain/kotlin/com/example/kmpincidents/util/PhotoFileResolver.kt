package com.example.kmpincidents.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual class PhotoFileResolver actual constructor() {

    @OptIn(ExperimentalEncodingApi::class)
    actual fun resolveToFile(uriString: String): PlatformFile? {
        if (!uriString.startsWith("data:")) return null

        val commaIndex = uriString.indexOf(',')
        if (commaIndex <= 5) return null

        val meta = uriString.substring(5, commaIndex) // e.g. image/png;base64
        val dataPart = uriString.substring(commaIndex + 1)
        if (!meta.contains("base64", ignoreCase = true)) return null

        val mime = meta.substringBefore(';').ifBlank { "application/octet-stream" }
        val extension = when (mime.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "bin"
        }

        val bytes = runCatching { Base64.decode(dataPart) }.getOrNull() ?: return null
        return PlatformFile(name = "photo.$extension", bytes = bytes, present = true)
    }
}
