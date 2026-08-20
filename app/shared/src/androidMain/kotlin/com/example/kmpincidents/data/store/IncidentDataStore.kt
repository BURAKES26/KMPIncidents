package com.example.kmpincidents.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kmpincidents.util.AndroidContextHolder
import com.example.kmpincidents.data.model.IncidentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.incidentDataStore: DataStore<Preferences> by preferencesDataStore(name = "incident_data")

actual class IncidentDataStore actual constructor() {

    private val context = AndroidContextHolder.appContext

    companion object {
        private val SELECTED_INCIDENT_KEY = stringPreferencesKey("selected_incident")
        private val RECENT_INCIDENTS_KEY = stringPreferencesKey("recent_incidents")
    }

    actual suspend fun saveSelectedIncident(incident: IncidentResponse) {
        context.incidentDataStore.edit { preferences ->
            preferences[SELECTED_INCIDENT_KEY] = Json.encodeToString(incident)
        }
    }

    actual val selectedIncident: Flow<IncidentResponse?> = context.incidentDataStore.data
        .map { preferences ->
            preferences[SELECTED_INCIDENT_KEY]?.let { incidentJson ->
                try { Json.decodeFromString<IncidentResponse>(incidentJson) } catch (e: Exception) { null }
            }
        }

    actual suspend fun clearSelectedIncident() {
        context.incidentDataStore.edit { preferences -> preferences.remove(SELECTED_INCIDENT_KEY) }
    }

    actual suspend fun saveRecentIncidents(incidents: List<IncidentResponse>) {
        context.incidentDataStore.edit { preferences ->
            preferences[RECENT_INCIDENTS_KEY] = Json.encodeToString(incidents)
        }
    }

    actual val recentIncidents: Flow<List<IncidentResponse>> = context.incidentDataStore.data
        .map { preferences ->
            preferences[RECENT_INCIDENTS_KEY]?.let { incidentsJson ->
                try { Json.decodeFromString<List<IncidentResponse>>(incidentsJson) } catch (e: Exception) { emptyList() }
            } ?: emptyList()
        }
}