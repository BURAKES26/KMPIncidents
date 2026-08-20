package com.example.kmpincidents.ui.screens.management

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpincidents.data.model.IncidentCategory
import com.example.kmpincidents.data.model.IncidentResponse
import com.example.kmpincidents.data.model.Priority
import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.data.model.Status
import com.example.kmpincidents.navigation.IncidentListKey
import com.example.kmpincidents.navigation.PlatformMapViewKey
import com.example.kmpincidents.navigation.MyIncidentListKey
import com.example.kmpincidents.navigation.StatsKey
import com.example.kmpincidents.navigation.UserManagementKey
import com.example.kmpincidents.ui.components.BottomNavBar
import com.example.kmpincidents.ui.components.FilterDialog
import com.example.kmpincidents.util.PlatformMapView
import com.example.kmpincidents.ui.components.LoadingOverlay
import com.example.kmpincidents.ui.components.SearchAndFilterBar
import com.example.kmpincidents.viewmodel.IncidentManagementViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlatformMapViewScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: IncidentManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unauthorizedState = uiState.unauthorizedState
    val userRole = uiState.userRole
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val filteredIncidents = uiState.filteredIncidents
    val hasActiveFilters by remember { derivedStateOf { viewModel.hasActiveFilters } }

    // Refresh data each time user navigates back to this screen
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadAllIncidents()
    }

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            onNavigateToMyIncidentList()
        }
    }

    PlatformMapViewContent(
        userRole = userRole,
        incidents = filteredIncidents,
        isLoading = isLoading,
        hasActiveFilters = hasActiveFilters,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = { q -> viewModel.updateSearchQuery(q) },
        selectedPriority = uiState.selectedPriorityFilter,
        selectedStatus = uiState.selectedStatusFilter,
        selectedCategory = uiState.selectedCategoryFilter,
        onUpdatePriority = { viewModel.updatePriorityFilter(it) },
        onUpdateStatus = { viewModel.updateStatusFilter(it) },
        onUpdateCategory = { viewModel.updateCategoryFilter(it) },
        onClearAllFilters = { viewModel.clearAllFilters() },
        onIncidentClick = { onNavigateToDetail(it.id) },
        onNavigateToIncidentList = onNavigateToIncidentList,
        onNavigateToUserManagement = onNavigateToUserManagement,
        onNavigateToMyIncidentList = onNavigateToMyIncidentList,
        onNavigateToStats = onNavigateToStats
    )
}

@Composable
private fun PlatformMapViewContent(
    userRole: Role?,
    incidents: List<IncidentResponse>,
    isLoading: Boolean,
    hasActiveFilters: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedPriority: Set<Priority>,
    selectedStatus: Set<Status>,
    selectedCategory: Set<IncidentCategory>,
    onUpdatePriority: (Set<Priority>) -> Unit,
    onUpdateStatus: (Set<Status>) -> Unit,
    onUpdateCategory: (Set<IncidentCategory>) -> Unit,
    onClearAllFilters: () -> Unit,
    onIncidentClick: (IncidentResponse) -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentKey = PlatformMapViewKey,
                userRole = userRole,
                onNavigateTo = { route ->
                    when (route) {
                        IncidentListKey -> onNavigateToIncidentList()
                        UserManagementKey -> onNavigateToUserManagement()
                        MyIncidentListKey -> onNavigateToMyIncidentList()
                        StatsKey -> onNavigateToStats()
                        else -> {}
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
            SearchAndFilterBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                hasActiveFilters = hasActiveFilters,
                onFilterClick = { showFilterDialog = true }
            )

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                PlatformMapView(
                    modifier = Modifier.fillMaxSize(),
                    incidents = incidents,
                    isLocationSelectionEnabled = false,
                    allowDetailNavigation = true,
                    onIncidentClick = onIncidentClick,
                    onLocationSelected = { _, _ -> },
                    onMapTouch = { }
                )
            }

            LoadingOverlay(isLoading = isLoading)
        }

        if (showFilterDialog) {
            FilterDialog(
                selectedPriority = selectedPriority,
                selectedStatus = selectedStatus,
                selectedCategory = selectedCategory,
                onUpdatePriority = onUpdatePriority,
                onUpdateStatus = onUpdateStatus,
                onUpdateCategory = onUpdateCategory,
                onClearAll = onClearAllFilters,
                onDismiss = { }
            )
        }
    }
}