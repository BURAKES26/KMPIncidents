package com.example.kmpincidents.util

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import web.geolocation.GeolocationPosition
import web.geolocation.GeolocationPositionError
import web.navigator.navigator
import kotlin.coroutines.resume

actual object LocationManager {

    actual fun hasLocationPermission(): Boolean {
        // Browsers prompt on demand; treat geolocation availability as permission capability.
        return jsGeolocationAvailable()
    }

    actual suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        if (!jsGeolocationAvailable()) {
            return Result.failure(Exception("Geolocation is not available in this browser"))
        }

        return suspendCancellableCoroutine { continuation ->
            navigator.geolocation.getCurrentPositionWithCallbacks(
                successCallback = { position: GeolocationPosition ->
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.success(position.coords.latitude to position.coords.longitude)
                        )
                    }
                },
                errorCallback = { error: GeolocationPositionError ->
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(Exception(error.message ?: "Failed to get location"))
                        )
                    }
                }
            )
        }
    }

    actual fun observeLocationUpdates(intervalMs: Long): Flow<Pair<Double, Double>> = callbackFlow {
        if (!jsGeolocationAvailable()) {
            close(Exception("Geolocation is not available in this browser"))
            return@callbackFlow
        }

        val watchId = navigator.geolocation.watchPositionWithCallbacks(
            successCallback = { position: GeolocationPosition ->
                trySend(position.coords.latitude to position.coords.longitude)
            },
            errorCallback = { error: GeolocationPositionError ->
                close(Exception(error.message ?: "Location watch failed"))
            }
        )

        awaitClose {
            navigator.geolocation.clearWatch(watchId)
        }
    }

    private fun jsGeolocationAvailable(): Boolean {
        return runCatching { navigator.geolocation != null }.getOrDefault(false)
    }
}
