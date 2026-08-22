package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import web.storage.localStorage

private const val SELECTED_INCIDENT_KEY = "selected_incident"
private const val RECENT_INCIDENTS_KEY = "recent_incidents"

actual class IncidentDataStore actual constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    private val selectedIncidentState = MutableStateFlow(readSelectedIncident())
    private val recentIncidentsState = MutableStateFlow(readRecentIncidents())

    actual suspend fun saveSelectedIncident(incident: IncidentResponse) {
        localStorage.setItem(SELECTED_INCIDENT_KEY, json.encodeToString(incident))
        selectedIncidentState.value = incident
    }

    actual val selectedIncident: Flow<IncidentResponse?> = selectedIncidentState.asStateFlow()

    actual suspend fun clearSelectedIncident() {
        localStorage.removeItem(SELECTED_INCIDENT_KEY)
        selectedIncidentState.value = null
    }

    actual suspend fun saveRecentIncidents(incidents: List<IncidentResponse>) {
        localStorage.setItem(RECENT_INCIDENTS_KEY, json.encodeToString(incidents))
        recentIncidentsState.value = incidents
    }

    actual val recentIncidents: Flow<List<IncidentResponse>> = recentIncidentsState.asStateFlow()

    private fun readSelectedIncident(): IncidentResponse? {
        val raw = localStorage.getItem(SELECTED_INCIDENT_KEY) ?: return null
        return runCatching { json.decodeFromString<IncidentResponse>(raw) }.getOrNull()
    }

    private fun readRecentIncidents(): List<IncidentResponse> {
        val raw = localStorage.getItem(RECENT_INCIDENTS_KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<IncidentResponse>>(raw) }.getOrDefault(emptyList())
    }
}
