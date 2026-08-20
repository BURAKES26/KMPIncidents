package com.example.kmpincidents.util

import com.example.kmpincidents.data.model.ApiResult
import com.example.kmpincidents.data.store.TokenPreferences
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend inline fun <reified T> performRequest(
    tokenPreferences: TokenPreferences,
    requiresAuth: Boolean = true,
    optionalAuth: Boolean = false,
    crossinline block: suspend (token: String?) -> HttpResponse
): ApiResult<T> {
    val token = if (requiresAuth || optionalAuth) tokenPreferences.getToken() else null

    if (requiresAuth && !optionalAuth && token == null) {
        return ApiResult.Unauthorized
    }

    return try {
        val response = block(token)
        when {
            response.status == HttpStatusCode.Unauthorized -> {
                if (requiresAuth) tokenPreferences.clearToken()
                ApiResult.Unauthorized
            }
            response.status.isSuccess() -> ApiResult.Success(response.body())
            else -> ApiResult.HttpError(response.status.value, response.status.description)
        }
    } catch (e: Exception) {
        ApiResult.Unknown(e)
    }
}