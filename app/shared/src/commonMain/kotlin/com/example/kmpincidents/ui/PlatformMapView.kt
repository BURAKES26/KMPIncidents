package com.example.kmpincidents.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.kmpincidents.data.model.IncidentResponse

@Composable
expect fun PlatformMapView(
    modifier: Modifier = Modifier,
    incidents: List<IncidentResponse>,
    isLocationSelectionEnabled: Boolean = false,
    allowDetailNavigation: Boolean = false,
    onIncidentClick: (IncidentResponse) -> Unit = {},
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    onMapTouch: (Boolean) -> Unit = {},
    shouldRequestLocationPermission: Boolean = false,
    shouldUseCurrentLocation: Boolean = false,
    onLocationPermissionHandled: () -> Unit = {},
    onCurrentLocationUsed: () -> Unit = {},
    onLocationError: (String) -> Unit = {}
)