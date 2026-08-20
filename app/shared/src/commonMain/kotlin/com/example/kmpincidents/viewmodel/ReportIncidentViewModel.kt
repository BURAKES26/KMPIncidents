package com.example.kmpincidents.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.kmpincidents.data.api.VehicleApi
import com.example.kmpincidents.data.repository.IncidentRepository
import com.example.kmpincidents.util.PhotoFileResolver
import com.example.kmpincidents.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportIncidentUiState(
    val selectedCategory: IncidentCategory = IncidentCategory.COMMUNAL,
    val description: String = "",
    val licensePlateNumber: String = "",
    val vehicleInfo: VehicleInfo? = null,
    val showVehicleInfoDialog: Boolean = false,
    val photos: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null,
    val showSuccessDialog: Boolean = false,
    val createdIncident: IncidentResponse? = null,
    val showPermissionDeniedWarning: Boolean = false,
    val showImageSourceDialog: Boolean = false,
    val shouldRequestLocationPermission: Boolean = false,
    val shouldUseCurrentLocation: Boolean = false
)

class ReportIncidentViewModel(
    private val repository: IncidentRepository,
    private val vehicleApi: VehicleApi,
    private val photoFileResolver: PhotoFileResolver
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState = _uiState.asStateFlow()

    // ... copy updateCategory / updateDescription / updateLicensePlateNumber / searchVehicleInfo /
    // dismissVehicleInfoDialog / addPhoto / removePhoto / updateLocation / clearLocation /
    // showLocationError / requestUseCurrentLocation / onLocationPermissionHandled /
    // onCurrentLocationUsed / showImageSourceDialog / dismissImageSourceDialog /
    // dismissPermissionWarning / onPhotoPermissionResult UNCHANGED from your original file ...

    fun submitReport() {
        val state = _uiState.value
        val latitude = state.latitude
        val longitude = state.longitude

        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a description") }
            return
        }
        if (state.selectedCategory == IncidentCategory.TRAFFIC && state.licensePlateNumber.isNotBlank()) {
            if (state.licensePlateNumber.length != 6 || !state.licensePlateNumber.all { it.isLetterOrDigit() }) {
                _uiState.update { it.copy(errorMessage = "License plate number must be 6 characters long and alphanumeric") }
                return
            }
        }
        if (latitude == null || longitude == null) {
            _uiState.update { it.copy(errorMessage = "Please select a location") }
            return
        }

        viewModelScope.launch {
            withLoading {
                try {
                    val result = repository.createIncident(
                        CreateIncidentRequest(
                            category = state.selectedCategory,
                            description = state.description,
                            latitude = latitude,
                            longitude = longitude,
                            priority = Priority.LOW,
                            licensePlateNumber = if (state.selectedCategory == IncidentCategory.TRAFFIC && state.licensePlateNumber.isNotBlank()) state.licensePlateNumber else null
                        )
                    )
                    when (result) {
                        is ApiResult.Success -> handleIncidentCreated(state.photos, result.data)
                        is ApiResult.HttpError -> setError("Failed to report incident, please try again later")
                        is ApiResult.NetworkError -> setError("Network error occurred while reporting incident")
                        is ApiResult.Timeout -> setError("Request timed out. Please try again.")
                        is ApiResult.Unknown -> setError("Unexpected error occurred while reporting incident.")
                        is ApiResult.Unauthorized -> Unit
                    }
                } catch (e: Exception) {
                    setError("Unexpected error: ${e.message ?: "Please try again"}")
                }
            }
        }
    }

    private suspend fun handleIncidentCreated(photos: List<String>, incident: IncidentResponse) {
        photos.forEach { uriString ->
            val file = photoFileResolver.resolveToFile(uriString)
            file?.let {
                when (repository.uploadImageToIncident(incidentId = incident.id, imageFile = it, description = "")) {
                    is ApiResult.HttpError -> setError("Failed to upload image: ${file.name}")
                    is ApiResult.NetworkError -> setError("Network error uploading image: ${file.name}")
                    is ApiResult.Timeout -> setError("Image upload timed out: ${file.name}")
                    is ApiResult.Unknown -> setError("Unknown error uploading image: ${file.name}")
                    else -> Unit
                }
            }
        }
        _uiState.update { it.copy(showSuccessDialog = true, createdIncident = incident, errorMessage = null) }
    }

    private fun setError(message: String) = _uiState.update { it.copy(errorMessage = message) }
    fun dismissSuccessDialog() = _uiState.update { it.copy(showSuccessDialog = false) }
    fun resetForm() = _uiState.update { ReportIncidentUiState(selectedCategory = IncidentCategory.COMMUNAL) }
}