package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

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
    onLocationError: (String) -> Unit
) {
    val firstIncident = incidents.firstOrNull()
    val initialLat = firstIncident?.latitude ?: 41.0082
    val initialLng = firstIncident?.longitude ?: 28.9784

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = initialLng, latitude = initialLat),
            zoom = 12.0
        )
    )

    LaunchedEffect(shouldRequestLocationPermission) {
        if (shouldRequestLocationPermission) {
            onLocationPermissionHandled()
        }
    }

    LaunchedEffect(shouldUseCurrentLocation) {
        if (shouldUseCurrentLocation) {
            onCurrentLocationUsed()
        }
    }

    val features = incidents.map { incident ->
        Feature(
            geometry = Point(Position(longitude = incident.longitude, latitude = incident.latitude)),
            properties = JsonObject(emptyMap()),
            id = JsonPrimitive(incident.id)
        )
    }
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(features))
    )

    MaplibreMap(
        modifier = modifier,
        cameraState = cameraState,
        onMapClick = { pos, _ ->
            if (isLocationSelectionEnabled) {
                onLocationSelected(pos.latitude, pos.longitude)
            }
            ClickResult.Pass
        }
    ) {
        CircleLayer(
            id = "incidents-layer",
            source = source,
            onClick = { features ->
                val clickedId = features.firstOrNull()?.id?.toString()?.toLongOrNull()
                val clickedIncident = incidents.firstOrNull { it.id == clickedId }
                if (clickedIncident != null && allowDetailNavigation) {
                    onIncidentClick(clickedIncident)
                }
                ClickResult.Pass
            }
        )
    }
}