package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.util.JwtDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences

actual class TokenPreferences actual constructor() {

    private val preferences: Preferences =
        Preferences.userRoot().node("com.example.kmpincidents")

    actual suspend fun saveToken(token: String) {
        withContext(Dispatchers.IO) {
            preferences.put(TOKEN_KEY, token)
            preferences.flush()
        }
    }

    actual suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        preferences.get(TOKEN_KEY, null)
    }

    actual suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            preferences.remove(TOKEN_KEY)
            preferences.flush()
        }
    }

    actual suspend fun getUserRole(): Role? {
        val token = getToken() ?: return null
        val roleString = JwtDecoder.getRoleFromToken(token)?.uppercase() ?: return null
        return runCatching { Role.valueOf(roleString) }.getOrNull()
    }

    companion object {
        private const val TOKEN_KEY = "auth_token"
    }
}
