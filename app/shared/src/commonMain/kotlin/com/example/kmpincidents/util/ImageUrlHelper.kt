package com.example.kmpincidents.util

object ImageUrlHelper {
    private val baseUrl: String get() = "http://$backendHost:8080"

    fun getFullImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null
        return if (imagePath.startsWith("http")) {
            imagePath
        } else {
            "$baseUrl/uploads/incidentsimages/$imagePath"
        }
    }
}