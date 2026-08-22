package com.example.kmpincidents.util

expect class PhotoFileResolver() {
    fun resolveToFile(uriString: String): PlatformFile?
}