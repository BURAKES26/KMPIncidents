
package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.Role

expect class TokenPreferences() {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun getUserRole(): Role?
}