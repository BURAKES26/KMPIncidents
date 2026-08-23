package com.example.kmpincidents.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual object LocationManager {

    actual fun hasLocationPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        if (!CLLocationManager.locationServicesEnabled()) {
            return Result.failure(Exception("Location services are disabled"))
        }

        return suspendCancellableCoroutine { continuation ->
            val locationManager = CLLocationManager()
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location != null && continuation.isActive) {
                        manager.stopUpdatingLocation()
                        val coords = location.coordinate.useContents { latitude to longitude }
                        LocationRequestHolder.clear()
                        continuation.resume(Result.success(coords))
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    if (continuation.isActive) {
                        LocationRequestHolder.clear()
                        continuation.resume(
                            Result.failure(
                                Exception(didFailWithError.localizedDescription ?: "Failed to get location")
                            )
                        )
                    }
                }

                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    if (!continuation.isActive) return
                    when (manager.authorizationStatus) {
                        kCLAuthorizationStatusAuthorizedWhenInUse,
                        kCLAuthorizationStatusAuthorizedAlways,
                        -> {
                            manager.desiredAccuracy = kCLLocationAccuracyBest
                            manager.requestLocation()
                        }

                        kCLAuthorizationStatusDenied,
                        kCLAuthorizationStatusRestricted,
                        -> {
                            LocationRequestHolder.clear()
                            continuation.resume(
                                Result.failure(SecurityException("Location permission not granted"))
                            )
                        }

                        kCLAuthorizationStatusNotDetermined -> {
                            // Wait for the user response.
                        }

                        else -> {
                            LocationRequestHolder.clear()
                            continuation.resume(
                                Result.failure(SecurityException("Location permission not granted"))
                            )
                        }
                    }
                }
            }

            LocationRequestHolder.retain(locationManager, delegate)
            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest

            when (CLLocationManager.authorizationStatus()) {
                kCLAuthorizationStatusNotDetermined -> {
                    locationManager.requestWhenInUseAuthorization()
                }

                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways,
                -> {
                    locationManager.requestLocation()
                }

                else -> {
                    LocationRequestHolder.clear()
                    continuation.resume(
                        Result.failure(SecurityException("Location permission not granted"))
                    )
                }
            }

            continuation.invokeOnCancellation {
                locationManager.stopUpdatingLocation()
                LocationRequestHolder.clear()
            }
        }
    }

    actual fun observeLocationUpdates(intervalMs: Long): Flow<Pair<Double, Double>> = callbackFlow {
        if (!CLLocationManager.locationServicesEnabled()) {
            close(Exception("Location services are disabled"))
            return@callbackFlow
        }
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val locationManager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                val coords = location.coordinate.useContents { latitude to longitude }
                trySend(coords)
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                // Keep the flow open; transient GPS errors are common.
            }
        }

        // Retain manager + delegate while the flow is active.
        val holder = LocationObserverHolder(locationManager, delegate)
        LocationObserverRegistry.add(holder)

        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        // CoreLocation does not take an interval directly; distanceFilter=0 emits all updates.
        locationManager.distanceFilter = 0.0
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
            LocationObserverRegistry.remove(holder)
        }
    }
}

private object LocationRequestHolder {
    private var manager: CLLocationManager? = null
    private var delegate: CLLocationManagerDelegateProtocol? = null

    fun retain(manager: CLLocationManager, delegate: CLLocationManagerDelegateProtocol) {
        this.manager = manager
        this.delegate = delegate
    }

    fun clear() {
        manager?.delegate = null
        manager = null
        delegate = null
    }
}

private class LocationObserverHolder(
    val manager: CLLocationManager,
    val delegate: CLLocationManagerDelegateProtocol,
)

private object LocationObserverRegistry {
    private val holders = mutableSetOf<LocationObserverHolder>()

    fun add(holder: LocationObserverHolder) {
        holders += holder
    }

    fun remove(holder: LocationObserverHolder) {
        holders -= holder
    }
}
