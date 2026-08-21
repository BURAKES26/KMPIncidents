package com.example.kmpincidents.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.*
import com.example.kmpincidents.ui.components.IncidentsTextField
import com.example.kmpincidents.ui.components.LoadingOverlay
import com.example.kmpincidents.viewmodel.RegisterState
import com.example.kmpincidents.viewmodel.RegisterUiState
import com.example.kmpincidents.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val isBusy by viewModel.isLoading.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val registrationSuccessMessage = stringResource(Res.string.registration_successful_you_can_now_log_in)

    LaunchedEffect(uiState.state) {
        if (uiState.state is RegisterState.Success) {
            scope.launch { snackbarHostState.showSnackbar(registrationSuccessMessage) }
            onNavigateToLogin()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
        RegisterContent(
            isBusy = isBusy,
            uiState = uiState,
            onRegister = { username, password, email, confirmPassword ->
                viewModel.register(username, password, email, confirmPassword)
            },
            onClearError = { viewModel.clearRegisterState() },
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

@Composable
private fun RegisterContent(
    isBusy: Boolean,
    uiState: RegisterUiState,
    onRegister: (String, String, String, String) -> Unit,
    onClearError: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF6EE7FF))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.width(360.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(30.dp)) {
                    Text(
                        text = stringResource(Res.string.create_account),
                        fontSize = 24.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (uiState.state is RegisterState.Error) onClearError()
                        },
                        placeholder = stringResource(Res.string.username),
                        isError = uiState.state is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (uiState.state is RegisterState.Error) onClearError()
                        },
                        placeholder = stringResource(Res.string.email),
                        isError = uiState.state is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.state is RegisterState.Error) onClearError()
                        },
                        placeholder = stringResource(Res.string.password),
                        isPassword = true,
                        isError = uiState.state is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (uiState.state is RegisterState.Error) onClearError()
                        },
                        placeholder = stringResource(Res.string.confirm_password),
                        isPassword = true,
                        isError = uiState.state is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (uiState.state is RegisterState.Error) {
                        Text(
                            text = uiState.state.message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Button(
                        onClick = { onRegister(username, password, email, confirmPassword) },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy && username.isNotBlank() && password.isNotBlank() &&
                                email.isNotBlank() && confirmPassword.isNotBlank()
                    ) { Text(stringResource(Res.string.register)) }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(Res.string.already_have_an_account_login_here),
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToLogin() }
                    )
                }
            }
        }

        LoadingOverlay(isLoading = isBusy)
    }
}