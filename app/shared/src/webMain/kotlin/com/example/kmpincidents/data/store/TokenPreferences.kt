package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.util.JwtDecoder
import web.storage.localStorage

private const val TOKEN_KEY = "auth_token"

actual class TokenPreferences actual constructor() {

    actual suspend fun saveToken(token: String) {
        localStorage.setItem(TOKEN_KEY, token)
    }

    actual suspend fun getToken(): String? = localStorage.getItem(TOKEN_KEY)

    actual suspend fun clearToken() {
        localStorage.removeItem(TOKEN_KEY)
    }

    actual suspend fun getUserRole(): Role? {
        val token = getToken() ?: return null
        val roleString = JwtDecoder.getRoleFromToken(token)?.uppercase() ?: return null
        return runCatching { Role.valueOf(roleString) }.getOrNull()
    }
}
