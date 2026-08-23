package com.example.kmpincidents.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

actual class PlatformFile(private val path: String) {
    actual val name: String
        get() = path.substringAfterLast('/').ifEmpty { path }

    actual fun exists(): Boolean =
        NSFileManager.defaultManager.fileExistsAtPath(path)

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readBytes(): ByteArray {
        val data = NSData.dataWithContentsOfFile(path) ?: return ByteArray(0)
        return data.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val result = ByteArray(size)
    val source = bytes ?: return ByteArray(0)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, length)
    }
    return result
}
