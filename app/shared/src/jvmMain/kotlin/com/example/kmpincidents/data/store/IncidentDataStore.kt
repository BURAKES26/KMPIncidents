package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

actual class IncidentDataStore actual constructor() {

    private val json = Json { ignoreUnknownKeys = true }
    private val storeDir: File by lazy {
        File(System.getProperty("user.home"), ".kmpincidents").also { it.mkdirs() }
    }
    private val selectedFile: File get() = File(storeDir, "selected_incident.json")
    private val recentFile: File get() = File(storeDir, "recent_incidents.json")

    private val selectedIncidentState = MutableStateFlow(readSelectedIncident())
    private val recentIncidentsState = MutableStateFlow(readRecentIncidents())

    actual suspend fun saveSelectedIncident(incident: IncidentResponse) {
        withContext(Dispatchers.IO) {
            selectedFile.writeText(json.encodeToString(incident))
        }
        selectedIncidentState.value = incident
    }

    actual val selectedIncident: Flow<IncidentResponse?> = selectedIncidentState.asStateFlow()

    actual suspend fun clearSelectedIncident() {
        withContext(Dispatchers.IO) {
            if (selectedFile.exists()) selectedFile.delete()
        }
        selectedIncidentState.value = null
    }

    actual suspend fun saveRecentIncidents(incidents: List<IncidentResponse>) {
        withContext(Dispatchers.IO) {
            recentFile.writeText(json.encodeToString(incidents))
        }
        recentIncidentsState.value = incidents
    }

    actual val recentIncidents: Flow<List<IncidentResponse>> = recentIncidentsState.asStateFlow()

    private fun readSelectedIncident(): IncidentResponse? {
        if (!selectedFile.exists()) return null
        return runCatching {
            json.decodeFromString<IncidentResponse>(selectedFile.readText())
        }.getOrNull()
    }

    private fun readRecentIncidents(): List<IncidentResponse> {
        if (!recentFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<IncidentResponse>>(recentFile.readText())
        }.getOrDefault(emptyList())
    }
}
