package com.example.kmpincidents.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual object LocationManager {

    actual fun hasLocationPermission(): Boolean = false

    actual suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        return Result.failure(UnsupportedOperationException("Location is not available on desktop"))
    }

    actual fun observeLocationUpdates(intervalMs: Long): Flow<Pair<Double, Double>> = emptyFlow()
}
