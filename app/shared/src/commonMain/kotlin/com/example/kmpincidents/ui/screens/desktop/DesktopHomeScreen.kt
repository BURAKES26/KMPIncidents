package com.example.kmpincidents.ui.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpincidents.data.model.ApiResult
import com.example.kmpincidents.data.repository.AuthRepository
import com.example.kmpincidents.data.repository.UserRepository
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.load_more
import com.example.kmpincidents.generated.resources.profile
import com.example.kmpincidents.ui.components.FilterDialog
import com.example.kmpincidents.ui.components.LoadingOverlay
import com.example.kmpincidents.ui.components.SearchAndFilterBar
import com.example.kmpincidents.ui.screens.management.EmptyState
import com.example.kmpincidents.ui.screens.management.IncidentCard
import com.example.kmpincidents.ui.screens.management.IncidentDetailScreen
import com.example.kmpincidents.viewmodel.IncidentManagementViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DesktopHomeScreen(
    onNavigateToProfile: (userJson: String) -> Unit,
    onLogout: () -> Unit,
    onUnauthorized: () -> Unit = onLogout,
    listViewModel: IncidentManagementViewModel = koinViewModel(),
    authRepository: AuthRepository = koinInject(),
    userRepository: UserRepository = koinInject(),
) {
    val uiState by listViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by listViewModel.isLoading.collectAsStateWithLifecycle()
    val filteredIncidents = uiState.filteredIncidents
    val showLoadMore = uiState.showLoadMore

    var selectedIncidentId by remember { mutableStateOf<Long?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var profileError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        listViewModel.refreshIncidents()
    }

    LaunchedEffect(uiState.unauthorizedState) {
        if (uiState.unauthorizedState) {
            onUnauthorized()
        }
    }

    LaunchedEffect(filteredIncidents) {
        if (selectedIncidentId != null && filteredIncidents.none { it.id == selectedIncidentId }) {
            selectedIncidentId = filteredIncidents.firstOrNull()?.id
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DesktopTopBar(
                onNavigateToProfile = {
                    scope.launch {
                        profileError = null
                        when (val result = userRepository.getCurrentUser()) {
                            is ApiResult.Success -> {
                                onNavigateToProfile(Json.encodeToString(result.data))
                            }
                            is ApiResult.Unauthorized -> onUnauthorized()
                            else -> profileError = "Failed to load profile"
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        onLogout()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            profileError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Left pane: incident list
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                ) {
                    SearchAndFilterBar(
                        query = uiState.searchQuery,
                        onQueryChange = { listViewModel.updateSearchQuery(it) },
                        hasActiveFilters = listViewModel.hasActiveFilters,
                        onFilterClick = { showFilterMenu = true }
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (filteredIncidents.isEmpty() && !isLoading) {
                            EmptyState()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredIncidents, key = { it.id }) { incident ->
                                    val isSelected = incident.id == selectedIncidentId
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isSelected) {
                                                    Modifier
                                                        .border(
                                                            width = 2.dp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    ) {
                                        IncidentCard(
                                            incident = incident,
                                            onClick = { selectedIncidentId = incident.id },
                                            onDelete = { incidentId ->
                                                if (selectedIncidentId == incidentId) {
                                                    selectedIncidentId = null
                                                }
                                                listViewModel.deleteIncident(incidentId)
                                            }
                                        )
                                    }
                                }

                                if (showLoadMore) {
                                    item {
                                        Button(
                                            onClick = { listViewModel.loadMoreIncidents() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(stringResource(Res.string.load_more))
                                        }
                                    }
                                }
                            }
                        }

                        LoadingOverlay(isLoading = isLoading)
                    }
                }

                // Right pane: incident detail
                Box(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (selectedIncidentId == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select an incident to view details",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        key(selectedIncidentId) {
                            IncidentDetailScreen(
                                incidentId = selectedIncidentId,
                                onNavigateBack = { selectedIncidentId = null },
                                onNavigateToMyIncidentList = onUnauthorized,
                                onIncidentUpdated = { listViewModel.updateIncidentInList(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showFilterMenu) {
            FilterDialog(
                selectedPriority = uiState.selectedPriorityFilter,
                selectedStatus = uiState.selectedStatusFilter,
                selectedCategory = uiState.selectedCategoryFilter,
                onUpdatePriority = { listViewModel.updatePriorityFilter(it) },
                onUpdateStatus = { listViewModel.updateStatusFilter(it) },
                onUpdateCategory = { listViewModel.updateCategoryFilter(it) },
                onClearAll = { listViewModel.clearAllFilters() },
                onDismiss = { showFilterMenu = false }
            )
        }
    }
}

@Composable
fun DesktopTopBar(
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    showHome: Boolean = false,
    onNavigateHome: () -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "KMP Incidents — Official Desktop",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (showHome) {
                        TextButton(onClick = onNavigateHome) {
                            Text("Home")
                        }
                    }
                    TextButton(onClick = onNavigateToProfile) {
                        Text(stringResource(Res.string.profile))
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color(0xFFD32F2F))
                    }
                }
            }
            HorizontalDivider()
        }
    }
}
