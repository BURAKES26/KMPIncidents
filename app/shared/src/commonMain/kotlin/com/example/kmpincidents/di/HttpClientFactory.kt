package com.example.kmpincidents.di

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect fun createHttpClient(json: Json): HttpClient
