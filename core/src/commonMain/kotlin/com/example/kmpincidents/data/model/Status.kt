package com.example.kmpincidents.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    REPORTED, ASSIGNED, RESOLVED
}