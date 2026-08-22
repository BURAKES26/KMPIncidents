package com.example.kmpincidents.util

actual class PlatformFile internal constructor(
    actual val name: String,
    private val bytes: ByteArray,
    private val present: Boolean = true,
) {
    actual fun exists(): Boolean = present

    actual suspend fun readBytes(): ByteArray = bytes
}
