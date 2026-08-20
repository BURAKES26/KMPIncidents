package com.example.kmpincidents.util

import kotlinx.coroutines.flow.Flow

expect object LocationManager {
    fun hasLocationPermission(): Boolean
    suspend fun getCurrentLocation(): Result<Pair<Double, Double>>
    fun observeLocationUpdates(intervalMs: Long): Flow<Pair<Double, Double>>
}