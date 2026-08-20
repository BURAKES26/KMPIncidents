package com.example.kmpincidents.util

expect class PhotoFileResolver() {
    fun resolveToFile(uriString: String): java.io.File?
}