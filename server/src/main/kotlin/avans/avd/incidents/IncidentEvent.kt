package avans.avd.incidents

sealed class IncidentEvent {
    data class Created(val incident: Incident) : IncidentEvent()

    // notifyReporter indicates that this update was made by an official/admin and the
    // user who reported the incident should receive a push notification about it
    data class Updated(val incident: Incident, val notifyReporter: Boolean = false) : IncidentEvent()
    data class Deleted(val incidentId: Long) : IncidentEvent()
}
