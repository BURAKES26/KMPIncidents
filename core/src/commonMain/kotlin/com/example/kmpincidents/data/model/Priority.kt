package com.example.kmpincidents.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}