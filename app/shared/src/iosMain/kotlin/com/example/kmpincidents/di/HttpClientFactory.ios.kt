package com.example.kmpincidents.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createHttpClient(json: Json): HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(json)
    }
}
