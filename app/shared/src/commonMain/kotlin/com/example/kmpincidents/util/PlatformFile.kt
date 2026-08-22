package com.example.kmpincidents.util

expect class PlatformFile {
    val name: String
    fun exists(): Boolean
    suspend fun readBytes(): ByteArray
}
