package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.util.JwtDecoder
import platform.Foundation.NSUserDefaults

actual class TokenPreferences actual constructor() {

    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun saveToken(token: String) {
        defaults.setObject(token, forKey = TOKEN_KEY)
        defaults.synchronize()
    }

    actual suspend fun getToken(): String? {
        return defaults.stringForKey(TOKEN_KEY)
    }

    actual suspend fun clearToken() {
        defaults.removeObjectForKey(TOKEN_KEY)
        defaults.synchronize()
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
