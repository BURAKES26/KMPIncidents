package com.example.kmpincidents.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpincidents.data.model.UserResponse
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.*
import com.example.kmpincidents.ui.components.IncidentsTextField
import com.example.kmpincidents.ui.components.LoadingOverlay
import com.example.kmpincidents.ui.components.TopNavBar
import com.example.kmpincidents.util.ChangeUserValidationHelper
import com.example.kmpincidents.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Pure-Kotlin replacement for java.net.URLDecoder.decode(s, "UTF-8"). */
private fun percentDecode(s: String): String {
    val bytes = ArrayList<Byte>(s.length)
    var i = 0
    while (i < s.length) {
        val c = s[i]
        when {
            c == '%' && i + 2 < s.length -> {
                val hex = s.substring(i + 1, i + 3)
                bytes.add(hex.toInt(16).toByte())
                i += 3
            }
            c == '+' -> { bytes.add(' '.code.toByte()); i++ }
            else -> { bytes.add(c.code.toByte()); i++ }
        }
    }
    return bytes.toByteArray().decodeToString()
}

@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    userJson: String?,
    viewModel: UserViewModel = koinViewModel()
) {
    val initialUser = remember(userJson) {
        userJson?.let { json ->
            try {
                Json.decodeFromString<UserResponse>(percentDecode(json))
            } catch (_: Exception) {
                null
            }
        }
    }

    if (initialUser == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    val isBusy by viewModel.isLoading.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf(initialUser.username) }
    var email by remember { mutableStateOf(initialUser.email) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val updateProfileSuccessMessage = stringResource(Res.string.profile_updated_successfully)
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            scope.launch { snackbarHostState.showSnackbar(updateProfileSuccessMessage) }
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.resetUpdateState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopNavBar(
                    title = stringResource(Res.string.edit_profile),
                    showBackButton = true,
                    onBackClick = { onNavigateBack() },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(Res.string.personal_information),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            IncidentsTextField(value = username, onValueChange = { username = it }, placeholder = stringResource(Res.string.username))
                            IncidentsTextField(value = email, onValueChange = { email = it }, placeholder = stringResource(Res.string.email))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.security),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = stringResource(Res.string.leave_blank_to_keep_current_password),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            IncidentsTextField(value = currentPassword, onValueChange = { currentPassword = it }, placeholder = stringResource(Res.string.current_password), isPassword = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            IncidentsTextField(value = newPassword, onValueChange = { newPassword = it }, placeholder = stringResource(Res.string.new_password), isPassword = true)
                            IncidentsTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = stringResource(Res.string.confirm_new_password), isPassword = true)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        onClick = {
                            val error = ChangeUserValidationHelper.validateUserProfile(
                                username = username,
                                email = email,
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                            if (error == null) {
                                viewModel.updateProfile(username = username, email = email, newPassword = newPassword.ifBlank { null })
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(25.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
                    ) {
                        Text(text = stringResource(Res.string.save_changes), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    }
                }

                TextButton(
                    onClick = { onNavigateBack() },
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    enabled = !isBusy
                ) {
                    Text(text = stringResource(Res.string.cancel), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        LoadingOverlay(isLoading = isBusy)
    }
}