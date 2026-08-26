package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.kmpincidents.data.model.IncidentCategory
import com.example.kmpincidents.data.model.Priority
import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.data.model.Status
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

object IncidentDisplayHelper {
    fun getStatusColorValue(status: Status): Long = when (status) {
        Status.REPORTED -> 0xFFFFC107L
        Status.ASSIGNED -> 0xFFFF6B35L
        Status.RESOLVED -> 0xFF4CAF50L
    }

    fun getStatusColor(status: Status): Color = Color(getStatusColorValue(status))

    fun getPriorityColorValues(priority: Priority): Pair<Long, Long> = when (priority) {
        Priority.CRITICAL -> 0xFFD32F2FL to 0xFFFFFFFFL
        Priority.HIGH -> 0xFFF57C00L to 0xFFFFFFFFL
        Priority.MEDIUM -> 0xFFFDD835L to 0xFF000000L
        Priority.LOW -> 0xFF66BB6AL to 0xFFFFFFFFL
    }

    fun getPriorityColors(priority: Priority): Pair<Color, Color> {
        val (bg, fg) = getPriorityColorValues(priority)
        return Color(bg) to Color(fg)
    }

    fun getRoleColorValue(role: Role): Long = when (role) {
        Role.ADMIN -> 0xFFE53935L
        Role.OFFICIAL -> 0xFF1E88E5L
        Role.USER -> 0xFF43A047L
    }

    fun getRoleColor(role: Role): Color = Color(getRoleColorValue(role))

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val normalized = if (!dateString.endsWith("Z")) "${dateString}Z" else dateString
            val instant = Instant.parse(normalized)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val day = localDateTime.date.day.toString().padStart(2, '0')
            val month = localDateTime.date.month.number.toString().padStart(2, '0')
            val year = localDateTime.date.year
            "$day-$month-$year"
        } catch (_: Exception) {
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