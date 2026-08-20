package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.CRIME
import com.example.kmpincidents.generated.resources.ENVIRONMENT
import com.example.kmpincidents.generated.resources.COMMUNAL
import com.example.kmpincidents.generated.resources.TRAFFIC
import com.example.kmpincidents.generated.resources.OTHER
import com.example.kmpincidents.generated.resources.LOW
import com.example.kmpincidents.generated.resources.MEDIUM
import com.example.kmpincidents.generated.resources.HIGH
import com.example.kmpincidents.generated.resources.CRITICAL
import com.example.kmpincidents.generated.resources.REPORTED
import com.example.kmpincidents.generated.resources.ASSIGNED
import com.example.kmpincidents.generated.resources.RESOLVED
import com.example.kmpincidents.generated.resources.USER
import com.example.kmpincidents.generated.resources.OFFICIAL
import com.example.kmpincidents.generated.resources.ADMIN
import com.example.kmpincidents.data.model.IncidentCategory
import com.example.kmpincidents.data.model.Priority
import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.data.model.Status
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

object IncidentDisplayHelper {
    fun getStatusColor(status: Status): Color = when (status) {
        Status.REPORTED -> Color(0xFFFFC107)
        Status.ASSIGNED -> Color(0xFFFF6B35)
        Status.RESOLVED -> Color(0xFF4CAF50)
    }

    fun getPriorityColors(priority: Priority): Pair<Color, Color> = when (priority) {
        Priority.CRITICAL -> Color(0xFFD32F2F) to Color.White
        Priority.HIGH -> Color(0xFFF57C00) to Color.White
        Priority.MEDIUM -> Color(0xFFFDD835) to Color.Black
        Priority.LOW -> Color(0xFF66BB6A) to Color.White
    }

    fun getRoleColor(role: Role): Color = when (role) {
        Role.ADMIN -> Color(0xFFE53935)
        Role.OFFICIAL -> Color(0xFF1E88E5)
        Role.USER -> Color(0xFF43A047)
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val normalized = if (!dateString.endsWith("Z")) "${dateString}Z" else dateString
            val instant = Instant.parse(normalized)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val day = localDateTime.date.day.toString().padStart(2, '0')
            val month = localDateTime.date.month.number.toString().padStart(2, '0')
            val year = localDateTime.date.year
            "$day-$month-$year"
        } catch (e: Exception) {
            dateString
        }
    }

    @Composable
    fun getCategoryLabel(category: IncidentCategory): String = stringResource(
        when (category) {
            IncidentCategory.CRIME -> Res.string.CRIME
            IncidentCategory.ENVIRONMENT -> Res.string.ENVIRONMENT
            IncidentCategory.COMMUNAL -> Res.string.COMMUNAL
            IncidentCategory.TRAFFIC -> Res.string.TRAFFIC
            IncidentCategory.OTHER -> Res.string.OTHER
        }
    )

    @Composable
    fun getPriorityLabel(priority: Priority): String = stringResource(
        when (priority) {
            Priority.LOW -> Res.string.LOW
            Priority.MEDIUM -> Res.string.MEDIUM
            Priority.HIGH -> Res.string.HIGH
            Priority.CRITICAL -> Res.string.CRITICAL
        }
    )

    @Composable
    fun getStatusLabel(status: Status): String = stringResource(
        when (status) {
            Status.REPORTED -> Res.string.REPORTED
            Status.ASSIGNED -> Res.string.ASSIGNED
            Status.RESOLVED -> Res.string.RESOLVED
        }
    )

    @Composable
    fun getRoleLabel(role: Role): String = stringResource(
        when (role) {
            Role.USER -> Res.string.USER
            Role.OFFICIAL -> Res.string.OFFICIAL
            Role.ADMIN -> Res.string.ADMIN
        }
    )
}