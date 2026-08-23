package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class IncidentDataStore actual constructor() {

    private val json = Json { ignoreUnknownKeys = true }
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    private val selectedIncidentState = MutableStateFlow(readSelectedIncident())
    private val recentIncidentsState = MutableStateFlow(readRecentIncidents())

    actual suspend fun saveSelectedIncident(incident: IncidentResponse) {
        defaults.setObject(json.encodeToString(incident), forKey = SELECTED_INCIDENT_KEY)
        defaults.synchronize()
        selectedIncidentState.value = incident
    }

    actual val selectedIncident: Flow<IncidentResponse?> = selectedIncidentState.asStateFlow()

    actual suspend fun clearSelectedIncident() {
        defaults.removeObjectForKey(SELECTED_INCIDENT_KEY)
        defaults.synchronize()
        selectedIncidentState.value = null
    }

    actual suspend fun saveRecentIncidents(incidents: List<IncidentResponse>) {
        defaults.setObject(json.encodeToString(incidents), forKey = RECENT_INCIDENTS_KEY)
        defaults.synchronize()
        recentIncidentsState.value = incidents
    }

    actual val recentIncidents: Flow<List<IncidentResponse>> = recentIncidentsState.asStateFlow()

    private fun readSelectedIncident(): IncidentResponse? {
        val raw = defaults.stringForKey(SELECTED_INCIDENT_KEY) ?: return null
        return runCatching { json.decodeFromString<IncidentResponse>(raw) }.getOrNull()
    }

    private fun readRecentIncidents(): List<IncidentResponse> {
        val raw = defaults.stringForKey(RECENT_INCIDENTS_KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<IncidentResponse>>(raw) }.getOrDefault(emptyList())
    }

    companion object {
        private const val SELECTED_INCIDENT_KEY = "selected_incident"
        private const val RECENT_INCIDENTS_KEY = "recent_incidents"
    }
}
