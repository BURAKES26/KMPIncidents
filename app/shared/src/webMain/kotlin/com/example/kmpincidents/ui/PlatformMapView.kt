package com.example.kmpincidents.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.kmpincidents.data.model.IncidentResponse
import com.example.kmpincidents.util.PlatformMapView as UtilPlatformMapView

@Composable
actual fun PlatformMapView(
    modifier: Modifier,
    incidents: List<IncidentResponse>,
    isLocationSelectionEnabled: Boolean,
    allowDetailNavigation: Boolean,
    onIncidentClick: (IncidentResponse) -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    onMapTouch: (Boolean) -> Unit,
    shouldRequestLocationPermission: Boolean,
    shouldUseCurrentLocation: Boolean,
    onLocationPermissionHandled: () -> Unit,
    onCurrentLocationUsed: () -> Unit,
    onLocationError: (String) -> Unit,
) {
    UtilPlatformMapView(
        modifier = modifier,
        incidents = incidents,
        isLocationSelectionEnabled = isLocationSelectionEnabled,
        allowDetailNavigation = allowDetailNavigation,
        onIncidentClick = onIncidentClick,
        onLocationSelected = onLocationSelected,
        onMapTouch = onMapTouch,
        shouldRequestLocationPermission = shouldRequestLocationPermission,
        shouldUseCurrentLocation = shouldUseCurrentLocation,
        onLocationPermissionHandled = onLocationPermissionHandled,
        onCurrentLocationUsed = onCurrentLocationUsed,
        onLocationError = onLocationError
    )
}
