package avans.avd.incidents

import avans.avd.utils.currentInstant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.file.Files.deleteIfExists
import kotlin.io.path.Path

class IncidentService(
    private val incidentRepository: IncidentRepository<Long>,
) {
    private val _incidentEvents = MutableSharedFlow<IncidentEvent>(extraBufferCapacity = 64)

    // Stream of incident change events that can be collected by SSE consumers
    val incidentEvents: SharedFlow<IncidentEvent> = _incidentEvents.asSharedFlow()

    suspend fun findAll(): List<Incident> =
        incidentRepository.findAll()

    suspend fun findAllPaginated(page: Int, pageSize: Int): Pair<List<Incident>, Long> {
        return incidentRepository.findAllPaginated(page, pageSize)
    }

    suspend fun findById(id: Long): Incident? =
        incidentRepository.findById(id)

    suspend fun findIncidentsReportedByUser(userId: Long): List<Incident> =
        incidentRepository.findIncidentsForUser(userId)

    suspend fun save(incident: Incident, notifyReporter: Boolean = false): Incident {
        val isNew = incident.id == Incident.NEW_INCIDENT_ID
        val savedIncident = incidentRepository.save(incident)
        _incidentEvents.emit(
            if (isNew) IncidentEvent.Created(savedIncident) else IncidentEvent.Updated(savedIncident, notifyReporter)
        )
        return savedIncident
    }

    suspend fun delete(incidentId: Long): Boolean {
        val foundIncident = incidentRepository.findById(incidentId)
        return if (foundIncident != null) {
            incidentRepository.delete(incidentId)
            foundIncident.images.forEach { imagefile ->
                val imageToDelete = Path(getImageUploadPath(imagefile))
                deleteIfExists(imageToDelete)
            }
            _incidentEvents.emit(IncidentEvent.Deleted(incidentId))
            true
        } else false
    }

    suspend fun changeStatus(incident: Incident, status: Status): Incident {
            val updatedIncident = when (status) {
            Status.RESOLVED -> incident.copy(
                status = status,
                completedAt = currentInstant(),
                updatedAt = currentInstant()
            )
            Status.REPORTED, Status.ASSIGNED -> incident.copy(
                status = status,
                completedAt = null,
                updatedAt = currentInstant()
            )
        }

        // Status changes are only performed by officials/admins, so the reporter should be notified
        return save(updatedIncident, notifyReporter = true)
    }


    suspend fun addImage(incidentId: Long, imageFileName: String): Incident {
        val incident = incidentRepository.findById(incidentId)
            ?: throw IllegalArgumentException("Incident not found: $incidentId")

        // Business logic here: updating the updatedAt timestamp
        val updatedIncident = incident.copy(
            images = incident.images + imageFileName,
            updatedAt = currentInstant()
        )

        // Use specialized repository method with prepared entity
        return save(updatedIncident)
    }


}
