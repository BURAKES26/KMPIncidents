package com.example.kmpincidents.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kmpincidents.data.model.IncidentResponse
import com.example.kmpincidents.data.model.Priority
import com.example.kmpincidents.data.model.Status
import com.example.kmpincidents.ui.icons.CloseIcon
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
    var selectedIncident by remember { mutableStateOf<IncidentResponse?>(null) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            MapLibreBridge.dispose()
        }
    }

    LaunchedEffect(Unit) {
        // Kick off permission/location early so markers can center when available
        if (LocationManager.hasLocationPermission()) {
            LocationManager.getCurrentLocation().onSuccess { userLocation = it }
                .onFailure { /* ignore until explicitly requested */ }
        }
    }

    LaunchedEffect(shouldRequestLocationPermission) {
        if (shouldRequestLocationPermission) {
            val result = LocationManager.getCurrentLocation()
            result.onSuccess {
                userLocation = it
                onLocationPermissionHandled()
            }.onFailure {
                onLocationError(it.message ?: "Location permission denied or unavailable")
                onLocationPermissionHandled()
            }
        }
    }

    LaunchedEffect(userLocation) {
        // light polling while map is shown
        while (true) {
            LocationManager.getCurrentLocation().onSuccess { userLocation = it }
            delay(5.seconds)
        }
    }

    LaunchedEffect(shouldUseCurrentLocation, userLocation) {
        if (shouldUseCurrentLocation) {
            val location = userLocation
            if (location != null) {
                selectedLocation = location
                onLocationSelected(location.first, location.second)
                MapLibreBridge.flyTo(location.first, location.second)
                onCurrentLocationUsed()
            } else {
                LocationManager.getCurrentLocation()
                    .onSuccess {
                        userLocation = it
                        selectedLocation = it
                        onLocationSelected(it.first, it.second)
                        MapLibreBridge.flyTo(it.first, it.second)
                        onCurrentLocationUsed()
                    }
                    .onFailure {
                        onLocationError(it.message ?: "Unable to get current location")
                        onCurrentLocationUsed()
                    }
            }
        }
    }

    LaunchedEffect(incidents, isLocationSelectionEnabled) {
        MapLibreBridge.setCallbacks(
            onIncident = { incident ->
                selectedIncident = incident
                onMapTouch(true)
            },
            onClick = { lat, lon ->
                if (isLocationSelectionEnabled) {
                    selectedLocation = lat to lon
                    onLocationSelected(lat, lon)
                    MapLibreBridge.updateSelectedLocation(lat, lon)
                } else {
                    selectedIncident = null
                }
                onMapTouch(true)
            },
            locationSelection = isLocationSelectionEnabled
        )
        MapLibreBridge.updateIncidents(incidents)
    }

    LaunchedEffect(selectedLocation) {
        MapLibreBridge.updateSelectedLocation(selectedLocation?.first, selectedLocation?.second)
    }

    LaunchedEffect(userLocation) {
        MapLibreBridge.updateUserLocation(userLocation?.first, userLocation?.second)
    }

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                MapLibreBridge.showAndPosition(
                    x = position.x.toDouble(),
                    y = position.y.toDouble(),
                    width = size.width.toDouble(),
                    height = size.height.toDouble()
                )
            }
    ) {
        // Transparent host; MapLibre HTML layer is positioned over this bounds.
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent))

        selectedIncident?.let { incident ->
            WebIncidentInfoCard(
                incident = incident,
                allowDetailNavigation = allowDetailNavigation,
                onClose = { selectedIncident = null },
                onNavigateToDetails = {
                    onIncidentClick(incident)
                    selectedIncident = null
                }
            )
        }
    }
}

@Composable
private fun WebIncidentInfoCard(
    incident: IncidentResponse,
    allowDetailNavigation: Boolean,
    onClose: () -> Unit,
    onNavigateToDetails: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = IncidentDisplayHelper.getCategoryLabel(incident.category),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WebStatusChip(status = incident.status)
                            WebPriorityChip(priority = incident.priority)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = CloseIcon,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                incident.description.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description.take(120) + if (description.length > 120) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Due date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = IncidentDisplayHelper.formatDateForDisplay(incident.dueAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (allowDetailNavigation) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Go to details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onNavigateToDetails() }
                    )
                }
            }
        }
    }
}

@Composable
private fun WebStatusChip(status: Status) {
    val statusColor = IncidentDisplayHelper.getStatusColor(status)
    Surface(shape = RoundedCornerShape(16.dp), color = statusColor.copy(alpha = 0.15f)) {
        Text(
            text = IncidentDisplayHelper.getStatusLabel(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WebPriorityChip(priority: Priority) {
    val (backgroundColor, textColor) = IncidentDisplayHelper.getPriorityColors(priority)
    Surface(shape = RoundedCornerShape(16.dp), color = backgroundColor) {
        Text(
            text = IncidentDisplayHelper.getPriorityLabel(priority),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
