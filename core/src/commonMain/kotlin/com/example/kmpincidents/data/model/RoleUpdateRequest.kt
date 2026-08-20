package com.example.kmpincidents.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RoleUpdateRequest(
    val role: Role
)
