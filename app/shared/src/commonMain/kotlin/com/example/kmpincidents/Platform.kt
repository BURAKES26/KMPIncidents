package com.example.kmpincidents

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform