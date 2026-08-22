package com.example.kmpincidents.util

import java.io.File

actual class PlatformFile(private val file: File) {
    actual val name: String get() = file.name
    actual fun exists(): Boolean = file.exists()
    actual suspend fun readBytes(): ByteArray = file.readBytes()
}
