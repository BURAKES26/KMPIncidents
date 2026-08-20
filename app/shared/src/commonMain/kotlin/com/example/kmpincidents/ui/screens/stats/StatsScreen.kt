package com.example.kmpincidents.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.stats
import com.example.kmpincidents.data.model.IncidentResponse
import com.example.kmpincidents.navigation.*
import com.example.kmpincidents.util.PlatformStatsChart
import com.example.kmpincidents.ui.components.BottomNavBar
import com.example.kmpincidents.viewmodel.StatsPeriod
import com.example.kmpincidents.viewmodel.StatsViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun StatsScreen(
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToIncidentMap: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: StatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentKey = StatsKey,
                userRole = uiState.userRole,
                onNavigateTo = { route ->
                    when (route) {
                        IncidentListKey -> onNavigateToIncidentList()
                        IncidentMapKey -> onNavigateToIncidentMap()
                        MyIncidentListKey -> onNavigateToMyIncidentList()
                        StatsKey -> { }
                        UserManagementKey -> onNavigateToUserManagement()
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(Res.string.stats),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Official Dashboard - Incident Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PeriodSelector(selectedPeriod = uiState.selectedPeriod, onPeriodSelected = { viewModel.selectPeriod(it) })
                Spacer(modifier = Modifier.height(16.dp))

                val incidents = uiState.incidents
                if (incidents.isEmpty()) {
                    Text("No incidents found to display statistics.")
                } else {
                    IncidentsByCategoryChart(incidents)
                    IncidentsByStatusChart(incidents)
                    IncidentsPerPeriodChart(incidents, uiState.selectedPeriod)
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(selectedPeriod: StatsPeriod, onPeriodSelected: (StatsPeriod) -> Unit) {
    val periods = StatsPeriod.entries.toList()
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                onClick = { onPeriodSelected(period) },
                selected = period == selectedPeriod
            ) {
                Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun IncidentsByCategoryChart(incidents: List<IncidentResponse>) {
    val categoryCounts = incidents.groupingBy { it.category.name }.eachCount()
    ChartCard(title = "By Category") {
        PlatformStatsChart(
            labels = categoryCounts.keys.toList(),
            values = categoryCounts.values.toList(),
            title = "Incidents by Category",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun IncidentsByStatusChart(incidents: List<IncidentResponse>) {
    val statusCounts = incidents.groupingBy { it.status.name }.eachCount()
    ChartCard(title = "By Status") {
        PlatformStatsChart(
            labels = statusCounts.keys.toList(),
            values = statusCounts.values.toList(),
            title = "Incidents by Status",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Parses createdAt (adding a "Z" suffix if missing, same convention as IncidentDisplayHelper.formatDateForDisplay). */
private fun parseCreatedAt(raw: String): Instant {
    val normalized = if (!raw.endsWith("Z") && !raw.contains("+")) "${raw}Z" else raw
    return Instant.parse(normalized)
}

private val monthAbbrev = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val dayAbbrev = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun hourLabel(instant: Instant, tz: TimeZone): String {
    val hour = instant.toLocalDateTime(tz).hour
    return "${hour.toString().padStart(2, '0')}:00"
}

private fun dayOfWeekLabel(instant: Instant, tz: TimeZone): String {
    // kotlinx.datetime.DayOfWeek.MONDAY has isoDayNumber == 1
    val isoDay = instant.toLocalDateTime(tz).dayOfWeek.isoDayNumber
    return dayAbbrev[isoDay - 1]
}

private fun monthLabel(instant: Instant, tz: TimeZone): String {
    val month = instant.toLocalDateTime(tz).month.number
    return monthAbbrev[month - 1]
}

private fun monthDayLabel(instant: Instant, tz: TimeZone): String {
    val date = instant.toLocalDateTime(tz).date
    return "${monthAbbrev[date.month.number - 1]} ${date.day.toString().padStart(2, '0')}"
}

@Composable
fun IncidentsPerPeriodChart(incidents: List<IncidentResponse>, period: StatsPeriod) {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now()

    val filteredIncidents = incidents.filter {
        try {
            val createdAt = parseCreatedAt(it.createdAt)
            when (period) {
                StatsPeriod.DAY -> createdAt > now.minus(1, DateTimeUnit.DAY, tz)
                StatsPeriod.WEEK -> createdAt > now.minus(1, DateTimeUnit.WEEK, tz)
                StatsPeriod.MONTH -> createdAt > now.minus(1, DateTimeUnit.MONTH, tz)
            }
        } catch (e: Exception) {
            false
        }
    }

    val groupedData = filteredIncidents.groupBy {
        try {
            val createdAt = parseCreatedAt(it.createdAt)
            when (period) {
                StatsPeriod.DAY -> monthDayLabel(createdAt, tz)
                StatsPeriod.WEEK -> dayOfWeekLabel(createdAt, tz)
                StatsPeriod.MONTH -> monthLabel(createdAt, tz)
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }.mapValues { it.value.size }

    val allPossibleKeys = when (period) {
        StatsPeriod.DAY -> listOf(monthDayLabel(now, tz))
        StatsPeriod.WEEK -> dayAbbrev
        StatsPeriod.MONTH -> groupedData.keys.toList().sortedBy { monthAbbrev.indexOf(it) }
    }

    ChartCard(title = "Incident Distribution") {
        PlatformStatsChart(
            labels = allPossibleKeys,
            values = allPossibleKeys.map { groupedData[it] ?: 0 },
            title = "Incidents distribution (${period.name})",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(350.dp).padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Box(modifier = Modifier.weight(1f)) { content() }
        }
    }
}