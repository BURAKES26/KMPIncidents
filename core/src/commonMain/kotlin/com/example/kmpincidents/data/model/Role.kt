package com.example.kmpincidents.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    USER, OFFICIAL, ADMIN
}