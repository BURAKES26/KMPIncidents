package com.example.kmpincidents.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kmpincidents.data.model.IncidentResponse
import com.example.kmpincidents.data.model.Priority
import com.example.kmpincidents.data.model.Status
import com.example.kmpincidents.util.IncidentDisplayHelper

/**
 * Desktop fallback map: MapLibre Compose is not available on JVM Desktop,
 * so we render a selectable incident list with coordinates instead.
 */
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

    LaunchedEffect(shouldRequestLocationPermission) {
        if (shouldRequestLocationPermission) {
            onLocationPermissionHandled()
        }
    }

    LaunchedEffect(shouldUseCurrentLocation) {
        if (shouldUseCurrentLocation) {
            onLocationError("Current location is not available on desktop")
            onCurrentLocationUsed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF5))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "Map view (desktop fallback)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isLocationSelectionEnabled) {
                    "Select an incident below to use its coordinates, or pick any listed location."
                } else {
                    "Interactive maps are not available on desktop. Browse incidents by location below."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (incidents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No incidents with locations to display",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(incidents, key = { it.id }) { incident ->
                        DesktopMapIncidentRow(
                            incident = incident,
                            selected = selectedIncident?.id == incident.id,
                            onClick = {
                                selectedIncident = incident
                                onMapTouch(true)
                                if (isLocationSelectionEnabled) {
                                    onLocationSelected(incident.latitude, incident.longitude)
                                }
                                if (allowDetailNavigation) {
                                    onIncidentClick(incident)
                                } else if (!isLocationSelectionEnabled) {
                                    onIncidentClick(incident)
                                }
                                onMapTouch(false)
                            }
                        )
                    }
                }
            }
        }

        selectedIncident?.let { incident ->
            DesktopMapInfoCard(
                incident = incident,
                allowDetailNavigation = allowDetailNavigation,
                onClose = { selectedIncident = null },
                onNavigateToDetails = {
                    onIncidentClick(incident)
                    selectedIncident = null
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun DesktopMapIncidentRow(
    incident: IncidentResponse,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = IncidentDisplayHelper.getCategoryLabel(incident.category),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lat ${"%.5f".format(incident.latitude)}, Lon ${"%.5f".format(incident.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DesktopStatusChip(incident.status)
                DesktopPriorityChip(incident.priority)
            }
        }
    }
}

@Composable
private fun DesktopMapInfoCard(
    incident: IncidentResponse,
    allowDetailNavigation: Boolean,
    onClose: () -> Unit,
    onNavigateToDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = IncidentDisplayHelper.getCategoryLabel(incident.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    modifier = Modifier
                        .clickable(onClick = onClose)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DesktopStatusChip(incident.status)
                DesktopPriorityChip(incident.priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (allowDetailNavigation) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "View details",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(onClick = onNavigateToDetails)
                )
            }
        }
    }
}

@Composable
private fun DesktopStatusChip(status: Status) {
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
private fun DesktopPriorityChip(priority: Priority) {
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
