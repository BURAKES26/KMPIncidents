package com.example.kmpincidents.util

import com.example.kmpincidents.data.model.IncidentResponse

/**
 * Plain global JS functions provided by `mapbridge.js` (loaded from webApp/index.html).
 * These use only primitive/string/function types so they work identically for both
 * Kotlin/JS and Kotlin/Wasm JS interop.
 */
private external fun kmpMapShow(x: Double, y: Double, w: Double, h: Double)
private external fun kmpMapHide()
private external fun kmpMapDispose()
private external fun kmpMapUpdateIncidentsJson(json: String)
private external fun kmpMapUpdateSelectedLocation(lat: Double, lon: Double)
private external fun kmpMapClearSelectedLocation()
private external fun kmpMapUpdateUserLocation(lat: Double, lon: Double)
private external fun kmpMapClearUserLocation()
private external fun kmpMapFlyTo(lat: Double, lon: Double, zoom: Double)
private external fun kmpMapSetLocationSelectionEnabled(enabled: Boolean)
private external fun kmpMapSetOnIncidentClick(callback: (String) -> Unit)
private external fun kmpMapSetOnMapClick(callback: (Double, Double) -> Unit)

/**
 * Lightweight MapLibre GL JS bridge used by the web PlatformMapView.
 * MapLibre is loaded globally from CDN (see webApp index.html); all interaction with
 * the JS map instance happens inside `mapbridge.js`.
 */
internal object MapLibreBridge {

    private var incidentsById: Map<Long, IncidentResponse> = emptyMap()
    private var onIncidentSelected: ((IncidentResponse) -> Unit)? = null
    private var onMapClicked: ((Double, Double) -> Unit)? = null

    fun showAndPosition(x: Double, y: Double, width: Double, height: Double) {
        kmpMapShow(x, y, width, height)
    }

    fun hide() {
        kmpMapHide()
    }

    fun dispose() {
        kmpMapDispose()
        onIncidentSelected = null
        onMapClicked = null
        incidentsById = emptyMap()
    }

    fun setCallbacks(
        onIncident: (IncidentResponse) -> Unit,
        onClick: (Double, Double) -> Unit,
        locationSelection: Boolean,
    ) {
        onIncidentSelected = onIncident
        onMapClicked = onClick
        kmpMapSetLocationSelectionEnabled(locationSelection)
        kmpMapSetOnIncidentClick { idString ->
            val id = idString.toLongOrNull()
            if (id != null) {
                incidentsById[id]?.let { onIncidentSelected?.invoke(it) }
            }
        }
        kmpMapSetOnMapClick { lat, lon ->
            onMapClicked?.invoke(lat, lon)
        }
    }

    fun updateIncidents(incidents: List<IncidentResponse>) {
        incidentsById = incidents.associateBy { it.id }
        kmpMapUpdateIncidentsJson(incidentsToJson(incidents))
    }

    fun updateSelectedLocation(lat: Double?, lon: Double?) {
        if (lat == null || lon == null) {
            kmpMapClearSelectedLocation()
        } else {
            kmpMapUpdateSelectedLocation(lat, lon)
        }
    }

    fun updateUserLocation(lat: Double?, lon: Double?) {
        if (lat == null || lon == null) {
            kmpMapClearUserLocation()
        } else {
            kmpMapUpdateUserLocation(lat, lon)
        }
    }

    fun flyTo(lat: Double, lon: Double, zoom: Double = 15.0) {
        kmpMapFlyTo(lat, lon, zoom)
    }

    private fun incidentsToJson(incidents: List<IncidentResponse>): String {
        val builder = StringBuilder("[")
        incidents.forEachIndexed { index, incident ->
            if (index > 0) builder.append(",")
            builder.append("{\"id\":").append(incident.id)
                .append(",\"lat\":").append(incident.latitude)
                .append(",\"lon\":").append(incident.longitude)
                .append("}")
        }
        builder.append("]")
        return builder.toString()
    }
}
