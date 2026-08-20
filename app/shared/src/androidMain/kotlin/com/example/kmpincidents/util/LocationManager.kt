package com.example.kmpincidents.util

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual object LocationManager {

    actual fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            AndroidContextHolder.appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(AndroidContextHolder.appContext)

        return try {
            val lastLocation = getLastLocation(fusedLocationClient)
            if (lastLocation != null) {
                return Result.success(lastLocation.latitude to lastLocation.longitude)
            }

            val freshLocation = requestFreshLocation(fusedLocationClient)
            Result.success(freshLocation.latitude to freshLocation.longitude)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getLastLocation(client: FusedLocationProviderClient): Location? {
        return suspendCancellableCoroutine { continuation ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    @Suppress("MissingPermission")
    private suspend fun requestFreshLocation(client: FusedLocationProviderClient): Location {
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L
            ).apply {
                setMinUpdateIntervalMillis(5000L)
                setMaxUpdateDelayMillis(15000L)
                setWaitForAccurateLocation(false)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null && continuation.isActive) {
                        client.removeLocationUpdates(this)
                        continuation.resume(location)
                    }
                }
            }

            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                client.removeLocationUpdates(locationCallback)
            }

            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (continuation.isActive) {
                    client.removeLocationUpdates(locationCallback)
                    continuation.resumeWithException(
                        Exception("Location request timed out. Please ensure GPS is enabled.")
                    )
                }
            }, 15000L)
        }
    }

    @Suppress("MissingPermission")
    actual fun observeLocationUpdates(intervalMs: Long): Flow<Pair<Double, Double>> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(AndroidContextHolder.appContext)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        ).apply {
            setMinUpdateIntervalMillis(intervalMs / 2)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.latitude to location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}