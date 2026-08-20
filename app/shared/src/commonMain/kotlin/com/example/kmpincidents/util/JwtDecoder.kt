package com.example.kmpincidents.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

object JwtDecoder {
    @OptIn(ExperimentalEncodingApi::class)
    fun getRoleFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val padded = parts[1].replace('-', '+').replace('_', '/')
            val payloadBase64 = padded + "=".repeat((4 - padded.length % 4) % 4)
            val payload = Base64.decode(payloadBase64).decodeToString()

            val jsonObject = Json.parseToJsonElement(payload) as JsonObject
            jsonObject["role"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}