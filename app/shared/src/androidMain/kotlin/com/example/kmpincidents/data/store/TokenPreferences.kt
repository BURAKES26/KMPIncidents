package com.example.kmpincidents.data.store

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.kmpincidents.util.AndroidContextHolder
import com.example.kmpincidents.util.JwtDecoder
import com.example.kmpincidents.data.model.Role
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

actual class TokenPreferences actual constructor() {

    private val context get() = AndroidContextHolder.appContext

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    actual suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    private fun getTokenFlow() = context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }

    actual suspend fun getToken(): String? = getTokenFlow().first()

    actual suspend fun clearToken() {
        context.dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
    }

    actual suspend fun getUserRole(): Role? {
        val token = getToken() ?: return null
        val roleString = JwtDecoder.getRoleFromToken(token)?.uppercase() ?: return null
        return runCatching { Role.valueOf(roleString) }.getOrNull()
    }
}