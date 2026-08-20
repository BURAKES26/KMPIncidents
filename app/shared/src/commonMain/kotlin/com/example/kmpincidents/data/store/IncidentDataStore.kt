package com.example.kmpincidents.data.store

import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.coroutines.flow.Flow

expect class IncidentDataStore() {
    suspend fun saveSelectedIncident(incident: IncidentResponse)
    val selectedIncident: Flow<IncidentResponse?>
    suspend fun clearSelectedIncident()
    suspend fun saveRecentIncidents(incidents: List<IncidentResponse>)
    val recentIncidents: Flow<List<IncidentResponse>>
}