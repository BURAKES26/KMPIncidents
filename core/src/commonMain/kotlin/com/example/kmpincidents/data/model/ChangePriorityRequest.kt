package com.example.kmpincidents.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangePriorityRequest(
    val priority: Priority
)
